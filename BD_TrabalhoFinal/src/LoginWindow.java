import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginWindow extends JFrame {
    private JPanel loginPanel;
    private JLabel login;
    private JPanel test;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton loginButton;
    private Connection connection;

    public LoginWindow(Connection connection) {
        this.connection = connection;
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(loginPanel);
        setVisible(true);
        setMinimumSize(new Dimension(450, 350));
        setLocationRelativeTo(null);
        pack();

        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addActionListeners();

    }

    private void addActionListeners() {
        registerButton.addActionListener(this::registerButtonClick);
        loginButton.addActionListener(this::loginButtonClick);
    }

    private void registerButtonClick(ActionEvent e) {
        dispose();
        RegisterWindow registerWindow = new RegisterWindow(connection);
    }

    private void loginButtonClick(ActionEvent e){
        String username = usernameField.getText();
        String password = String.valueOf(passwordField.getPassword());

        if(checkValidLogin(username,password)){
            JOptionPane.showMessageDialog(this, "Login efetuado com sucesso");
            dispose();
            MainApp app = new MainApp(connection, username);
        }
        else
            JOptionPane.showMessageDialog(this, "Login errado ou utilizador inexistente");
    }

    private boolean checkValidLogin(String username, String password){
        try {
            if(username.isEmpty() || password.isEmpty())
                return false;
            String query = String.format("SELECT username, password FROM login WHERE (username = '%s' AND password = \'%s\');",
                    username, password);
            ResultSet rs = QueryUtils.executeRS(connection, query);
            if(rs.isBeforeFirst()) return true;
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return false;
    }
}
