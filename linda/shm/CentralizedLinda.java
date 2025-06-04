
package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;



/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    ArrayList<Tuple> tupleSpace;
    private final Map<Tuple, Queue<Thread>> readQueues;
    private final Map<Tuple, Queue<Thread>> takeQueues;
    private final String SAVE_FILE = "tuplespace.dat";
    private volatile boolean modifier = false;  // indique si l’état a changé depuis la dernière sauvegarde
    
    private final List<Event> events;

    public CentralizedLinda(){
        this.tupleSpace = new ArrayList<>();
        this.readQueues = new HashMap<>();
        this.takeQueues = new HashMap<>();
        this.events = new ArrayList<>();
        loadFromFile();
        startAutoSaver();
    }


    private static class Event{
        eventMode mode;
        eventTiming timing;
        Tuple template;
        Callback callback;
        
        Event(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
            this.mode = mode;
            this.timing = timing;
            this.template = template;
            this.callback = callback;
        }
    }

    //private final ArrayList<Event> events = new ArrayList<>();

    private synchronized void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(tupleSpace);
            modifier = false;
            System.out.println("Tuplespace sauvegardé.");
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            tupleSpace = (ArrayList<Tuple>) in.readObject();
            System.out.println("Tuplespace restauré depuis le fichier.");
        } catch (Exception e) {
            System.err.println("Erreur de restauration : " + e.getMessage());
        }
    }

    private void startAutoSaver() {
        Thread saver = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                    if (modifier) { 
                        saveToFile();
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        saver.setDaemon(true);
        saver.start();
    }

    private synchronized Queue<Thread> getQueueForTemplate(Map<Tuple, Queue<Thread>> queueMap, Tuple template) {
        return queueMap.computeIfAbsent(template, k -> new LinkedList<>());
    }

    
    public void write(Tuple t) {
        synchronized (this) {
            tupleSpace.add(t);
            modifier = true;  // Marquer l'état comme modifié
        }
    

        wakeNextReaderOrTaker(t);

        // Gestion des callbacks avec priorité READ sur TAKE
        List<Event> eventsCopy = new ArrayList<>(events);

        // Traitement de tous les READ d'abord
        for (Event event : eventsCopy) {
            if (t.matches(event.template) && event.mode == eventMode.READ) {
                events.remove(event);
                event.callback.call(t);
            }
        }
        // traitement du premier TAKE, mais seulement si le tuple est encore présent
        for (Event event : eventsCopy) {
            if (t.matches(event.template) && event.mode == eventMode.TAKE) {
                synchronized (this) {
                    // Recherche du tuple dans l'espace de tuple
                    Tuple tupleToRemove = null;
                    for (Tuple tupleInSpace : tupleSpace) {
                        if (tupleInSpace.matches(event.template)) {
                            tupleToRemove = tupleInSpace;
                            break;
                        }
                    }
                    
                    // Appel du Callback, supression du tuple de la liste et suppression du callback de la liste
                    if (tupleToRemove != null) {
                        events.remove(event);
                        tupleSpace.remove(tupleToRemove);
                        event.callback.call(tupleToRemove);
                        return;
                    }
                }
            }
        }
    }



    public Tuple take (Tuple template) {
        synchronized (this) {
            for (Tuple t : tupleSpace) {
                if (t.matches(template)) {
                    tupleSpace.remove(t);
                    modifier = true;  // Marquer l'état comme modifié
                    return t;
                }
            }

            Queue<Thread> queue = getQueueForTemplate(takeQueues, template);
            queue.add(Thread.currentThread());
        }

        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return take(template);
    }



    
    
    public Tuple read(Tuple template) {
        synchronized (this) {
            for (Tuple t : tupleSpace) {
                if (t.matches(template)) {
                    return t;
                }
            }

            Queue<Thread> queue = getQueueForTemplate(readQueues, template);
            queue.add(Thread.currentThread());
        }

        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        Tuple result = read(template);
        wakeNextReaderOrTaker(template);
        return result;
    }




    private void wakeNextReaderOrTaker(Tuple writtenTuple) {
        synchronized (this) {
            for (Map.Entry<Tuple, Queue<Thread>> entry : readQueues.entrySet()) {
                if (writtenTuple.matches(entry.getKey())) {
                    Queue<Thread> queue = entry.getValue();
                    if (!queue.isEmpty()) {
                        Thread thread = queue.poll();
                        synchronized (thread) {
                            thread.notify();
                        }
                        if (queue.isEmpty()) {
                            readQueues.remove(entry.getKey());
                        }
                        return;
                    }
                }
            }

            for (Map.Entry<Tuple, Queue<Thread>> entry : takeQueues.entrySet()) {
                if (writtenTuple.matches(entry.getKey())) {
                    Queue<Thread> queue = entry.getValue();
                    if (!queue.isEmpty()) {
                        Thread thread = queue.poll();
                        synchronized (thread) {
                            thread.notify();
                        }
                        System.out.println("→ Réveil TAKE : " + thread.getName());
                        if (queue.isEmpty()) {
                            takeQueues.remove(entry.getKey());
                        }
                        return;
                    }
                }
            }
        }
    }



    public synchronized Tuple tryTake(Tuple template){
        for (Tuple t:tupleSpace){
            if (t.matches(template)) {
                Tuple ret_tuple = t;
                tupleSpace.remove(t);
                modifier = true;  // Marquer l'état comme modifié
                return ret_tuple;
            }
        }
        return null;
    }

    public synchronized Tuple tryRead(Tuple template){
        for (Tuple t:tupleSpace){
            if (t.matches(template)) {
                return t; 
            }
        }
        return null;
    }

    public synchronized Collection<Tuple> takeAll (Tuple template){
        ArrayList<Tuple> tupleToSend = new ArrayList<Tuple>();

        for (Tuple t:tupleSpace){
            if (t.matches(template)){
                tupleToSend.add(t);
                modifier = true; // Marquer l'état comme modifié
            }
        }
        tupleSpace.removeAll(tupleToSend);
        return tupleToSend;
    }

    public synchronized Collection<Tuple> readAll (Tuple template){
        ArrayList<Tuple> tupleToSend = new ArrayList<Tuple>();

        for (Tuple t:tupleSpace){
            if (t.matches(template)){
                tupleToSend.add(t);
            }
        }
        return tupleToSend;
    }

    

    public synchronized void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        Event event = new Event(mode, timing, template, callback);

        if (timing == eventTiming.IMMEDIATE) {
            Tuple found_tuple = (mode == eventMode.READ) ? tryRead(template) : tryTake(template);
            if (found_tuple != null) {
                callback.call(found_tuple);
                return;
            }
        }

        for (Event e : events) {
            if (e.template.equals(template) && e.callback == callback && e.mode == mode) {
                return;
            }
        }

        events.add(event);
    }



    public void clean_Tspace() {
        tupleSpace.clear();
        new File(SAVE_FILE).delete();  // suppression du fichier
        modifier = false;                 // plus rien à sauvegarder
        System.out.println("Tuplespace nettoyé.");
    }

    public void debug(String prefix) {
        System.out.println(prefix + "--------------------------");
        System.out.println(prefix + " Tuples actuels :");
        for (Tuple t : tupleSpace) {
            System.out.println(prefix + " " + t);
        }
        System.out.println(prefix + "--------------------------");
        System.out.println(prefix + " Callbacks enregistrés :");
        for (Event e : events) {
            System.out.println(prefix + " Mode: " + e.mode + ", Timing: " + e.timing + ", Template: " + e.template);
        }
        System.out.println(prefix + "--------------------------");
    }


}
