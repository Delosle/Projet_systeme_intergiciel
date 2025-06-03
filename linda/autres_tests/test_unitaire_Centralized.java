package linda.autres_tests;

import linda.*;
import linda.server.LindaClient;
import linda.shm.CentralizedLinda;

import java.util.Collection;

public class test_unitaire_Centralized {

    public void testWrite(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test Write ===");
        Tuple tuple1 = new Tuple(1, "test1");
        Tuple tuple2 = new Tuple(2, "test2");
        linda.debug("Avant les write");
        linda.write(tuple1);
        linda.write(tuple2);
        linda.debug("Apres les write");
    }

    public void testTakeImmediate(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test Take (Immediate) ===");

        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("Avant le Take (Immediate)");

        Tuple motif = new Tuple(1, String.class);
        Tuple result = linda.take(motif);

        System.out.println("Motif : " + motif);
        System.out.println("Tuple retiré immédiatement : " + result);
        linda.debug("Apres le Take (Immediate)");
    }

    public void testTakeBlocking(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test Take (Blocking) ===");

        Thread takerThread = new Thread(() -> {
            Tuple motif = new Tuple(2, String.class);
            linda.debug("Avant le Take (Blocking)");
            System.out.println("Thread Take : En attente du tuple " + motif);
            Tuple result = linda.take(motif);
            System.out.println("Thread Take : Tuple retiré après attente : " + result);
            linda.debug("Apres le Take (Blocking)");
        });

        takerThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Tuple tuple2 = new Tuple(2, "test2");
        System.out.println("Ajout du tuple " + tuple2);
        linda.write(tuple2);

        try {
            takerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testReadImmediate(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test Read (Immediate) ===");
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("Avant Read (Immediate)");
        Tuple motif = new Tuple(1, String.class);
        Tuple result = linda.read(motif);

        System.out.println("Motif : " + motif);
        System.out.println("Tuple lu immédiatement : " + result);
        linda.debug("Après Read (Immediate)");
    }

    public void testReadBlocking(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test Read (Blocking) ===");

        Thread readerThread = new Thread(() -> {
            Tuple motif = new Tuple(2, String.class);
            linda.debug("Avant Read (Blocking)");
            System.out.println("Thread Read : En attente du tuple " + motif);
            Tuple result = linda.read(motif);
            System.out.println("Thread Read : Tuple lu après attente : " + result);
            linda.debug("Après Read (Blocking)");
        });

        readerThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Tuple tuple2 = new Tuple(2, "test2");
        System.out.println("Ajout du tuple " + tuple2);
        linda.write(tuple2);

        try {
            readerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testTryTake(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test TryTake ===");

        // Cas 1 : Le tuple est présent
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("Avant TryTake (essai 1)");

        Tuple motif1 = new Tuple(1, String.class);
        Tuple result1 = linda.tryTake(motif1);
        System.out.println("Cas 1 - test récupération d'un tuple avec motif" + motif1);
        System.out.println("Cas 1 - Tuple retiré immédiatement : " + result1);
        linda.debug("Après TryTake (essai 1)");

        // Cas 2 : Le tuple n'est pas présent
        Tuple motif2 = new Tuple(2, String.class);

        Tuple result2 = linda.tryTake(motif2);
        linda.debug("");
        System.out.println("Cas 2 - test récupération d'un tuple avec motif" + motif2);
        System.out.println("Cas 2 - Aucun tuple trouvé : " + result2);
    }

    public void testTryRead(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test TryRead ===");

        // Cas 1 : Le tuple est présent
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);

        linda.debug("Avant TryRead (essai 1)");

        Tuple motif1 = new Tuple(1, String.class);
        Tuple result1 = linda.tryRead(motif1);
        
        System.out.println("Cas 1 - test lecture d'un tuple avec motif" + motif1);
        System.out.println("Cas 1 - Tuple lu immédiatement : " + result1);

        linda.debug("Apres TryRead (essai 1)");

        // Cas 2 : Le tuple n'est pas présent
        Tuple motif2 = new Tuple(2, String.class);
        Tuple result2 = linda.tryRead(motif2);
        System.out.println("Cas 2 - test lecture d'un tuple avec motif" + motif2);
        System.out.println("Cas 2 - Aucun tuple trouvé : " + result2);
    }

    private void cleanTupleSpace(Linda linda) {
        if (linda instanceof CentralizedLinda) {
            ((CentralizedLinda) linda).clean_Tspace();
        } else if (linda instanceof LindaClient) {
            ((LindaClient) linda).clean_Tspace();
        } else {
            System.out.println("clean_Tspace() non disponible pour ce type: " + linda.getClass().getSimpleName());
        }
    }

    public void testTakeAll(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test TakeAll ===");

        // Ajouter plusieurs tuples qui matchent le même motif
        Tuple tuple1 = new Tuple(1, "test1");
        Tuple tuple2 = new Tuple(1, "test2");
        Tuple tuple3 = new Tuple(1, "test3");
        Tuple tuple4 = new Tuple(2, "autre"); // Ne matche pas

        linda.write(tuple1);
        linda.write(tuple2);
        linda.write(tuple3);
        linda.write(tuple4);

        System.out.println("Tuples ajoutés : " + tuple1 + ", " + tuple2 + ", " + tuple3 + ", " + tuple4);
        linda.debug("Avant takeAll");

        // Tester takeAll avec un motif qui matche plusieurs tuples
        Tuple motif = new Tuple(1, String.class);
        Collection<Tuple> results = linda.takeAll(motif);

        System.out.println("Motif utilisé : " + motif);
        System.out.println("Tuples récupérés avec takeAll : " + results);
        System.out.println("Nombre de tuples récupérés : " + results.size());

        linda.debug("Après takeAll");

        // Tester takeAll avec un motif qui ne matche aucun tuple
        Tuple motif2 = new Tuple(3, String.class);
        Collection<Tuple> results2 = linda.takeAll(motif2);
        System.out.println("Test avec motif qui ne matche rien : " + motif2);
        System.out.println("Résultat : " + results2 + " (taille: " + results2.size() + ")");
    }

    public void testReadAll(Linda linda) {
        cleanTupleSpace(linda);

        System.out.println("=== Test ReadAll ===");

        // Ajouter plusieurs tuples qui matchent le même motif
        Tuple tuple1 = new Tuple(1, "test1");
        Tuple tuple2 = new Tuple(1, "test2");
        Tuple tuple3 = new Tuple(1, "test3");
        Tuple tuple4 = new Tuple(2, "autre"); // Ne matche pas

        linda.write(tuple1);
        linda.write(tuple2);
        linda.write(tuple3);
        linda.write(tuple4);

        System.out.println("Tuples ajoutés : " + tuple1 + ", " + tuple2 + ", " + tuple3 + ", " + tuple4);
        linda.debug("Avant readAll");

        // Tester readAll avec un motif qui matche plusieurs tuples
        Tuple motif = new Tuple(1, String.class);
        Collection<Tuple> results = linda.readAll(motif);

        System.out.println("Motif utilisé : " + motif);
        System.out.println("Tuples lus avec readAll : " + results);
        System.out.println("Nombre de tuples lus : " + results.size());

        linda.debug("Après readAll : ");

        // Vérifier que les tuples sont encore dans l'espace
        Tuple motifVerification = new Tuple(1, String.class);
        Collection<Tuple> verification = linda.readAll(motifVerification);
        System.out.println("Vérification - tuples encore présents : " + verification.size());

        // Tester readAll avec un motif qui ne matche aucun tuple
        Tuple motif2 = new Tuple(3, String.class);
        Collection<Tuple> results2 = linda.readAll(motif2);
        System.out.println("Test avec motif qui ne matche rien : " + motif2);
        System.out.println("Résultat : " + results2 + " (taille: " + results2.size() + ")");

        // Tester readAll avec un motif très générique
        Tuple motifGenerique = new Tuple(Integer.class, String.class);
        Collection<Tuple> resultsGenerique = linda.readAll(motifGenerique);
        System.out.println("Test avec motif générique : " + motifGenerique);
        System.out.println("Résultat générique : " + resultsGenerique + " (taille: " + resultsGenerique.size() + ")");
    }

    public void testCleanTspace(Linda linda) {
        System.out.println("=== Test Clean_Tspace ===");

        // Ajouter plusieurs tuples
        Tuple tuple1 = new Tuple(1, "test1");
        Tuple tuple2 = new Tuple(2, "test2");
        Tuple tuple3 = new Tuple(3, "test3");

        linda.write(tuple1);
        linda.write(tuple2);
        linda.write(tuple3);

        System.out.println("Tuples ajoutés : " + tuple1 + ", " + tuple2 + ", " + tuple3);
        linda.debug("Avant clean_Tspace");

        // Vérifier qu'il y a des tuples avec readAll
        Tuple motifGenerique = new Tuple(Integer.class, String.class);
        Collection<Tuple> avant = linda.readAll(motifGenerique);
        System.out.println("Nombre de tuples avant nettoyage : " + avant.size());

        // Nettoyer l'espace de tuples
        cleanTupleSpace(linda);
        System.out.println("Nettoyage effectué avec clean_Tspace()");

        linda.debug("Après clean_Tspace");

        // Vérifier que l'espace est vide
        Collection<Tuple> apres = linda.readAll(motifGenerique);
        System.out.println("Nombre de tuples après nettoyage : " + apres.size());

        // Test avec tryRead pour confirmer que l'espace est vide
        Tuple testVide = linda.tryRead(new Tuple(Integer.class, String.class));
        System.out.println("Test tryRead après clean : " + testVide + " (doit être null)");

        // Test avec tryTake pour confirmer que l'espace est vide
        Tuple testVide2 = linda.tryTake(new Tuple(Integer.class, String.class));
        System.out.println("Test tryTake après clean : " + testVide2 + " (doit être null)");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java linda.tests_unitaires.test_unitaire_Centralized //localhost:4000/LindaServer");
            return;
        }
        test_unitaire_Centralized test = new test_unitaire_Centralized();
        Linda linda = new LindaClient(args[0]);

        test.testCleanTspace(linda);
        test.testWrite(linda);
        test.testTakeImmediate(linda);
        test.testTakeBlocking(linda);
        test.testReadImmediate(linda);
        test.testReadBlocking(linda);
        test.testTryTake(linda);
        test.testTryRead(linda);
        test.testTakeAll(linda);
        test.testReadAll(linda);
        
        System.out.println("=== Tests terminés ===");
        System.exit(0);
    }
}
