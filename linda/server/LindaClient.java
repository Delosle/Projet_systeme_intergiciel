package linda.server;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */

//public class LindaClient implements Linda {
	
    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    //public LindaClient(String serverURI) {
        // TO BE COMPLETED
    //}
    
    // TO BE COMPLETED

//}

import java.rmi.*;

public class LindaClient {
    public static void main(String[] args) {
        try {
            // Recherche du service RMI
            MonService service = (MonService) Naming.lookup("rmi://localhost/MonService");

            // Envoi d'un message au serveur
            String message = "Bonjour depuis le client !";
            String reponse = service.envoyerMessage(message);

            // Affichage de la réponse du serveur
            System.out.println("Réponse du serveur : " + reponse);
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion au serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}