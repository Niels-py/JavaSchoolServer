public class ClientData {

    public String ip;

    public int port;

    public String name;

    public String password;

    public ClientData(String pip, int pport, String pname) {
        this.ip = pip;
        this.port = pport;
        this.name = pname;
        this.password = "";
    }
}
