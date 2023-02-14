import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class User {
    private String NIF;
    private String user;
    private String distrito;
    private String concelho;
    private String freguesia;
    private boolean is_profis;
    private boolean is_cliente;
    private String nome;
    private String apelido;
    private String email;
    private String telefone;
    private String morada;


    public User(String NIF, String user, String distrito, String concelho, String freguesia, boolean is_profis,
                boolean is_cliente, String nome, String apelido, String email, String telefone, String morada) {
        this.NIF = NIF;
        this.user = user;
        this.distrito = distrito;
        this.concelho = concelho;
        this.freguesia = freguesia;
        this.is_profis = is_profis;
        this.is_cliente = is_cliente;
        this.nome = nome;
        this.apelido = apelido;
        this.email = email;
        this.telefone = telefone;
        this.morada = morada;
    }
    public User(String username){
        this.user = username;
    }

    public void setIs_profis(boolean is_profis) {
        this.is_profis = is_profis;
    }

    public void setIs_cliente(boolean is_cliente) {
        this.is_cliente = is_cliente;
    }

    public String getNIF() {
        return NIF;
    }

    public String getUser() {
        return user;
    }

    public String getDistrito() {
        return distrito;
    }

    public String getConcelho() {
        return concelho;
    }

    public String getFreguesia() {
        return freguesia;
    }

    public boolean isIs_profis() {
        return is_profis;
    }

    public boolean isIs_cliente() {
        return is_cliente;
    }

    public String getNome() {
        return nome;
    }

    public String getApelido() {
        return apelido;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getMorada() {
        return morada;
    }

    public static int getIDUtilizador(String nif, Connection connection){
        String query = String.format("SELECT id_utilizador FROM utilizador WHERE nif_utilizador = '%s'", nif);
        ResultSet rs = QueryUtils.executeRS(connection, query);
        int id_utilizador = 0;
        try{
            rs.next();
            id_utilizador = Integer.parseInt(rs.getString(1));
        } catch(SQLException ex){
            ex.printStackTrace();
        }
        return id_utilizador;
    }
    /**
     *
     * @param username recolhido da janela de login
     * @param connection conexão à base de dados
     * @return instância de utilizador
     */
    public static User getUser(String username, Connection connection) {
        String query = String.format("SELECT * FROM utilizador WHERE username = '%s';", username);
        ResultSet rs = QueryUtils.executeRS(connection, query);
        User user = null;
        try {
            rs.next();
            user = new User(rs.getString("nif_utilizador"), rs.getString("username"), rs.getString("cod_distrito"), rs.getString("cod_concelho"),
                    rs.getString("cod_freguesia"), rs.getBoolean("is_profis"), rs.getBoolean("is_cliente"), rs.getString("nome"), rs.getString("apelido"),
                    rs.getString("email"), rs.getString("telefone"), rs.getString("morada"));

        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "getUser, login ilegal");
            System.exit(-1);
        }
        return user;
    }

}
