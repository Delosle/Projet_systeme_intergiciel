package linda.server;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

// Classe principale du serveur
public class LindaServer {
    public static void main(String[] args) {
        try {
            // Création de l'instance du service
            MonService service = new MonServiceImpl();

            // Création du registre RMI sur le port 1099
            LocateRegistry.createRegistry(1099);

            // Enregistrement du service dans le registre
            Naming.rebind("rmi://localhost/MonService", service);

            System.out.println("Serveur démarré et en attente de connexions...");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}