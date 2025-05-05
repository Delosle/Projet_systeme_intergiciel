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

    public synchronized void write (Tuple t){
        tupleSpace.add(t);
        notifyAll();
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
        // TODO: Implement event registration logic here
        throw new UnsupportedOperationException("Event registration not implemented yet.");
    }

    public void debug(String prefix) {
        System.out.println(prefix + " Tuples actuels dans l'espace de tuples:");
        for (Tuple t : tupleSpace) {
            System.out.println(prefix + " " + t.toString());
        }
    }

}
