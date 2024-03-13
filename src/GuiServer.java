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
                        while (connections.getContent() != null) {
                            if (connections.getContent().port == pClientPort) {
                                connections.getContent().currentUser.name = msg;
                                send(pClientIP,pClientPort,"[SERVER] changed name to " + msg);
                                break;
                            }
                            connections.next();
                        }
                        send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list");
                    }
                    case "PSWRD" -> {
                        connections.toFirst();
                        while (connections.getContent() != null) {
                            if (connections.getContent().port == pClientPort) {
                                if (connections.getContent().currentUser.password == "msg") {
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
                        while (connections.getContent() != null) {
                            if (connections.getContent().port == pClientPort) {
                                connections.getContent().currentUser.password = msg;
                                send(pClientIP,pClientPort,"[SERVER] updated your password.");
                                break;
                            }
                            connections.next();
                        }
                        send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list.");
                    }
                    case "LOGIN", "REG" -> {
                        boolean userExists = false;
                        String name = msg.split(" ")[0];
                        String passwd = msg.split(" ")[1];

                        // look if user already exists
                        connections.toFirst();
                        while (userList.getContent() != null) {
                            if (userList.getContent().name == name) {
                                userExists = true;
                                break;
                            }
                            userList.next();
                        }

                        // if user already exists
                        if (userExists) {
                            send(pClientIP, pClientPort, "[SERVER] user was found. Begin login ...");

                            // password is correct as well
                            if (userList.getContent().password == passwd) {

                                boolean connectionInUse = false;
                                connections.toFirst();
                                while (connections.getContent() != null) {
                                    if (connections.getContent().currentUser == userList.getContent()) {
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
                                    connections.append(new OpenConnection(pClientIP, pClientPort, userList.getContent()));
                                    connections.toLast();
                                    send(pClientIP,pClientPort, "OK");
                                    send(pClientIP,pClientPort,"[SERVER] you are now " + connections.getContent().currentUser.name);
                                }
                            }

                            // password is not correct
                            else {
                                send(pClientIP, pClientPort, "[SERVER] wrong password sucker.");
                            }
                        }

                        // if user does not exist jet
                        else {
                            send(pClientIP,pClientPort,"[SERVER] couldn't find you in the user list.");
                            send(pClientIP, pClientPort, "[SERVER] creating new user");

                            userList.append(new User(name, passwd));
                            userList.toLast();
                            connections.append(new OpenConnection(pClientIP, pClientPort, userList.getContent()));
                            connections.toLast();
                            send(pClientIP,pClientPort, "OK");
                            send(pClientIP,pClientPort,"[SERVER] you are now " + connections.getContent().currentUser.name);
                        }
                    }
                    case "MSG" -> {
                        String recName = msg.split(" ")[0];

                        String trueMessage = "";
                        if (msg.length() > recName.length()) {
                            trueMessage = pMessage.strip().substring(command.length() + 1);
                        }
                        boolean foundUser = false;
                        connections.toFirst();
                        while (connections.getContent() != null) {
                            if (connections.getContent().currentUser.name == recName) {
                                foundUser = true;
                                break;
                            }
                            connections.next();
                        }
                        if (foundUser) {
                            String recIp = connections.getContent().ip;
                            int recPort = connections.getContent().port;
                            String senderName = "Random";

                            //find name of sender
                            connections.toFirst();
                            while (connections.getContent() != null) {
                                if (connections.getContent().port == pClientPort) {
                                    senderName = connections.getContent().currentUser.name;
                                    break;
                                }
                                connections.next();
                            }

                            send(connections.getContent().ip, connections.getContent().port, senderName + ": " + trueMessage);
                        } else {
                            send(pClientIP,pClientPort, "[SERVER] coundn't find user in active connections");
                        }

                    }
                    default -> {
                        connections.toFirst();
                        while (connections.getContent() != null) {
                            if (connections.getContent().port == pClientPort) {
                                sendToAll(connections.getContent().currentUser.name + ": " + pMessage);
                                break;
                            }
                        }
                        addToLog("[ERROR] coulnd't find user who sended something in connection list.");
                    }
                }
            }

            @Override
            public void processClosingConnection(String pClientIP, int pClientPort) {
                addToLog("[WARNING] closing connection (" + pClientIP + ":" + pClientPort + ")");
            }
        };

        log = new List<String>();

        log.append("hi");

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

    public void update() {
        log.toFirst();
        String text = "";
        while (log.hasAccess()) {
            text = text + log.getContent() + "<br>";
            log.next();
        }
        this.label.setText("<html>" + text + "</html>");
    }

    public void addToLog(String text) {
        int logLength = 0;
        this.log.toFirst();

        while (this.log.current != null) {
            logLength++;
            this.log.next();
        }

        System.out.println(logLength);

        if (logLength >= maxLogLength) {
            this.log.toFirst();
            this.log.next();
            this.log.first = this.log.current;
        }
        this.log.append(text);
    }
}
