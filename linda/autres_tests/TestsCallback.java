package linda.autres_tests;

import linda.*;
import linda.shm.CentralizedLinda;

public class TestsCallback {

    public static void testCCW() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(1), t -> {
            System.out.println("Callback 1: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(1), t -> {
            System.out.println("Callback 2: " + t);
        });

        linda.debug("Avant write (testCCW)");
        linda.write(new Tuple(1));
        linda.debug("Après write (testCCW)");
    }

    public static void testCWC() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(2), t -> {
            System.out.println("Callback 1: " + t);
        });

        linda.debug("Avant write (testCWC)");
        linda.write(new Tuple(2));
        linda.debug("Après write (testCWC)");

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(2), t -> {
            System.out.println("Callback 2: " + t);
        });
    }

    public static void testWCC() {
        Linda linda = new CentralizedLinda();

        linda.debug("Avant write (testWCC)");
        linda.write(new Tuple(3));
        linda.debug("Après write (testWCC)");

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(3), t -> {
            System.out.println("Callback 1: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(3), t -> {
            System.out.println("Callback 2: " + t);
        });
    }

    public static void testCCW_TakeRead() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(4), t -> {
            System.out.println("Callback TAKE: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(4), t -> {
            System.out.println("Callback READ: " + t);

            
        });

        linda.debug("Avant write (testCCW_TakeRead)");
        linda.write(new Tuple(4));
        linda.debug("Après write (testCCW_TakeRead)");
    }

    public static void testCCW_Immediate() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(5), t -> {
            System.out.println("Callback 1 (IMMEDIATE): " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(5), t -> {
            System.out.println("Callback 2 (IMMEDIATE): " + t);
        });

        linda.debug("Avant write (testCCW_Immediate)");
        linda.write(new Tuple(5));
        linda.debug("Après write (testCCW_Immediate)");
    }

    public static void testCWC_Immediate() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(6), t -> {
            System.out.println("Callback 1 (IMMEDIATE): " + t);
        });

        linda.debug("Avant write (testCWC_Immediate)");
        linda.write(new Tuple(6));
        linda.debug("Après write (testCWC_Immediate)");

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(6), t -> {
            System.out.println("Callback 2 (IMMEDIATE): " + t);
        });
    }

    public static void testWCC_TakeRead_Immediate() {
        Linda linda = new CentralizedLinda();

        linda.debug("Avant write (testCCW_TakeRead_Immediate)");
        linda.write(new Tuple(7));
        linda.debug("Après write (testCCW_TakeRead_Immediate)");

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(7), t -> {
            System.out.println("Callback TAKE (IMMEDIATE): " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(7), t -> {
            System.out.println("Callback READ (IMMEDIATE): " + t);
        });
        linda.debug("Après callbacks (testCCW_TakeRead_Immediate)");
    }

    public static void testWCC_Immediate() {
        Linda linda = new CentralizedLinda();

        linda.debug("Avant write (testWCC_Immediate)");
        linda.write(new Tuple(8));
        linda.debug("Après write (testWCC_Immediate)");

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(8), t -> {
            System.out.println("Callback 1 (IMMEDIATE): " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(8), t -> {
            System.out.println("Callback 2 (IMMEDIATE): " + t);
        });
    }

    public static void testTTW() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(9), t -> {
            System.out.println("Callback TAKE 1: " + t);
        });

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(9), t -> {
            System.out.println("Callback TAKE 2: " + t);
        });

        linda.debug("Avant write (testTTW)");
        linda.write(new Tuple(9));
        linda.debug("Après write (testTTW)");
    }

    public static void testTRW() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(10), t -> {
            System.out.println("Callback TAKE: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(10), t -> {
            System.out.println("Callback READ: " + t);
        });

        linda.debug("Avant write (testTRW)");
        linda.write(new Tuple(10));
        linda.debug("Après write (testTRW)");
    }

    public static void testRTW() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(11), t -> {
            System.out.println("Callback READ: " + t);
        });

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(11), t -> {
            System.out.println("Callback TAKE: " + t);
        });

        linda.debug("Avant write (testRTW)");
        linda.write(new Tuple(11));
        linda.debug("Après write (testRTW)");
    }

    public static void testWTT_Immediate() {
        Linda linda = new CentralizedLinda();

        linda.debug("Avant write (testWTT_Immediate)");
        linda.write(new Tuple(13));
        linda.debug("Après write (testWTT_Immediate)");

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(13), t -> {
            System.out.println("Callback TAKE 1 (IMMEDIATE): " + t);
        });

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(13), t -> {
            System.out.println("Callback TAKE 2 (IMMEDIATE): " + t);
        });
    }

    public static void testNoMatch() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(100), t -> {
            System.out.println("Callback 1: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(200), t -> {
            System.out.println("Callback 2: " + t);
        });

        linda.debug("Avant write (testNoMatch)");
        linda.write(new Tuple(300)); // Aucun callback ne doit être appelé
        linda.debug("Après write (testNoMatch)");
    }

    public static void testOneMatch() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(400), t -> {
            System.out.println("Callback 1: " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(401), t -> {
            System.out.println("Callback 2: " + t);
        });

        linda.debug("Avant write (testOneMatch)");
        linda.write(new Tuple(400)); // Seul Callback 1 doit être appelé
        linda.debug("Après write (testOneMatch)");
    }

    public static void testReadTakeOneMatch() {
        Linda linda = new CentralizedLinda();

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, new Tuple(500), t -> {
            System.out.println("Callback READ: " + t);
        });

        linda.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.FUTURE, new Tuple(501), t -> {
            System.out.println("Callback TAKE: " + t);
        });

        linda.debug("Avant write (testReadTakeOneMatch)");
        linda.write(new Tuple(500)); // Seul Callback READ doit être appelé
        linda.debug("Après write (testReadTakeOneMatch)");
    }

    public static void testImmediateDifferentMotifs() {
        Linda linda = new CentralizedLinda();

        linda.debug("Avant write (testImmediateDifferentMotifs)");
        linda.write(new Tuple(601)); // Seul Callback 2 doit être appelé
        linda.debug("Après write (testImmediateDifferentMotifs)");

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(600), t -> {
            System.out.println("Callback 1 (IMMEDIATE): " + t);
        });

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.IMMEDIATE, new Tuple(601), t -> {
            System.out.println("Callback 2 (IMMEDIATE): " + t);
        });

        
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== testCCW ===");
        testCCW();
        System.out.println("=== testCWC ===");
        testCWC();
        System.out.println("=== testWCC ===");
        testWCC();
        System.out.println("=== testCCW_TakeRead ===");
        testCCW_TakeRead();
        System.out.println("=== testCCW_Immediate ===");
        testCCW_Immediate();
        System.out.println("=== testCWC_Immediate ===");
        testCWC_Immediate();
        System.out.println("=== testWCC_TakeRead_Immediate ===");
        testWCC_TakeRead_Immediate();
        System.out.println("=== testWCC_Immediate ===");
        testWCC_Immediate();
        System.out.println("=== testTTW ===");
        testTTW();
        System.out.println("=== testTRW ===");
        testTRW();
        System.out.println("=== testRTW ===");
        testRTW();
        System.out.println("=== testWTT_Immediate ===");
        testWTT_Immediate();
        System.out.println("=== testNoMatch ===");
        testNoMatch();
        System.out.println("=== testOneMatch ===");
        testOneMatch();
        System.out.println("=== testReadTakeOneMatch ===");
        testReadTakeOneMatch();
        System.out.println("=== testImmediateDifferentMotifs ===");
        testImmediateDifferentMotifs();
    }

}


