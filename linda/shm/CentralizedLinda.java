package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;



/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    ArrayList<Tuple> tupleSpace;
    private final String SAVE_FILE = "tuplespace.dat";
    private volatile boolean modifier = false;  // indique si l’état a changé depuis la dernière sauvegarde
    

    public CentralizedLinda(){
        tupleSpace = new ArrayList<Tuple>();
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

    private final ArrayList<Event> events = new ArrayList<>();

    private synchronized void saveToFile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(tupleSpace);
            modifier = false;
            System.out.println("✅ Tuplespace sauvegardé.");
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde : " + e.getMessage());
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
            System.out.println("✅ Tuplespace restauré depuis le fichier.");
        } catch (Exception e) {
            System.err.println("❌ Erreur de restauration : " + e.getMessage());
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


    
    public synchronized void write(Tuple t) {
        tupleSpace.add(t);
        modifier = true;  // Marquer l'état comme modifié
        notifyAll();

        // Créer une copie de la liste des événements pour éviter les modifications concurrentes
        ArrayList<Event> eventsCopy = new ArrayList<>(events);

        for (Event event : eventsCopy) {
            if (t.matches(event.template)) {
                // Supprimer l'événement d'abord pour éviter les doubles déclenchements
                events.remove(event);

                if (event.mode == eventMode.TAKE) {
                    tupleSpace.remove(t);
                }

                event.callback.call(t);
            }
        }
    }



    public synchronized Tuple take (Tuple template){
        while(true){
            for (Tuple t : tupleSpace){
                if (t.matches(template)) {
                    Tuple ret_tuple = t;
                    tupleSpace.remove(t);
                    modifier = true;  // Marquer l'état comme modifié
                    return ret_tuple;
                }
            }
            try {
                // Attendre qu'un autre thread ajoute un tuple correspondant
                wait();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Réinterrompre le thread
                return null;
            }
        }
    }

    public synchronized Tuple read (Tuple template){
        while(true){
            for (Tuple t:tupleSpace){
                if (t.matches(template)) {
                    return t;
                }
            }
            try {
                // Attendre qu'un autre thread ajoute un tuple correspondant
                wait();
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Réinterrompre le thread
                return null;
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
            }
        }

        // Vérifier si un événement identique existe déjà
        for (Event e : events) {
            if (e.template.equals(template) && e.callback == callback && e.mode == mode) {
                return; // Ne pas ajouter de doublon
            }
        }

        // Ajouter l'événement à la liste
        events.add(event);
    }

    public void clean_Tspace() {
        tupleSpace.clear();
        new File(SAVE_FILE).delete();  // suppression du fichier
        modifier = false;                 // plus rien à sauvegarder
        System.out.println("✅ Tuplespace nettoyé.");
    }

    public void debug(String prefix) {
        System.out.println(prefix + " Tuples actuels dans l'espace de tuples:");
        for (Tuple t : tupleSpace) {
            System.out.println(prefix + " " + t.toString());
        }
    }

}
