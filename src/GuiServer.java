import javax.swing.*;
import java.awt.*;

public class GuiServer {
    JFrame frame;

    JPanel panel;

    JLabel label;

    Server server;

    List<String> log;

    int maxLogLength = 30;

    public GuiServer(int port) {
        server = new Server(port) {
            @Override
            public void processNewConnection(String pClientIP, int pClientPort) {
                addToLog("[INFO] new connection (" + pClientIP + ":" + pClientPort + ")");
                sendToAll("[SERVER] New Friend: (" + pClientIP + ":" + pClientPort + ")");
            }

            @Override
            public void processMessage(String pClientIP, int pClientPort, String pMessage) {
                addToLog("[MSG] " + pClientIP + ": " + pMessage);
                sendToAll(pClientPort + ": " + pMessage);
            }

            @Override
            public void processClosingConnection(String pClientIP, int pClientPort) {
                addToLog("[WARNING] closing connection (" + pClientIP + ":" + pClientPort + ")");
            }
        };

        log = new List<String>();

        log.append("Server Initialisiert");

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

        while (this.log.hasAccess()) {
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
