import java.sql.*;

public class Main {

    private static final String DBNAME = "lassunca_BDNg04";


    public static void main(String[] args) {
        Connection connection = QueryUtils.connect(DBNAME);
        LoginWindow loginWindow = new LoginWindow(connection);
    }


}
