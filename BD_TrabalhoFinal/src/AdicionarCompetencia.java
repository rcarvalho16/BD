import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class AdicionarCompetencia extends JDialog {
    private JComboBox<String> competenciaBox;
    private JComboBox<String> nivelBox;
    private JButton adicionarButton;
    private JButton terminarButton;
    private JPanel competenciaPanel;
    private JTextArea display;
    private MainApp app;
    private Connection connection;
    private User user;
    private boolean start;
    private int count = 0;

    AdicionarCompetencia(Connection connection, MainApp app, User user) {
        this.app = app;
        this.connection = connection;
        this.user = user;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(competenciaPanel);
        setMinimumSize(new Dimension(1440, 720));
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(app);
        setCompetenciaBox();
        addActionListeners();
    }

    private void setCompetenciaBox() {
        competenciaBox.setMaximumRowCount(15);
        QueryUtils.setComboBoxes(competenciaBox, "SELECT DISTINCT designacao FROM competencia", connection);
    }

    private void setNivelBox(ActionEvent e) {
        String query = String.format("SELECT nivel FROM competencia WHERE designacao = '%s';", competenciaBox.getSelectedItem().toString());
        QueryUtils.setComboBoxes(nivelBox, query, connection);
    }

    private void addCompetencia(ActionEvent e) {
        String query;
        String id_user = String.format("SELECT id_utilizador FROM utilizador WHERE username = '%s'", user.getUser());
        String competencia = competenciaBox.getSelectedItem().toString();
        int nivel = Integer.parseInt(nivelBox.getSelectedItem().toString());
        try {
            if (!start) {
                QueryUtils.execSQL(connection, "START TRANSACTION;");
                start = true;
            }
            query = String.format("INSERT INTO prof_comp VALUES((SELECT cod_competencia FROM competencia WHERE (designacao = '%s' AND nivel = %d)), (%s))",
                    competencia, nivel, id_user);

            QueryUtils.execSQL(connection, query);
            display.append(String.format("Adicionado: %s Nivel %d\n", competencia, nivel));
            count++;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "addCompetencia(): Competencia repetida");
        }
    }

    private void setTerminarButton(ActionEvent e){
        try{
            QueryUtils.execSQL(connection, "COMMIT;");
            JOptionPane.showMessageDialog(this, "Foram adicionadas " + count + " novas competencias");
            dispose();
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "setTerminarButton(): Nao foi possivel adicionar as competencias todas");
        }
    }
    private void addActionListeners() {
        competenciaBox.addActionListener(this::setNivelBox);
        terminarButton.addActionListener(this::setTerminarButton);
        adicionarButton.addActionListener(this::addCompetencia);
    }

}
