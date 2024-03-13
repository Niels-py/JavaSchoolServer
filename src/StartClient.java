public class StartClient {
    public static void main(String[] args) {
        int port = 69;
        String serverIP = "127.0.0.1";

        GuiClient clen = new GuiClient(serverIP, port);

        for (;;){ clen.update();}
    }
}