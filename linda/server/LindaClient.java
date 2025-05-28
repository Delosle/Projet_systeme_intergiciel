package linda.server;

import linda.*;
import java.rmi.*;
import java.util.Collection;

public class LindaClient implements Linda {
    private RemoteLinda remoteLinda;

    public LindaClient(String serverURI) {
        try {
            this.remoteLinda = (RemoteLinda) Naming.lookup(serverURI);
        } catch (Exception e) {
            System.err.println("Échec de connexion au serveur: " + e.getMessage());
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            remoteLinda.write(t);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur write RMI", e);
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return remoteLinda.take(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur take RMI", e);
        }
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return remoteLinda.read(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur read RMI", e);
        }
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return remoteLinda.tryTake(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur tryTake RMI", e);
        }
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return remoteLinda.tryRead(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur tryRead RMI", e);
        }
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return remoteLinda.takeAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur takeAll RMI", e);
        }
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return remoteLinda.readAll(template);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur readAll RMI", e);
        }
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing,
                            Tuple template, Callback clientCallback) {
        try {
            // 1) créer le stub RMI qui enveloppe ton callback local
            RemoteCallback cbStub = new AsynchronousCallbackImpl(clientCallback);
            // 2) appeler la méthode distante avec ce stub
            System.out.println("CLIENT : enregistrement d’un callback RMI sur " + template);
            remoteLinda.eventRegister(mode, timing, template, cbStub);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur eventRegister RMI", e);
        }
    }

    @Override
    public void debug(String prefix) {
        try {
            remoteLinda.debug(prefix);
        } catch (RemoteException e) {
            throw new RuntimeException("Erreur debug RMI", e);
        }
    }
}


