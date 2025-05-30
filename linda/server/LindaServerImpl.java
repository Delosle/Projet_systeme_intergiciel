package linda.server;

import linda.*;
import linda.shm.CentralizedLinda;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

public class LindaServerImpl extends UnicastRemoteObject implements RemoteLinda {

    private final CentralizedLinda localLinda;

    public LindaServerImpl() throws RemoteException {
    super();
    this.localLinda = new CentralizedLinda();
    }


    public void write(Tuple t) throws RemoteException {
        localLinda.write(t);
    }

    public Tuple take(Tuple template) throws RemoteException {
        return localLinda.take(template);
    }

    public Tuple read(Tuple template) throws RemoteException {
        return localLinda.read(template);
    }
    
    public Tuple tryTake(Tuple template) throws RemoteException {
        return localLinda.tryTake(template);
    }

    public Tuple tryRead(Tuple template) throws RemoteException {
        return localLinda.tryRead(template);
    }

    public Collection<Tuple> takeAll(Tuple template) throws RemoteException {
        return localLinda.takeAll(template);
    }

    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        return localLinda.readAll(template);
    }

    @Override
    public void eventRegister(eventMode mode,
                            eventTiming timing,
                            Tuple template,
                            RemoteCallback remoteCb)
                            throws RemoteException {
        // Créer l'adaptateur local
        Callback localCb = new Callback() {
            @Override
            public void call(Tuple t) {
                try {
                    System.out.println("SERVEUR : reçu eventRegister() pour " + template);
                    remoteCb.call(t);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        // Utiliser le callback local pour l'enregistrement
        localLinda.eventRegister(mode, timing, template, localCb);
    }

    public void debug(String prefix) throws RemoteException {
        localLinda.debug(prefix);
    }
}
//
//     public void debug(String prefix) {
//         System.out.println(prefix + " : " + localLinda.tupleSpace);
//     }
//      