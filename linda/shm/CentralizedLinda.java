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
    
    public synchronized void write (Tuple t){
        tupleSpace.add(t);
        notifyAll();

        // Vérifier les événements enregistrés
        ArrayList<Event> triggeredEvents = new ArrayList<>();
        for (Event event : events) {
            if (t.matches(event.template)) {
                // Exécuter le callback
                event.callback.call(t);

                // Si le mode est TAKE, retirer le tuple de l'espace de tuples
                if (event.mode == eventMode.TAKE) {
                    tupleSpace.remove(t);
                }

                // Ajouter l'événement à la liste des événements déclenchés
                triggeredEvents.add(event);
            }
        }

        // Supprimer les événements déclenchés de la liste
        events.removeAll(triggeredEvents);
    }

    // TODO: voir quel Tuple est trouvé en premier
    // Le premier Tuple avec le meme template est renvoyé

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

    public void eventRegister (eventMode mode, eventTiming timing, Tuple template, Callback callback){
        Event event = new Event(mode, timing, template, callback);

        if (mode == eventMode.READ) {
            if (timing == eventTiming.IMMEDIATE) {
                Tuple found_tuple = tryRead(template);
                if (found_tuple != null) {
                    callback.call(found_tuple);
                }
                return;
            }
            events.add(event);
        }
        else if (mode == eventMode.TAKE) {
            if (timing == eventTiming.IMMEDIATE) {
                Tuple found_tuple = tryTake(template);
                if (found_tuple != null) {
                    callback.call(found_tuple);
                }
                return;
            }
            events.add(event);
        }
        
    }

    public void debug(String prefix) {
        System.out.println(prefix + " Tuples actuels dans l'espace de tuples:");
        for (Tuple t : tupleSpace) {
            System.out.println(prefix + " " + t.toString());
        }
    }

}
