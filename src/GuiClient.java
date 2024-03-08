import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GuiClient {

    JFrame frame;

    JPanel panel;

    JLabel label;

    JTextField textField;

    Client client;

    List<String> log;

    String eingabe;

    int maxLogLength = 25;

    String name = "";

    public GuiClient(String ip, int port) {
        client = new Client(ip, port) {
            @Override
            public void processMessage(String pMessage) {
                addToLog(pMessage);
            }
        };

        log = new List<String>();

        frame = new JFrame("Niels Chat Client");
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();
        textField = new JTextField(100);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (name == "") {
                    name = textField.getText().strip();
                    if (validName(name)) {
                        client.send("[HELO] name");
                    }
                    textField.setText("");
                } else {
                    eingabe = textField.getText().strip();
                    if (eingabe.charAt(0) == '/') {
                        String command = eingabe.split(" ")[0].toUpperCase().strip().substring(1);
                        String message = Arrays.stream(eingabe.split("\\s+")).skip(1).collect(Collectors.joining(" "));
                        switch (command) {
                            case "MSG":
                                client.send("[MSG] " + message);
                            case "REG":
                                if (validName(message)){
                                    client.send("[REG] " + message);
                                }
                        }

                    } else {
                        client.send(eingabe);
                    }
                    textField.setText("");
                }
            }
        });

        log.append("Setze Name:");

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

    private boolean validName(String name) {

        if (name.length() > 20) {
            name = "";
            log.append("Name zu lang. Höchstens 20 Zeichen. Nochmal bitte:");
            return false;
        } else if (name.length() < 2) {
            name = "";
            log.append("Name zu kurz. Mindestens 2 Zeichen. Nochmal bitte:");
            return false;
        }
        return true;
    }
}
