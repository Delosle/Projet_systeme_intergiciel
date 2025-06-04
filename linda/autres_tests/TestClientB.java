package linda.autres_tests;

import linda.*;
import linda.server.LindaClient;


public class TestClientB {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java linda.test.TestClientB //localhost:4000/LindaServer");
            return;
        }

        Linda linda = new LindaClient(args[0]);

        Tuple tuple = new Tuple("event", 123);
        linda.write(tuple);

        System.out.println("Client B a écrit le tuple : " + tuple);
    }
}
