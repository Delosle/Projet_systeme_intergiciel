package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.ArrayList;
import java.util.Collection;

/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {
	
    ArrayList<Tuple> tupleSpace;
    

    public CentralizedLinda(){
        tupleSpace = new ArrayList<Tuple>();
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
    
    public synchronized void write(Tuple t) {
        tupleSpace.add(t);
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
    }

    public void debug(String prefix) {
        System.out.println(prefix + " Tuples actuels dans l'espace de tuples:");
        for (Tuple t : tupleSpace) {
            System.out.println(prefix + " " + t.toString());
        }
    }

}
