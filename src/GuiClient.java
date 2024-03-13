import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GuiClient {

    JFrame frame;

    JPanel panel;

    JLabel label;

    JTextField textField;

    Client client;

    List<String> log;

    int maxLogLength = 10;

    String name;

    public GuiClient(String ip, int port) {

        name = "";

        log = new List<>();

        frame = new JFrame("Niels Chat Client");
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();
        textField = new JTextField(100);

        panel.add(label);
        panel.add(textField);
        frame.add(panel);
        update();
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        client = new Client(ip, port) {
            @Override
            public void processMessage(String pMessage) {
                addToLog(pMessage);
            }
        };

        client.send("hi");

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textField.getText();
                if (name.isEmpty()) {
                    name = text;
                    addToLog("neuer Name: " + name);
                    send("/HELO " + text);
                } else {
                    addToLog(text);
                    send(text);
                }
                textField.setText("");
            }
        });

        addToLog("Setze Name:");

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

        if (logLength > maxLogLength) {
            this.log.toFirst();
            this.log.next();
            this.log.first = this.log.current;
        }
        log.append(text);

        update();
    }

    public void send(String text) {
        this.client.send(text);
    }
}
