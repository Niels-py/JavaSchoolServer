import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GuiClient {

    JFrame frame;

    JPanel panel;

    JLabel label;

    JTextField textField;

    Client client;

    List<String> log;

    String eingabe;

    int maxLogLength = 25;

    public GuiClient(String ip, int port) {
        client = new Client(ip, port) {
            @Override
            public void processMessage(String pMessage) {
                addToLog(pMessage);
            }
        };

        log = new List<String>();

        frame = new JFrame("Niels und Julius Chat");
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();
        textField = new JTextField(100);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eingabe = textField.getText();
                client.send(eingabe);
                textField.setText("");
            }
        });

        panel.add(label);
        panel.add(textField);
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
