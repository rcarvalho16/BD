import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class listPropostasWindow extends JDialog{
    private JPanel listPanel;
    private JComboBox<String> pedidosBox;
    private JComboBox<String> profissionalBox;
    private JTextArea linhasArea;
    private JTextField subtotalField;
    private JButton aceitarButton;
    private JButton recusarButton;
    private Connection connection;
    private MainApp app;
    private User user;
    private int id_pedido;
    private int id_proposta;

    public listPropostasWindow(Connection connection, MainApp app, User user){
        this.connection = connection;
        this.app = app;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(listPanel);
        setMinimumSize(new Dimension(650, 550));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        addActionListeners();
        setPedidosBox();
    }


    private void setPedidosBox(){
        int id = User.getIDUtilizador(user.getNIF(), connection);

        if(id!=0){
            String query = String.format("SELECT CONCAT('Pedido ', id_pedido, ': ', descricao) FROM pedido WHERE id_cliente = %d \n" +
                    "AND id_pedido NOT IN (SELECT id_pedido FROM aceita_prop);", id);
            QueryUtils.setComboBoxes(pedidosBox, query, connection);
        }

    }


    private void setProfissionalBox(ActionEvent e){
        linhasArea.setText("");
        id_pedido = Integer.parseInt(pedidosBox.getSelectedItem().toString().replaceAll("[^0-9]", ""));
        String query = String.format("SELECT CONCAT('Proposta ', id_proposta, ': ', vt.nome) FROM vTodosprofis vt INNER JOIN proposta p ON vt.id = p.id_profissional WHERE id_pedido = %d;", id_pedido);
        QueryUtils.setComboBoxes(profissionalBox, query, connection);
    }

    private void setLinhasArea(ActionEvent e){
        linhasArea.setText("");
        id_proposta = Integer.parseInt(profissionalBox.getSelectedItem().toString().replaceAll("[^0-9]", ""));
        int quantidade;
        double mao_obra = 0, preco_unitario, taxa_iva, preco;
        int horas = 0;
        double total = 0;
        String nome, simbolo;
        try{
            String query = "SELECT mao_obra, horas_trabalho FROM proposta WHERE id_proposta = " + id_proposta;
            ResultSet rs = QueryUtils.executeRS(connection, query);
            rs.next();
            mao_obra = rs.getDouble(1);
            horas = rs.getInt(2);
            total = mao_obra * horas;
            linhasArea.append(String.format("Mao de obra: %.2f euros\n", total));

            query = String.format("SELECT nome, simbolo, quantidade, preco_unitario, taxa_iva FROM material m \n" +
                    "INNER JOIN linha_proposta lp ON m.cod_material = lp.cod_material \n" +
                    "INNER JOIN proposta p ON p.id_proposta = lp.id_proposta \n" +
                    "WHERE p.id_proposta = %d;", id_proposta);

            rs = QueryUtils.executeRS(connection, query);

            while(rs.next()){
                nome = rs.getString(1);
                simbolo = rs.getString(2);
                quantidade = rs.getInt(3);
                preco_unitario = rs.getDouble(4);
                taxa_iva = rs.getDouble(5);
                preco = quantidade * preco_unitario * (1+taxa_iva);
                total += preco;
                linhasArea.append(String.format("%s %s x%d: %.2f euros\n", nome, simbolo, quantidade, preco));
            }

            subtotalField.setText(String.format("%.2f euros", total));

        } catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    private void setAceitarButton(ActionEvent e){
        String query = String.format("INSERT INTO aceita_prop VALUES (%d, %d, %d, current_date());", id_proposta, id_pedido, User.getIDUtilizador(user.getNIF(), connection ));
        try{
            QueryUtils.execSQL(connection, query);
            JOptionPane.showMessageDialog(this, "Pedido aceite");
            dispose();
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Nao foi possivel aceitar o pedido");
        }

    }


    private void setRecusarButton(ActionEvent e){
        String query = String.format("DELETE FROM proposta WHERE id_proposta = %d", id_proposta);
        profissionalBox.removeItemAt(profissionalBox.getSelectedIndex());
        try{
            QueryUtils.execSQL(connection, query);
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Nao foi possivel apagar a proposta");
        }
    }

    private void addActionListeners(){
        pedidosBox.addActionListener(this::setProfissionalBox);
        profissionalBox.addActionListener(this::setLinhasArea);
        aceitarButton.addActionListener(this::setAceitarButton);
        recusarButton.addActionListener(this::setRecusarButton);
    }

}
