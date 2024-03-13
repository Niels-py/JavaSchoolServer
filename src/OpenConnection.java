public class OpenConnection {
    public String ip;

    public int port;

    public User currentUser;

    public OpenConnection(String pIp, int pPort, User pUser){
        this.ip = pIp;
        this.port = pPort;
        this.currentUser = pUser;
    }
}
