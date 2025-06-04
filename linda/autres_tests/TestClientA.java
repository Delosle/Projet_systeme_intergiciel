package linda.autres_tests;

import linda.*;
import linda.server.LindaClient;

public class TestClientA {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java linda.test.TestClientA //localhost:4000/LindaServer");
            return;
        }

        Linda linda = new LindaClient(args[0]);

        Tuple template = new Tuple("event", Integer.class);

        linda.eventRegister(Linda.eventMode.READ, Linda.eventTiming.FUTURE, template, new Callback() {
            public void call(Tuple t) {
                System.out.println("✅ CALLBACK reçu côté Client A : " + t);
            }
        });

        System.out.println("Client A prêt, en attente d’un tuple qui matche : " + template);
    }
}
