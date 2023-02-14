import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query1Window extends JFrame {
    private JComboBox<String> concelhoBox;
    private JComboBox<String> freguesiaBox;
    private JButton concelhoButton;
    private JButton freguesiaButton;
    private JPanel query1Panel;
    private JTable outputTable;
    private JScrollPane scrollPane;
    private JComboBox<String> competenciaBox;
    private final Connection connection;
    private final MainApp app;

    public Query1Window(Connection connection, MainApp app) {
        this.app = app;
        this.connection = connection;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(query1Panel);
        setMinimumSize(new Dimension(1440, 720));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        setConcelhoBox();
        setCompetenciaBox();
        addActionListeners();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    }

    private void setConcelhoBox(){
        String query = "SELECT desc_concelho FROM concelho ORDER BY 1 ASC";
        QueryUtils.setComboBoxes(concelhoBox, query, connection);
    }
    private void setCompetenciaBox(){
        String query = "SELECT DISTINCT designacao FROM competencia ORDER BY 1 ASC";
        QueryUtils.setComboBoxes(competenciaBox, query, connection);
    }

    private void setFreguesiaBox(ActionEvent e){
        String query = String.format("\n" +
                "SELECT DISTINCT desc_freguesia FROM freguesia f \n" +
                "INNER JOIN concelho c \n" +
                "ON (f.cod_distrito = c.cod_distrito AND f.cod_concelho = c.cod_concelho)\n" +
                "WHERE c.desc_concelho = '%s' ORDER BY 1 ASC;", concelhoBox.getSelectedItem().toString());

        try{
            ResultSet rs = QueryUtils.executeRS(connection, query);
            while(rs.next()){
                freguesiaBox.addItem(rs.getString(1));
            }
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "setFreguesiaBox(): Nao foi possivel preencher a combo box das freguesias");
        }
    }

    private void setConcelhoButton(ActionEvent e){
        String query = String.format("SELECT p.id_profissional ID, u.nome NOME, u.apelido APELIDO, c.desc_concelho CONCELHO, c2.designacao DESIGNACAO FROM utilizador u \n" +
                "INNER JOIN profissional p ON u.id_utilizador = p.id_profissional \n" +
                "INNER JOIN concelho c ON (u.cod_concelho = c.cod_concelho AND u.cod_distrito = c.cod_distrito ) \n" +
                "INNER JOIN prof_comp pc ON pc.id_profissional = p.id_profissional \n" +
                "INNER JOIN competencia c2 ON pc.cod_competencia = c2.cod_competencia\n" +
                "WHERE c.desc_concelho  = '%s' AND c2.designacao = '%s';", concelhoBox.getSelectedItem().toString(), competenciaBox.getSelectedItem());

        scrollPane.setViewportView(outputTable);
        try{
            ResultSet rs = QueryUtils.executeRS(connection, query);
            QueryUtils.printTable(rs, outputTable);
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Q1 setConcelhoButton(): Erro a representar dados");
        }
    }

    private void setFreguesiaButton(ActionEvent e){
        String query = String.format("SELECT p.id_profissional ID, u.nome NOME, u.apelido APELIDO, f.desc_freguesia FREGUESIA , c2.designacao DESIGNACAO FROM utilizador u \n" +
                "INNER JOIN profissional p ON u.id_utilizador = p.id_profissional \n" +
                "INNER JOIN freguesia f ON (u.cod_concelho = f.cod_concelho AND u.cod_distrito = f.cod_distrito AND u.cod_freguesia = f.cod_freguesia) \n" +
                "INNER JOIN prof_comp pc ON pc.id_profissional = p.id_profissional \n" +
                "INNER JOIN competencia c2 ON pc.cod_competencia = c2.cod_competencia\n" +
                "WHERE f.desc_freguesia  = '%s' AND c2.designacao = '%s';", freguesiaBox.getSelectedItem().toString(), competenciaBox.getSelectedItem());

        scrollPane.setViewportView(outputTable);
        try{
            ResultSet rs = QueryUtils.executeRS(connection, query);
            QueryUtils.printTable(rs, outputTable);
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Q1 setFreguesiaButton(): Erro a representar dados");
        }
    }

    private void addActionListeners(){
        concelhoBox.addActionListener(this::setFreguesiaBox);
        concelhoButton.addActionListener(this::setConcelhoButton);
        freguesiaButton.addActionListener(this::setFreguesiaButton);
    }
}
