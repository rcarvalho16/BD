import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class QueryUtils {

    private static final String USERNAME = "lassunca_Ng04";
    private static final String MYPASS = "mtr.429.dai";

    public static boolean execSQL(Connection conn, String sql) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        return true;
    }

    public static Connection connect(String DBname) {
        Connection connection = null;
        try {
            String connect;
            String driver = "com.mysql.cj.jdbc.Driver";
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                System.out.println("O driver MySql Connector não está instalado.");
                return null;
            }
            connect = "jdbc:mysql://lassuncao-server.com:3306/" + DBname + "?useSSL=false";
            connection = DriverManager.getConnection(connect, USERNAME, MYPASS);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }

    public static ResultSet executeRS(Connection ligacao, String sqlcmd) {
        ResultSet rs = null;
        try {
            Statement stmt = ligacao.createStatement();
            rs = stmt.executeQuery(sqlcmd);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    /**
     * Método que preenche a JTable com os cabeçalhos e as diferentes linhas de um ResultSet
     * @param rs
     * @param tableDados
     * @throws SQLException
     */
    public static void printTable(ResultSet rs,JTable tableDados) throws SQLException {
        List<String[]> rows=printBaseRow(rs);
        String[] headers= printBaseHeader(rs);
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        String[][] tabrows = new String[rows.size()][colCount];
        int i = 0;
        for (String[] lin : rows) {
            tabrows[i] = lin;
            i++;
        }
        DefaultTableModel data = new DefaultTableModel(tabrows, headers);
        tableDados.setModel(data);
    }

    /**
     * Método definido para ser utilizado na definição dos cabeçalhos de uma JTable
     * @param rs ResultSet resultante da query, efetuada anteriormente
     * @return um array de Strings com os cabeçalhos de um ResultSet
     * @throws SQLException
     */
    private static String[] printBaseHeader(ResultSet rs)throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        String[] headers = new String[colCount];
        for (int h = 1; h <= colCount; h++) {
            headers[h - 1] = meta.getColumnName(h);
        }
        return headers;
    }

    /**
     * Método definido para mostrar numa JTable o resultado de ResultSet obtido através de uma Query SQL
     * sem incluir os cabeçalhos
     * @param rs
     * @return uma lista de Arrays de Strings com o conteúdo de um ResultSet, sem cabeçalhos
     * @throws SQLException
     */
    private static List<String[]> printBaseRow(ResultSet rs)throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        List<String[]> rows = new ArrayList<>();
        // Iterate Result set
        while (rs.next()) {
            String[] record = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                String val = rs.getString(i + 1);
                record[i] = val;
            }
            rows.add(record);
        }
        return rows;
    }

    /**
     * O método define o texto de uma determinada combobox passada como parâmetro, obtido através do resultado de uma query SQL
     * O texto definido é o da primeira coluna do resultado da query.
     *
     * @param comboBox a preencher
     * @param query a executar para preencher a combo box
     * @param connection
     */
    public static void setComboBoxes(JComboBox<String> comboBox, String query, Connection connection) {
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

}
