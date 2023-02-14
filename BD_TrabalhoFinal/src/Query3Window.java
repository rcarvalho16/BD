import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query3Window extends JFrame {
    private JComboBox<String> concelhoBox;
    private JScrollPane scrollPane;
    private JTable outputTable;
    private JButton OKButton;
    private JComboBox<String> competenciaBox;
    private JPanel query3Panel;
    private final Connection connection;
    private final MainApp app;

    Query3Window(Connection connection, MainApp app){
        this.app = app;
        this.connection = connection;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(query3Panel);
        setMinimumSize(new Dimension(1440, 720));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        setConcelhoBox();
        setCompetenciaBox();
        OKButton.addActionListener(this::setOKButton);

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

    private void setOKButton(ActionEvent e){
        String query = String.format("SELECT p.id_profissional AS \"ID\", u.nome Nome, u.Apelido, COUNT(pr.id_proposta) as \"Nº propostas\", p.valor_hora, c.desc_concelho as \"Concelho de atuação\" from prof_indep p\n" +
                "INNER JOIN utilizador u ON p.id_profissional = u.id_utilizador\n" +
                "INNER JOIN proposta pr ON pr.id_profissional = p.id_profissional\n" +
                "INNER JOIN prof_comp pc ON p.id_profissional = pc.id_profissional \n" +
                "INNER JOIN competencia c2 ON pc.cod_competencia = c2.cod_competencia \n" +
                "INNER JOIN concelho c  ON (u.cod_distrito = c.cod_distrito AND u.cod_concelho  = c.cod_concelho)\n" +
                "WHERE c.desc_concelho = '%s' AND c2.designacao = '%s'\n" +
                "GROUP BY pr.id_profissional \n" +
                "ORDER BY 4 DESC LIMIT 3;", concelhoBox.getSelectedItem().toString(), competenciaBox.getSelectedItem().toString());

        scrollPane.setViewportView(outputTable);
        try{
            ResultSet rs = QueryUtils.executeRS(connection, query);
            QueryUtils.printTable(rs, outputTable);
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "Q3 setOKButton(): Erro a representar dados");
        }

    }
}
