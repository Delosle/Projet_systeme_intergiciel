package linda.server;

import linda.Tuple;
import linda.Callback;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implémentation RMI asynchrone du callback côté client.
 */
public class AsynchronousCallbackImpl 
    extends UnicastRemoteObject 
    implements RemoteCallback {

    private final Callback clientCallback;

    /**
     * @param clientCallback le Callback local du client
     */
    public AsynchronousCallbackImpl(Callback clientCallback) 
            throws RemoteException {
        super();  // exporte cet objet en tant que stub RMI
        this.clientCallback = clientCallback;
    }

    /**
     * Méthode appelée par le serveur RMI.
     * On crée un thread pour exécuter le callback local sans bloquer.
     */
    @Override
    public void call(final Tuple t) throws RemoteException {
        new Thread(() -> {
            clientCallback.call(t);
        }).start();
    }
}
