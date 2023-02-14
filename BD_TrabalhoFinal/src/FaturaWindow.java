import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FaturaWindow extends JDialog {
    private JComboBox<String> trabalhosBox;
    private JButton emitirButton;
    private JPanel faturaPanel;
    private JTable linhasTable;
    private JScrollPane linhasPanel;
    private Connection connection;
    private MainApp app;
    private User user;
    private int id_user;
    private int id_proposta;

    FaturaWindow(Connection connection, MainApp app, User user) {
        this.connection = connection;
        this.app = app;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(faturaPanel);
        setMinimumSize(new Dimension(960, 720));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        setTrabalhosBox();
        addActionListeners();
        linhasPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        linhasPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    }


    private void setTrabalhosBox() {
        id_user = User.getIDUtilizador(user.getNIF(), connection);
        if(checkTrabalhos()) {
            String query = String.format("SELECT CONCAT('Proposta ',p.id_proposta, ': ' ,p2.descricao) as descricao FROM proposta p \n" +
                    "INNER JOIN pedido p2 ON p.id_pedido = p2.id_pedido\n" +
                    "INNER JOIN aceita_prop ap ON p.id_proposta = ap.id_proposta \n" +
                    "WHERE p.id_profissional = %d AND p.id_proposta NOT IN (SELECT id_proposta FROM fatura);", id_user);

            QueryUtils.setComboBoxes(trabalhosBox, query, connection);
            id_proposta = Integer.parseInt(trabalhosBox.getSelectedItem().toString().replaceAll("[^0-9]", ""));
        }
    }

    private boolean checkTrabalhos(){
        String query = String.format("SELECT * FROM aceita_prop ap INNER JOIN proposta p ON ap.id_pedido = p.id_pedido \n" +
                "WHERE p.id_profissional = %d AND ap.id_proposta NOT IN (SELECT id_proposta FROM fatura f2);", id_user);
        ResultSet rs = QueryUtils.executeRS(connection, query);
        try{
            if(rs.next()) return true;
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Nao efetuou qualquer trabalho");
        }
        return false;
    }

    private void setLinhasArea(ActionEvent e) {
        int id_proposta = Integer.parseInt(trabalhosBox.getSelectedItem().toString().replaceAll("[^0-9]", ""));
        String[] columnNames = {"Nome", "Quantidade", "Preco Unitario", "Taxa Iva", "Subtotal"};

        try {
            String query = String.format("SELECT nome, simbolo, quantidade, preco_unitario, taxa_iva, ROUND(quantidade * preco_unitario * (1+taxa_iva),2) as Total FROM material m \n" +
                    "INNER JOIN linha_proposta lp ON m.cod_material = lp.cod_material \n" +
                    "INNER JOIN proposta p ON p.id_proposta = lp.id_proposta \n" +
                    "WHERE p.id_proposta = %d\n" +
                    "UNION\n" +
                    "SELECT 'Mao de Obra','h', horas_trabalho, mao_obra,'', ROUND(mao_obra * horas_trabalho,2) FROM proposta WHERE id_proposta = %d;", id_proposta, id_proposta);

            ResultSet rs = QueryUtils.executeRS(connection, query);
            QueryUtils.printTable(rs, linhasTable);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void setEmitirButton(ActionEvent e){
        String query = String.format("INSERT INTO fatura (id_proposta, data_emissao) VALUES ('%d', current_date());", id_proposta);
        try{
            QueryUtils.execSQL(connection, query);
            JOptionPane.showMessageDialog(this, "Fatura emitida com sucesso");
            dispose();
        } catch (SQLException ex){
            JOptionPane.showMessageDialog(this, "Nao foi possivel emitir a fatura");
        }
    }

    private void addActionListeners(){
        trabalhosBox.addActionListener(this::setLinhasArea);
        emitirButton.addActionListener(this::setEmitirButton);
    }
}

