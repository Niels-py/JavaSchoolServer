import javax.swing.*;
import java.awt.*;

public class GuiServer {
    JFrame frame;

    JPanel panel;

    JLabel label;

    Server server;

    List<String> log;

    List<ClientData> clientDataList;

    int maxLogLength = 10;

    public GuiServer(int port) {

        clientDataList = new List<>();

        log = new List<>();

        frame = new JFrame("Niels Chat Server");
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();

        panel.add(label);
        frame.add(panel);
        update();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        server = new Server(port) {
            @Override
            public void processNewConnection(String pClientIP, int pClientPort) {

                clientDataList.append(new ClientData(
                        pClientIP,
                        pClientPort,
                        ""
                ));

                addToLog("[INFO] new connection (" + pClientIP + ":" + pClientPort + ")");
                sendToAll("[SERVER] New Friend: (" + pClientIP + ":" + pClientPort + ")");
            }

            @Override
            public void processMessage(String pClientIP, int pClientPort, String pMessage) {

                //debug
                addToLog("[DEBUG] " + pMessage);
                addToLog("[DEBUG] " + pClientPort);

                String command = pMessage.strip().split(" ")[0];

                addToLog("[DEBUG] \"" + command + "\"");

                String msg;
                try {
                    msg = pMessage.strip().substring(command.length() + 1);
                } catch (Exception ignored) {
                    msg = "";
                }
                addToLog("[DEBUG] \"" + msg + "\"");
                switch (command) {

                    case "/HELO" -> {

                        addToLog("[DEBUG] processing HELO");

                        if (validName(msg)) {
                            addToLog("[DEBUG] valid Name");
                            clientDataList.toFirst();
                            while (clientDataList.hasAccess()) {
                                if (clientDataList.getContent().port == port) {
                                    clientDataList.getContent().name = msg;
                                    addToLog("[DEBUG] added name to entry");
                                }
                            }
                            send(pClientIP, pClientPort, "Name wurde erfolgreich gesetzt.");
                        } else {
                            send(pClientIP, pClientPort, "Name ist nicht gut. Nochmal!");
                        }
                    }

                    case "/MSG" -> {

                        String name = msg.split(" ")[0];
                        ClientData cD;
                        cD = getClientByName(name);
                        if (cD == null) {
                            server.send(pClientIP, pClientPort, "Nutzer exestiert nicht!");
                        } else {
                            server.send(cD.ip, cD.port, "(privat) " + getClientByPort(pClientPort).name + ": " + msg.substring(name.length() + 1));
                        }

                    }

                    case "/QUIT" -> {
                        server.closeConnection(pClientIP, pClientPort);
                        addToLog("[INFO] " + getClientByPort(pClientPort).name + " hat sich abgemeldet.");
                    }

                    default -> {
                        String name = getClientByPort(pClientPort).name;
                        if (name.isEmpty()) {
                            addToLog("[MSG] " + pClientIP + ": " + pMessage);
                            server.sendToAll(pClientPort + ": " + pMessage);
                        } else {
                            addToLog("[MSG] " + name + ": " + pMessage);
                            server.sendToAll(name + ": " + pMessage);
                        }
                    }
                }
            }

            @Override
            public void processClosingConnection(String pClientIP, int pClientPort) {
                addToLog("[INFO] schließen der Verbindung von \"" + getClientByPort(pClientPort).name + "\" (" + pClientIP + ":" + pClientPort + ")");
            }
        };

        addToLog("Server Initialisiert");
        update();
    }

    public void update() {
        log.toFirst();
        StringBuilder text = new StringBuilder();
        while (log.hasAccess()) {
            text.append(log.getContent()).append("<br>");
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

        if (logLength >= maxLogLength) {
            this.log.toFirst();
            this.log.next();
            this.log.first = this.log.current;
        }
        this.log.append(text);

        update();
    }

    public ClientData getClientByPort(int port) {
        clientDataList.toFirst();
        while (clientDataList.hasAccess()) {
            if (clientDataList.getContent().port == port) {
                return clientDataList.getContent();
            }
        }
        return null;
    }

    public ClientData getClientByName(String name) {
        clientDataList.toFirst();
        while (clientDataList.hasAccess()) {
            if (clientDataList.getContent().name.equals(name)) {
                return clientDataList.getContent();
            }
        }
        return null;
    }

    private boolean validName(String name) {
        return name.length() <= 20 && name.length() >= 2 && !name.contains(" ");
    }
}

