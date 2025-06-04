package linda.autres_tests;

import linda.*;
import linda.server.LindaClient;

public class TestPersistence {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java linda.test.TestPersistence //localhost:4000/LindaServer");
            return;
        }

        Linda linda = new LindaClient(args[0]);

        System.out.println("\n--- Écriture de tuples ---");
        linda.write(new Tuple("A", 1));
        linda.write(new Tuple("B", 2));
        linda.write(new Tuple("C", 3));

        System.out.println("Tuples écrits. Attente de 5 secondes pour forcer la sauvegarde...");
        Thread.sleep(5000);  // attendre que l'auto-saver sauvegarde

        System.out.println("\n--- Redémarre le serveur maintenant, puis relance ce client ---");
        System.out.println("Tuples présents :");
        for (Tuple t : linda.readAll(new Tuple(Object.class, Object.class))) {
            System.out.println(" → " + t);
        }

        System.out.println("\n--- Appel eraseAll() pour vider et supprimer la sauvegarde ---");
        linda.takeAll(new Tuple(Object.class, Object.class));
        linda.write(new Tuple("Whiteboard", "ERASEALL"));
        linda.takeAll(new Tuple("Whiteboard", "ERASEALL"));

        System.out.println("Tuples supprimés. Redémarre et re-teste pour voir que le fichier .dat est vide.");
    }
}
