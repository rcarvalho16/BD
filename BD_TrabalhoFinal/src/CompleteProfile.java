import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CompleteProfile extends JFrame {
    private JPanel CompetencePanel;
    private JLabel completeLabel;
    private JComboBox<String> comboType;
    private JLabel profType;
    private JLabel value_nif;
    private JTextField custoField;
    private JCheckBox clienteCheckBox;
    private JCheckBox profissionalCheckBox;
    private JComboBox<String> idiomaBox;
    private JTextField salarioField;
    private JComboBox<String> empresaBox;
    private JComboBox<String> chefeBox;
    private JComboBox<String> competenciaBox;
    private JComboBox<String> nivelBox;
    private Connection connection;

    public JComboBox<String> getChefeBox() { return chefeBox; }

    public JComboBox getCompetenciaBox() { return competenciaBox; }

    public JComboBox getNivelBox() { return nivelBox; }

    public JPanel getCompetencePanel() {
        return CompetencePanel;
    }

    public JTextField getCustoField() {
        return custoField;
    }

    public JComboBox<String> getEmpresaBox() {
        return empresaBox;
    }

    public JTextField getSalarioField() {
        return salarioField;
    }

    public JComboBox<String> getComboType() {
        return comboType;
    }

    public JComboBox<String> getIdiomaBox() {
        return idiomaBox;
    }

    public JCheckBox getProfissionalCheckBox() {
        return profissionalCheckBox;
    }

    public JCheckBox getClienteCheckBox() {
        return clienteCheckBox;
    }

    public CompleteProfile(Connection connection) {
        this.connection = connection;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(CompetencePanel);
        this.setPreferredSize(new Dimension(1440, 720));
        disableFields();
        addActionListeners();
    }

    private void checkProfessionalCheckbox(ActionEvent e) {
        if (!profissionalCheckBox.isSelected()) {
            disableFields();
            idiomaBox.setEnabled(false);
        } else {
            comboType.setEnabled(true);
            loadCompetenceBox();
            competenciaBox.setEnabled(true);
            nivelBox.setEnabled(true);

        }
    }

    private void profcomboBoxAction(ActionEvent e) {
        String professionalType;
        professionalType = comboType.getSelectedItem().toString();
        if (professionalType.equals("Outrem")) {
            loadEmpresacomboBox();
            chefeBox.setEnabled(true);
            salarioField.setEnabled(true);
            empresaBox.setEnabled(true);
            custoField.setText("");
            custoField.setEnabled(false);
            idiomaBox.setEnabled(false);
            idiomaBox.setSelectedItem("");
        } else if (professionalType.equals("Independente")) {
            custoField.setEnabled(true);
            chefeBox.setEnabled(false);
            empresaBox.setEnabled(false);
            empresaBox.setSelectedItem("");
            chefeBox.setSelectedItem("");
            chefeBox.setEnabled(false);
            salarioField.setEnabled(false);
            salarioField.setText("");
        } else {
            disableFields();
        }
    }


    private void disableFields() {
        competenciaBox.setEnabled(false);
        competenciaBox.setSelectedItem("");
        nivelBox.setEnabled(false);
        nivelBox.setSelectedItem("");
        comboType.setEnabled(false);
        comboType.setSelectedItem("");
        empresaBox.setEnabled(false);
        empresaBox.setSelectedItem("");
        idiomaBox.setEnabled(false);
        idiomaBox.setSelectedItem("");
        empresaBox.setEnabled(false);
        empresaBox.setSelectedItem("");
        custoField.setEnabled(false);
        custoField.setText("");
        chefeBox.setEnabled(false);
        chefeBox.setSelectedItem("");
        salarioField.setEnabled(false);
        salarioField.setText("");
    }

    private void checkClientCheckbox(ActionEvent e) {
        if (clienteCheckBox.isSelected()){
            idiomaBox.setEnabled(true);
            String query = "SELECT desc_idioma FROM idioma";
            QueryUtils.setComboBoxes(idiomaBox, query, connection);
        }
        else idiomaBox.setEnabled(false);
    }

    private void loadEmpresacomboBox() {
        try {
            String query = "SELECT nome FROM empresa";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                empresaBox.addItem(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChefeBox(ActionEvent e) {
        String empresa = empresaBox.getSelectedItem().toString();
        String query = String.format("SELECT CONCAT(u.nome,' ', u.apelido) as nome FROM empresa e \n" +
                "INNER JOIN prof_empresa pe ON e.NIF_empresa = pe.NIF_empresa\n" +
                "INNER JOIN utilizador u ON u.id_utilizador = pe.id_profissional \n" +
                "WHERE e.nome = '%s';", empresa);

        QueryUtils.setComboBoxes(chefeBox, query, connection);
    }

    private void loadCompetenceBox(){
        QueryUtils.setComboBoxes(competenciaBox, "SELECT DISTINCT designacao FROM competencia", connection);
    }
    private void loadNivelBox(ActionEvent e){
        String query = String.format("SELECT nivel FROM competencia WHERE designacao = '%s'", competenciaBox.getSelectedItem());
        QueryUtils.setComboBoxes(nivelBox, query, connection);
    }

    private void addActionListeners() {
        profissionalCheckBox.addActionListener(this::checkProfessionalCheckbox);
        clienteCheckBox.addActionListener(this::checkClientCheckbox);
        comboType.addActionListener(this::profcomboBoxAction);
        empresaBox.addActionListener(this::loadChefeBox);
        competenciaBox.addActionListener(this::loadNivelBox);
    }


}
