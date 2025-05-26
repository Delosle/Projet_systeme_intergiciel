import linda.*;

public class TestVide {
    public static void main(String[] a) throws Exception {
        final Linda linda = new linda.shm.CentralizedLinda();
        linda.write(new Tuple(4, 5));
        System.out.println("");
        System.out.println("Ok, I have found Linda.");
    }
}

