public class StartServer {
    public static void main(String[] args) {
        int port = 69;

        new GuiServer(port);

        for (;;){}
    }
}
