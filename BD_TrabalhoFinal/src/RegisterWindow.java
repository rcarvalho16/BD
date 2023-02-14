import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class RegisterWindow extends JFrame {
    private JTextField userField;
    private JPanel registerPanel;
    private JTextField nifField;
    private JTextField telefoneField;
    private JTextField nomeField;
    private JPasswordField passwordField;
    private JComboBox<String> distritoBox;
    private JTextField emailField;
    private JTextField sobrenomeField;
    private JPasswordField confirmpasswordField;
    private JButton registerButton;
    private JTextField moradaField;
    private JComboBox<String> concelhoBox;
    private JComboBox<String> freguesiaBox;
    private Connection connection;

    private String username;

    public RegisterWindow(Connection connection) {
        this.connection = connection;
        setTitle("Registo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(registerPanel);
        setVisible(true);
        setMinimumSize(new Dimension(550, 750));
        setLocationRelativeTo(null);
        setResizable(false);
        pack();
        QueryUtils.setComboBoxes(distritoBox, "SELECT desc_distrito FROM distrito", connection);
        addActionListeners();
    }


    private void registo(ActionEvent e) {
        if (insertLogin())
            completeProfile();
    }

    private void insertUser(CompleteProfile pc) {
        boolean is_prof = pc.getProfissionalCheckBox().isSelected();
        boolean is_client = pc.getClienteCheckBox().isSelected();

        User newUser = new User(nifField.getText(), userField.getText(), distritoBox.getSelectedItem().toString(), concelhoBox.getSelectedItem().toString(),
                freguesiaBox.getSelectedItem().toString(), is_prof, is_client, nomeField.getText(), sobrenomeField.getText(), emailField.getText(), telefoneField.getText(), moradaField.getText());

        try {
            String command = String.format("INSERT INTO utilizador(nif_utilizador, cod_distrito, cod_concelho, cod_freguesia, is_profis, is_cliente, username, nome, apelido, email, telefone, morada) " +
                            "VALUES('%s', (SELECT cod_distrito FROM distrito WHERE desc_distrito = '%s'), (SELECT cod_concelho FROM concelho WHERE desc_concelho = '%s')," +
                            "(SELECT cod_freguesia FROM freguesia WHERE desc_freguesia = '%s'), %b, %b, '%s', '%s', '%s', '%s', '%s', '%s')", newUser.getNIF(),
                    newUser.getDistrito(), newUser.getConcelho(), newUser.getFreguesia(), newUser.isIs_profis(), newUser.isIs_cliente(), newUser.getUser()
                    , newUser.getNome(), newUser.getApelido(), newUser.getEmail(), newUser.getTelefone(), newUser.getMorada());
            QueryUtils.execSQL(connection, command);

        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(this, "Preencher todos os campos");
        }
    }

    private boolean insertLogin() {
        String password = String.valueOf(passwordField.getPassword());
        String confirmPW = String.valueOf(confirmpasswordField.getPassword());
        username = userField.getText();

        if (checkConstraints(username, password, confirmPW)) {
            try {
                if (!checkFields()) JOptionPane.showMessageDialog(this, "Por favor preencher todos os campos");
                else {
                    QueryUtils.execSQL(connection, "START TRANSACTION;");
                    String command = String.format("INSERT INTO login VALUES('%s', '%s', current_date());", username, password);
                    QueryUtils.execSQL(connection, command);
                    dispose();
                    LoginWindow loginWindow = new LoginWindow(connection);
                    return true;
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Utilizador ja se encontra registado na base de dados");
            }
        }
        return false;
    }


    private void completeProfile() {
        CompleteProfile pc = new CompleteProfile(connection);
        JOptionPane.showMessageDialog(this, pc.getCompetencePanel(), "Completar perfil", JOptionPane.PLAIN_MESSAGE);
        insertUser(pc);

        String id_user = String.format("SELECT id_utilizador FROM utilizador WHERE username = '%s'", username);

        if(pc.getProfissionalCheckBox().isSelected() && pc.getClienteCheckBox().isSelected()){
            insertClient(id_user, pc);
            InsertProf(id_user, pc);
        }
        else if (pc.getProfissionalCheckBox().isSelected()) {
            InsertProf(id_user, pc);
        } else if (pc.getClienteCheckBox().isSelected()) {
            insertClient(id_user, pc);
        } else {
            JOptionPane.showMessageDialog(this, "Os campos para completar o perfil devem ser preenchidos");
        }
        try {
            QueryUtils.execSQL(connection, "COMMIT;");
        } catch(SQLException ex){
            JOptionPane.showMessageDialog(this, "completeProfile(): Erro no registo");
        }
    }


    private boolean checkConstraints(String user, String password, String confirmPassword) {
        if (user.isBlank()) {
            JOptionPane.showMessageDialog(this, "Utilizador nao pode estar vazio");
            return false;
        }
        if (user.contains(" ")) {
            JOptionPane.showMessageDialog(this, "Utilizador nao pode conter espacos");
            return false;
        }
        if (password.length() < 9) {
            JOptionPane.showMessageDialog(this, "A password deve ser superior a 9 caracteres");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords nao sao iguais");
            return false;
        }
        return true;
    }

    private void setConcelhos(ActionEvent e) {
        String distrito = distritoBox.getSelectedItem().toString();
        String query = String.format("SELECT DISTINCT desc_concelho FROM concelho c INNER JOIN distrito d ON c.cod_distrito = (SELECT cod_distrito FROM distrito d WHERE d.desc_distrito = '%s')"
                , distrito);
        QueryUtils.setComboBoxes(concelhoBox, query, connection);
    }

    private void setFreguesias(ActionEvent e) {
        freguesiaBox.setSelectedItem(null);
        String distrito = distritoBox.getSelectedItem().toString();
        String concelho = concelhoBox.getSelectedItem().toString();
        String query = String.format("SELECT DISTINCT desc_freguesia FROM freguesia f \n" +
                "INNER JOIN concelho c \n" +
                "ON (\n" +
                "\tf.cod_concelho = (SELECT cod_concelho FROM concelho WHERE desc_concelho = '%s')\n" +
                "\tAND f.cod_distrito = (SELECT cod_distrito from distrito d WHERE d.desc_distrito = '%s')\n" +
                ");", concelho, distrito);
        QueryUtils.setComboBoxes(freguesiaBox, query, connection);
    }

    private void setComboBoxes(JComboBox<String> comboBox, String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            comboBox.setModel(new DefaultComboBoxModel<>());
            while (rs.next()) {
                comboBox.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addActionListeners() {
        registerButton.addActionListener(this::registo);
        distritoBox.addActionListener(this::setConcelhos);
        concelhoBox.addActionListener(this::setFreguesias);
    }

    private boolean checkFields() {
        String NIF = nifField.getText();
        String user = userField.getText();
        String nome = nomeField.getText();
        String apelido = sobrenomeField.getText();
        String email = emailField.getText();
        String telefone = telefoneField.getText();
        String morada = moradaField.getText();

        return !NIF.isEmpty() && !user.isEmpty() && !nome.isEmpty() && !email.isEmpty() && !telefone.isEmpty() && !morada.isEmpty();
    }

    private void InsertProf(String id_user, CompleteProfile pc) {
        String query_prof, query_type_prof;
        String type = pc.getComboType().getSelectedItem().toString();
        try {
            if (!type.isBlank()) {
                query_prof = String.format("INSERT INTO profissional VALUES ((%s), '%s');", id_user, type);
                QueryUtils.execSQL(connection, query_prof);

                if (type.equals("Independente")) {
                    int valor_hora = Integer.parseInt(pc.getCustoField().getText());
                    query_type_prof = String.format("INSERT INTO prof_indep VALUES((%s), %d)", id_user, valor_hora);
                    QueryUtils.execSQL(connection, query_type_prof);
                } else if (type.equals("Outrem")) {
                    String id_chefe = String.format("SELECT pe.id_profissional from prof_empresa pe \n" +
                            "INNER JOIN utilizador u ON pe.id_profissional = u.id_utilizador \n" +
                            "WHERE CONCAT(u.nome, ' ', u.apelido) = '%s'", pc.getChefeBox().getSelectedItem());

                    int salario = Integer.parseInt(pc.getSalarioField().getText());
                    String NIF_empresa = String.format("SELECT NIF_empresa from empresa WHERE nome = '%s'", pc.getEmpresaBox().getSelectedItem());
                    query_type_prof = String.format("INSERT INTO prof_empresa VALUES((%s), (%s), %d)", id_user, NIF_empresa, salario);
                    QueryUtils.execSQL(connection, query_type_prof);
                    QueryUtils.execSQL(connection, String.format("INSERT INTO chefia VALUES((%s), (%s))", id_user, id_chefe));
                }
                query_prof = String.format("INSERT INTO prof_comp VALUES((SELECT cod_competencia FROM competencia WHERE (designacao = '%s' AND nivel = %d)), (%s))",
                        pc.getCompetenciaBox().getSelectedItem(), Integer.parseInt(pc.getNivelBox().getSelectedItem().toString()), id_user);
                QueryUtils.execSQL(connection, query_prof);
                String concelho = String.format("SELECT cod_concelho FROM concelho WHERE desc_concelho = '%s'", concelhoBox.getSelectedItem());
                String distrito = String.format("SELECT cod_distrito FROM distrito WHERE desc_distrito = '%s'", distritoBox.getSelectedItem());
                query_prof = String.format("INSERT INTO opera_concelho VALUES((%s),(%s), (%s));", concelho, distrito, id_user);
                QueryUtils.execSQL(connection, query_prof);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        catch (NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Colocar salario");
        }
    }

    private void insertClient(String id_user, CompleteProfile pc) {
        String cod_idioma = String.format("SELECT cod_idioma FROM idioma WHERE desc_idioma = '%s'",
                pc.getIdiomaBox().getSelectedItem());
        String query = String.format("INSERT INTO cliente VALUES((%s), (%s))", id_user, cod_idioma);
        try {
            QueryUtils.execSQL(connection, query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



