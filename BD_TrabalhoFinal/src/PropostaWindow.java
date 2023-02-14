import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PropostaWindow extends JDialog {
    private JComboBox<String> pedidosBox;
    private JTextField moradaField;
    private JComboBox<String> materiaisBox;
    private JTextField quantidadeField;
    private JButton adicionarButton;
    private JLabel propostaLabel;
    private JTextArea propostaArea;
    private JLabel pedidosLabel;
    private JLabel moradaLabel;
    private JLabel mao_obrafield;
    private JPanel propostaPanel;
    private JButton OKButton;
    private JTextField horasField;
    private JTextField totalField;
    private JTextField dataField;
    private Connection connection;
    private MainApp app;
    private boolean prof_IndepOrEmp;
    private User user;
    private boolean started;
    private int num_pedido;
    private int mao_obra;

    public PropostaWindow(Connection connection, MainApp app, User user) {
        this.app = app;
        this.connection = connection;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(propostaPanel);
        setMinimumSize(new Dimension(550, 650));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        addActionListeners();
        app.setEnabled(false);
        setPedidosBox();
        setMateriaisBox();
        app.setEnabled(true);
    }

    private void addActionListeners() {
        pedidosBox.addActionListener(this::setMoradaField);
        adicionarButton.addActionListener(this::addItem);
        OKButton.addActionListener(this::setOKButton);
    }

    private void setPedidosBox() {
        String query = "SELECT CONCAT('Pedido ', id_pedido, ': ', descricao) FROM pedido WHERE id_pedido NOT IN (SELECT id_pedido FROM aceita_prop);";
        QueryUtils.setComboBoxes(pedidosBox, query, connection);
    }

    private void setMateriaisBox() {
        String query = "SELECT nome FROM material";
        QueryUtils.setComboBoxes(materiaisBox, query, connection);
    }

    private void setMoradaField(ActionEvent e) {
        num_pedido = Integer.parseInt(pedidosBox.getSelectedItem().toString().replaceAll("[^0-9]", ""));
        String query = "SELECT morada_intervencao, data_pedido FROM pedido WHERE id_pedido = " + num_pedido;
        try {
            ResultSet rs = QueryUtils.executeRS(connection, query);
            rs.next();
            moradaField.setText(rs.getString(1));
            dataField.setText(rs.getString(2));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "setMoradaField(): Nao foi possivel mostrar a morada");
        }
    }

    private void startProposta() {

        String type = profType(user);
        int horas;
        String query = null;
        String view = null;

        if (!horasField.getText().isEmpty()) {
            horas = Integer.parseInt(horasField.getText());

            query = String.format("INSERT INTO proposta(id_profissional, mao_obra, horas_trabalho, id_pedido) VALUES" +
                            "((SELECT id from vTodosprofis WHERE nif = '%s'), (SELECT valor_hora FROM vTodosprofis WHERE nif = '%s'), %d, %d);",
                    user.getNIF(), user.getNIF(), horas, num_pedido);
            try {
                QueryUtils.execSQL(connection, "START TRANSACTION;");
                QueryUtils.execSQL(connection, query);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void addItem(ActionEvent e) {
        String query = null;
        double total = 0;
        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

        try {
            if (!started) {
                if (propostaReady()) {
                    startProposta();
                    started = true;
                    horasField.setEnabled(false);
                    int horas = Integer.parseInt(horasField.getText().toString());
                    total += getMaoObra(user);
                    propostaArea.append(String.format("Mao Obra: %.2f euros\n", total * horas));
                    totalField.setText(String.format("%.2f", total * horas)+ " euros");
                }
            }
            if (!quantidadeField.getText().isEmpty()) {
                int quantidade = Integer.parseInt(quantidadeField.getText());
                String material = materiaisBox.getSelectedItem().toString();
                pedidosBox.setEnabled(false);

                query = String.format("INSERT INTO linha_proposta (id_proposta, cod_material, quantidade) VALUES (" +
                                "(SELECT id_proposta FROM proposta ORDER BY 1 DESC LIMIT 1), (SELECT cod_material FROM material WHERE nome = '%s'), %d)",
                        material, quantidade);

                total = format.parse(totalField.getText().toString()).doubleValue();
                total += getPreco(material) * quantidade;

                try{
                    QueryUtils.execSQL(connection, query);
                    propostaArea.append(String.format("%dx %s: %.2f euros\n", quantidade, material, getPreco(material) * quantidade));
                    totalField.setText(String.format("%.2f", total) + " euros");
                } catch(SQLException ex){
                    JOptionPane.showMessageDialog(this, "addItem(): Nao adicionar produtos repetidos");
                }

            } else {
                JOptionPane.showMessageDialog(this, "Inserir a quantidade do material");
            }
        } catch (ParseException ex){
            JOptionPane.showMessageDialog(this, "Parse Exception");
        }

    }

    private String profType(User user) {
        String query = String.format("SELECT tipo_profis FROM profissional INNER JOIN utilizador u ON u.id_utilizador = id_profissional " +
                "WHERE u.username = '%s';", user.getUser());
        String type = null;
        try {
            ResultSet rs = QueryUtils.executeRS(connection, query);
            rs.next();
            type = rs.getString(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return type;
    }

    private void setOKButton(ActionEvent e){
        try{
            QueryUtils.execSQL(connection, "COMMIT;");
            JOptionPane.showMessageDialog(this, "Proposta feita com sucesso");
            dispose();
        }catch (SQLException ex){
            JOptionPane.showMessageDialog(this, "setOkButton(): Erro a introduzir proposta");
        }
    }

    private boolean propostaReady() {
        return !horasField.getText().isEmpty() && !quantidadeField.getText().isEmpty();
    }

    private double getPreco(String material){
        double preco = 0;
        try{
            String query = String.format("SELECT preco_unitario * (1+taxa_iva) FROM material WHERE nome = '%s'", material);
            ResultSet rs = QueryUtils.executeRS(connection, query);
            rs.next();
            preco = rs.getDouble(1);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return preco;
    }

    private double getMaoObra(User user){
        String query = String.format("SELECT valor_hora FROM vTodosprofis WHERE nif = '%s';", user.getNIF());
        ResultSet rs = QueryUtils.executeRS(connection, query);
        double mao_obra = 0;
        try{
            rs.next();
            mao_obra = rs.getDouble(1);
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return mao_obra;
    }
}
