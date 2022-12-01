package projector;

public class Main {

    public static void main(String[] args) {
        try {
            System.setProperty("java.net.preferIPv4Stack", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainDesktop.main(args);
    }
}
