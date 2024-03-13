public class StartServer {
    public static void main(String[] args) {
        int port = 69;

        GuiServer serv = new GuiServer(port);

        for (;;){serv.update();}
    }
}
