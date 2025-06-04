package linda.autres_tests;

import linda.*;
import linda.server.LindaClient;
import linda.Tuple;


public class testSupprimeTupleClient {
    public static void main(String[] args) throws Exception {
        LindaClient client = new LindaClient("//localhost:4000/LindaServer");

        // Ajouter un tuple
        client.write(new Tuple(42, "test"));
        System.out.println("Tuple écrit.");

        // Effacer tout
        client.eraseAll();
        System.out.println("Espace vidé et fichier supprimé.");
    }
}
