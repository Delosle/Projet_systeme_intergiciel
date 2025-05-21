package linda.server;

import linda.RemoteLinda;


import java.rmi.*;
import java.rmi.registry.*;

public class LindaServer {
    public static void main(String[] args) {
        try {
            // Création de l'instance du service
            RemoteLinda service = new LindaServerImpl();


            // Création du registre RMI sur le port 4000
            LocateRegistry.createRegistry(4000);

            // Enregistrement du service dans le registre
            Naming.rebind("//localhost:4000/LindaServer", service);

            System.out.println("Serveur Linda démarré et en attente de connexions...");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}