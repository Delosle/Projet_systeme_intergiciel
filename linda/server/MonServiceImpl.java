package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class MonServiceImpl extends UnicastRemoteObject implements MonService {
    public MonServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String envoyerMessage(String message) throws RemoteException {
        System.out.println("Message reçu du client : " + message);
        return "Message reçu : " + message;
    }
}