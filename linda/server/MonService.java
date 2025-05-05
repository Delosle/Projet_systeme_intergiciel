package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MonService extends Remote {
    String envoyerMessage(String message) throws RemoteException;
}