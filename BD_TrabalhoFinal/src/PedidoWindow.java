import javax.swing.*;
import java.awt.*;

public class PedidoWindow extends JFrame{
    private JLabel moradaLabel;
    private JTextField moradaField;
    private JLabel descricaoLabel;
    private JPanel pedidoPanel;
    private JTextArea descricaoArea;

    PedidoWindow (){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(pedidoPanel);
        setMinimumSize(new Dimension(450, 450));
    }
    private void createUIComponents() {
        descricaoArea = new JTextArea();
        descricaoArea.setPreferredSize(new Dimension(350, 200));
        descricaoArea.setLineWrap(true);
        descricaoArea.setBorder(BorderFactory.createLineBorder(Color.black));
        moradaField = new JTextField();
        moradaField.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    public JPanel getPedidoPanel() {
        return pedidoPanel;
    }

    public String getMoradaField() {
        return moradaField.getText().toString();
    }

    public String getDescricaoField() {
        return descricaoArea.getText().toString();
    }
}
