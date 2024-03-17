import javax.swing.*;
import java.awt.*;

public class GuiServer {
    JFrame frame;

    JPanel panel;

    JLabel label;

    Server server;

    List<String> log;

    List<OpenConnection> connections;

    List<User> userList;

    int maxLogLength = 20;

    public GuiServer(int port) {

        connections = new List<>();
        userList = new List<>();

        server = new Server(port) {
            @Override
            public void processNewConnection(String pClientIP, int pClientPort) {
                connections.append(new OpenConnection(pClientIP, pClientPort, -1));
                addToLog("[INFO] new connection (" + pClientIP + ":" + pClientPort + ")");
            }

            @Override
            public void processMessage(String pClientIP, int pClientPort, String pMessage) {
                pMessage = pMessage.strip();
                String command = pMessage.strip().split(" ")[0];
                String msg = "";
                if (pMessage.length() > command.length()) {
                    msg = pMessage.strip().substring(command.length() + 1);
                }

                addToLog("command: " + command);
                addToLog("message: " + msg);

                switch (command) {
                    case "CHANGENAME" -> {
                        connections.toFirst();
                        while (connections.hasAccess()) {
                            if (connections.getContent().port == pClientPort) {
                                getUser(connections.getContent().userIndex).name = msg;
                                send(pClientIP,pClientPort,"[SERVER] changed name to " + msg);
                                break;
                            }
                            connections.next();
                        }
                        // send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list");
                    }
                    case "PSWRD" -> {
                        connections.toFirst();
                        while (connections.hasAccess()) {
                            if (connections.getContent().port == pClientPort) {
                                if (getUser(connections.getContent().userIndex).password.equals("msg")) {
                                    send(pClientIP,pClientPort, "[SERVER] that was the correct password.");
                                } else {
                                    send(pClientIP,pClientPort, "[SERVER] that was not the correct password.");
                                }
                                break;
                            }
                            connections.next();
                        }
                        send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list.");
                    }
                    case "CHANGEPSWRD" -> {
                        connections.toFirst();
                        while (connections.hasAccess()) {
                            if (connections.getContent().port == pClientPort) {
                                getUser(connections.getContent().userIndex).password = msg;
                                send(pClientIP,pClientPort,"[SERVER] updated your password.");
                                break;
                            }
                            connections.next();
                        }
                        send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list.");
                    }
                    case "LOGIN", "REG", "HELO" -> {
                        String name = msg.split(" ")[0];
                        String passwd = msg.split(" ")[1];

                        // look if user already exists
                        int userIndex = getUserIndexByName(name);

                        // if user already exists
                        if (userIndex > -1) {
                            User user = getUser(userIndex);
                            send(pClientIP, pClientPort, "[SERVER] user was found. Begin login ...");

                            // password is correct as well
                            if (user.password.equals(passwd)) {

                                send(pClientIP, pClientPort, "[SERVER] password is correct");

                                boolean connectionInUse = false;

                                connections.toFirst();
                                while (connections.hasAccess()) {
                                    if (connections.getContent().userIndex == userIndex) {
                                        connectionInUse = true;
                                        break;
                                    }
                                    connections.next();
                                }

                                // user is in use already
                                if (connectionInUse) {
                                    send(pClientIP, pClientPort, "[SERVER] user already in use by someone.");
                                }

                                // user is not in use
                                else {
                                    connections.toFirst();
                                    while (connections.hasAccess()) {
                                        if (connections.getContent().ip.equals(pClientIP) && connections.getContent().port == pClientPort) {
                                            connections.getContent().userIndex = userIndex;
                                            break;
                                        }
                                        connections.next();
                                    }
                                    send(pClientIP,pClientPort, "OK");
                                    send(pClientIP,pClientPort,"[SERVER] you are now " + getUser(userIndex).name);
                                }
                            }

                            // password is not correct
                            else {
                                send(pClientIP, pClientPort, "[SERVER] wrong password sucker.");
                            }
                        }

                        // if user does not exist yet
                        else {
                            send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list.");
                            send(pClientIP, pClientPort, "[SERVER] creating new user");

                            userList.append(new User(name, passwd));
                            addToLog("[INFO] new User: " + name + ", " + passwd);

                            connections.toFirst();
                            while (connections.hasAccess()) {
                                // addToLog("con: " + connections.getContent().ip + ":" + connections.getContent().port);
                                // addToLog("current con: " + pClientIP + ":" + pClientPort);
                                if (connections.getContent().ip.equals(pClientIP) && connections.getContent().port == pClientPort) {

                                    // get new user Index, so basically the length of the userList
                                    // we can do it like this because userIndex must be -1 and it at least has the length
                                    // of one so the userIndex is always correct, I guess
                                    userList.toFirst();
                                    while (userList.hasAccess()) {
                                        userIndex++;
                                        userList.next();
                                    }
                                    connections.getContent().userIndex = userIndex;
                                    send(pClientIP, pClientPort, "OK");
                                    send(pClientIP,pClientPort,"[SERVER] you are now " + getUser(userIndex).name);
                                    return;
                                }
                                connections.next();
                            }
                            addToLog("something went horribly wrong!");
                        }
                    }
                    case "MSG" -> {
                        String recName = msg.split(" ")[0];

                        String trueMessage = "";
                        if (msg.length() > recName.length()) {
                            trueMessage = pMessage.strip().substring(command.length() + 1);
                        }
                        int recUserIndex = getUserIndexByName(recName);
                        boolean foundUser = false;

                        // find name in active connections
                        if (recUserIndex != -1) {
                            connections.toFirst();
                            while (connections.hasAccess()) {
                                if (connections.getContent().userIndex == recUserIndex) {
                                    foundUser = true;
                                    break;
                                }
                                connections.next();
                            }
                        }

                        // if name is connected to server in some way
                        if (foundUser) {
                            String senderName = "[ERROR - couldn't find name]";

                            //find name of sender
                            connections.toFirst();
                            while (connections.hasAccess()) {
                                if (connections.getContent().port == pClientPort) {
                                    senderName = getUser(connections.getContent().userIndex).name;
                                    break;
                                }
                                connections.next();
                            }

                            send(connections.getContent().ip, connections.getContent().port, senderName + ": " + trueMessage);
                            addToLog("[MSG] " + senderName + ": " + trueMessage);
                        } else {
                            send(pClientIP,pClientPort, "[SERVER] coundn't find user in active connections");
                            addToLog("[INFO] invalid name on MSG request");
                        }

                    }
                    default -> {
                        addToLog("[INFO] Default");
                        boolean found = false;

                        connections.toFirst();
                        while (connections.hasAccess()) {
                            if (connections.getContent().port == pClientPort) {
                                found = true;
                                break;
                            }
                            connections.next();
                        }
                        if (!found) {
                            addToLog("[ERROR] couldn't find user who sended something in connection list.");
                        }
                        else {
                            sendToAll(getUser(connections.getContent().userIndex).name + ": " + pMessage);
                            addToLog("[INFO] sending text: " + pMessage);
                        }
                    }
                }
            }

            @Override
            public void processClosingConnection(String pClientIP, int pClientPort) {
                addToLog("[WARNING] closing connection (" + pClientIP + ":" + pClientPort + ")");
            }
        };

        log = new List<>();

        log.append("\u2764 Server Init");

        frame = new JFrame("Niels Chat Server");
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();

        panel.add(label);
        frame.add(panel);
        update();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public User getUser(int index) {
        this.userList.toFirst();
        for (int i = 0; i < index; i++) {
            this.userList.next();
        }
        return userList.getContent();
    }

    public int getUserIndexByName(String pName) {
        int index = 0;
        userList.toFirst();
        while (userList.hasAccess()) {
            if (userList.getContent().name.equals(pName)) {
                return index;
            }
            index++;
            userList.next();
        }
        return -1;
    }

    public void update() {
        String text = "";

        log.toFirst();
        while (log.hasAccess()) {
            text = text + log.getContent() + "<br>";
            log.next();
        }
        this.label.setText("<html>" + text + "</html>");
    }

    public void addToLog(String text) {
        int logLength = 0;

        this.log.toFirst();
        while (this.log.hasAccess()) {
            logLength++;
            log.next();
        }

        if (logLength >= maxLogLength) {
            this.log.toFirst();
            this.log.next();
            this.log.first = this.log.current;
        }
        this.log.append(text);
    }
}
