package linda.tests_unitaires;

import linda.*;
import linda.shm.CentralizedLinda;

import java.util.Collection;

public class test_unitaire_Centralized {

    public void testWrite(CentralizedLinda linda) {
        linda.clean_Tspace();
        
        System.out.println("=== Test Write ===");
        linda.debug("");

        Tuple tuple1 = new Tuple(1, "test1");
        Tuple tuple2 = new Tuple(2, "test2");

        linda.write(tuple1);
        linda.write(tuple2);

        System.out.println("Tuples ajoutés : " + tuple1 + " et " + tuple2);
        linda.debug("");
    }

    public void testTakeImmediate(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test Take (Immediate) ===");

        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("");

        Tuple motif = new Tuple(1, String.class);
        Tuple result = linda.take(motif);

        System.out.println("Motif : " + motif);
        System.out.println("Tuple retiré immédiatement : " + result);
        linda.debug("");
    }

    public void testTakeBlocking(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test Take (Blocking) ===");

        Thread takerThread = new Thread(() -> {
            Tuple motif = new Tuple(2, String.class);
            linda.debug("");
            System.out.println("Thread Take : En attente du tuple " + motif);
            Tuple result = linda.take(motif);
            System.out.println("Thread Take : Tuple retiré après attente : " + result);
            linda.debug("");
        });

        takerThread.start();

        try {
            Thread.sleep(2000); // Attendre 2 secondes avant d'ajouter le tuple
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

    public void testReadImmediate(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test Read (Immediate) ===");
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("");
        Tuple motif = new Tuple(1, String.class);
        Tuple result = linda.read(motif);

        System.out.println("Motif : " + motif);
        System.out.println("Tuple lu immédiatement : " + result);
        linda.debug("");
    }

    public void testReadBlocking(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test Read (Blocking) ===");

        Thread readerThread = new Thread(() -> {
            Tuple motif = new Tuple(2, String.class);
            linda.debug("");
            System.out.println("Thread Read : En attente du tuple " + motif);
            Tuple result = linda.read(motif);
            System.out.println("Thread Read : Tuple lu après attente : " + result);
            linda.debug("");
        });

        readerThread.start();

        try {
            Thread.sleep(2000); // Attendre 2 secondes avant d'ajouter le tuple
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

    public void testReadPriorityOverTake(CentralizedLinda linda) {
        linda.clean_Tspace();
    
        System.out.println("=== Test Priorité des READ sur les TAKE ===");
    
        // Thread pour effectuer un read
        Thread readerThread1 = new Thread(() -> {
            Tuple motif = new Tuple(1, String.class);
            System.out.println("Thread READ 1 : En attente du tuple " + motif);
            Tuple result = linda.read(motif);
            System.out.println("Thread READ 1 : Tuple lu : " + result);
        }, "READ-1");
    
        // Thread pour effectuer un autre read
        Thread readerThread2 = new Thread(() -> {
            Tuple motif = new Tuple(1, String.class);
            System.out.println("Thread READ 2 : En attente du tuple " + motif);
            Tuple result = linda.read(motif);
            System.out.println("Thread READ 2 : Tuple lu : " + result);
        }, "READ-2");
    
        // Thread pour effectuer un take
        Thread takerThread = new Thread(() -> {
            Tuple motif = new Tuple(1, String.class);
            System.out.println("Thread TAKE : En attente du tuple " + motif);
            Tuple result = linda.take(motif);
            System.out.println("Thread TAKE : Tuple retiré : " + result);
        }, "TAKE");
    
        // Démarrer les threads
        readerThread1.start();
        readerThread2.start();
        takerThread.start();
    
        // Ajouter un tuple après un délai
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Tuple tuple1 = new Tuple(1, "test1");
                System.out.println("Ajout du tuple : " + tuple1);
                linda.write(tuple1);
    
                Thread.sleep(2000);
                Tuple tuple2 = new Tuple(1, "test2");
                System.out.println("Ajout du tuple : " + tuple2);
                linda.write(tuple2);
    
                Thread.sleep(2000);
                Tuple tuple3 = new Tuple(1, "test3");
                System.out.println("Ajout du tuple : " + tuple3);
                linda.write(tuple3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    
        // Attendre la fin des threads
        try {
            readerThread1.join();
            readerThread2.join();
            takerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void testTryTake(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test TryTake ===");

        // Cas 1 : Le tuple est présent
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);
        linda.debug("");

        Tuple motif1 = new Tuple(1, String.class);
        Tuple result1 = linda.tryTake(motif1);
        System.out.println("Cas 1 - test récupération d'un tuple avec motif" + motif1);
        System.out.println("Cas 1 - Tuple retiré immédiatement : " + result1);

        // Cas 2 : Le tuple n'est pas présent
        Tuple motif2 = new Tuple(2, String.class);
        Tuple result2 = linda.tryTake(motif2);
        linda.debug("");
        System.out.println("Cas 2 - test récupération d'un tuple avec motif" + motif2);
        System.out.println("Cas 2 - Aucun tuple trouvé : " + result2);
    }

    public void testTryRead(CentralizedLinda linda) {
        linda.clean_Tspace();

        System.out.println("=== Test TryRead ===");

        // Cas 1 : Le tuple est présent
        Tuple tuple1 = new Tuple(1, "test1");
        linda.write(tuple1);

        linda.debug("");

        Tuple motif1 = new Tuple(1, String.class);
        Tuple result1 = linda.tryRead(motif1);
        
        System.out.println("Cas 1 - test lecture d'un tuple avec motif" + motif1);
        System.out.println("Cas 1 - Tuple lu immédiatement : " + result1);

        linda.debug("");

        // Cas 2 : Le tuple n'est pas présent
        Tuple motif2 = new Tuple(2, String.class);
        Tuple result2 = linda.tryRead(motif2);
        System.out.println("Cas 2 - test lecture d'un tuple avec motif" + motif2);
        System.out.println("Cas 2 - Aucun tuple trouvé : " + result2);
    }

    public static void main(String[] args) {
        test_unitaire_Centralized test = new test_unitaire_Centralized();
        CentralizedLinda linda = new CentralizedLinda();

        // Appeler les tests
        test.testWrite(linda);
        test.testTakeImmediate(linda);
        test.testTakeBlocking(linda);
        test.testReadImmediate(linda);
        test.testReadBlocking(linda);
        test.testTryTake(linda);
        test.testTryRead(linda);
    }
}
