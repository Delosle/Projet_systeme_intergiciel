package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.*;


/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    private final List<Tuple> tupleSpace;
    private final Map<Tuple, Queue<Thread>> readQueues;
    private final Map<Tuple, Queue<Thread>> takeQueues;

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

    private final List<Event> events;


    public CentralizedLinda(){
        this.tupleSpace = new ArrayList<>();
        this.readQueues = new HashMap<>();
        this.takeQueues = new HashMap<>();
        this.events = new ArrayList<>();


    }

    private synchronized Queue<Thread> getQueueForTemplate(Map<Tuple, Queue<Thread>> queueMap, Tuple template) {
        return queueMap.computeIfAbsent(template, k -> new LinkedList<>());
    }

    
    public void write(Tuple t) {
        synchronized (this) {
            tupleSpace.add(t);
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
