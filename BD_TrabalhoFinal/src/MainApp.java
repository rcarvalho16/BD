import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainApp extends JFrame {
    private JPanel mainApp;
    private JPanel menuPanel;
    private JMenuBar menuBar1;
    private JMenu profissional;
    private JMenu clientItem;
    private JMenuItem AddComp;
    private JMenuItem AddProp;
    private JMenuItem AddPedido;
    private JMenuItem AcceptProp;
    private JMenu Queries;
    private JMenuItem Q1;
    private JMenuItem Q2;
    private JMenuItem Q3;
    private JMenuItem Q4;
    private JMenuItem Q5;
    private JMenuItem Q6;
    private JMenuItem Q7;
    private JMenuItem Q8;
    private JMenuItem Q9;
    private JMenuItem Q10;
    private JMenuItem Q11;
    private JMenuItem Q12;
    private JMenuItem Q13;
    private JTable tabelaDados;
    private JScrollPane ScrollPane;
    private JButton logoutButton;
    private JMenuItem AddFatura;
    private final Connection connection;
    private User user;

    public MainApp(Connection connection, String username) {
        super("Aplicacao");
        this.connection = connection;
        user = User.getUser(username, connection);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(mainApp);
        setVisible(true);
        setMinimumSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);
        addActionListeners();
        setMenuBar();
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ScrollPane.setViewportView(tabelaDados);
        ScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        ScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    }


    private void addPedido(ActionEvent e) {
        PedidoWindow pw = new PedidoWindow();
        JOptionPane.showMessageDialog(this, pw.getPedidoPanel(), "Pedido", JOptionPane.PLAIN_MESSAGE);
        String descricao = pw.getDescricaoField();
        String morada = pw.getMoradaField();
        String query = String.format("INSERT INTO pedido (id_cliente,descricao,data_pedido, morada_intervencao)" +
                "VALUES (( SELECT c.id_cliente FROM cliente c INNER JOIN utilizador u ON u.id_utilizador = c.id_cliente WHERE u.username = '%s')," +
                "'%s', current_date(), '%s')", user.getUser(), descricao, morada);
        try {
            if (descricao.isEmpty() || morada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor preencher os campos");
            } else {
                QueryUtils.execSQL(connection, query);
                JOptionPane.showMessageDialog(this, "Pedido efetuado com sucesso");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Nao foi possivel concluir o pedido");
        }
    }

    private void addProp(ActionEvent e) {
        PropostaWindow pw = new PropostaWindow(connection, this, user);
    }

    private void addComp(ActionEvent e) {
        AdicionarCompetencia ac = new AdicionarCompetencia(connection, this, user);
    }

    private void addQuery1(ActionEvent e) {
        Query1Window q1 = new Query1Window(connection, this);
    }

    private void addQuery2(ActionEvent e) {

        Object input = OptionPaneObject("SELECT nome FROM empresa", "Erro a selecionar empresas", "Selecione a empresa", "Query 2");
        String query = String.format("SELECT pe.id_profissional 'ID Profissional', CONCAT(u.nome, ' ', u.apelido) 'Nome completo', COUNT(pe.id_profissional) as Competencias \n" +
                "                FROM prof_comp pc\n" +
                "                INNER JOIN prof_empresa pe ON pc.id_profissional = pe.id_profissional\n" +
                "                INNER JOIN empresa e ON pe.NIF_empresa = e.NIF_empresa\n" +
                "                INNER JOIN utilizador u ON pe.id_profissional = u.id_utilizador \n" +
                "                WHERE e.nome = '%s'\n" +
                "                GROUP BY pe.id_profissional HAVING Competencias>1;", input.toString());

        executeAndPrint(query, 2);
    }

    private void addQuery3(ActionEvent e) {
        Query3Window query3Window = new Query3Window(connection, this);
    }

    private void addQuery4(ActionEvent e) {
        executeAndPrint("SELECT * FROM vQUERY4", 4);
    }

    private void addQuery5(ActionEvent e) {
        String query = "SELECT p.id_profissional as 'ID Profissional', CONCAT(u.nome,' ', u.apelido) as Nome, count(*) as 'Propostas Nao Adjudicadas' FROM profissional p \n" +
                "INNER JOIN proposta pr ON pr.id_profissional = p.id_profissional\n" +
                "INNER JOIN utilizador u ON u.id_utilizador = p.id_profissional\n" +
                "WHERE id_proposta NOT IN (SELECT id_proposta FROM aceita_prop ap)\n" +
                "GROUP BY p.id_profissional ORDER BY 3 DESC, 2 \n" +
                "LIMIT 10;";

        executeAndPrint(query, 5);
    }

    private void addQuery6(ActionEvent e) {
        FaturaWindow faturaWindow = new FaturaWindow(connection, this, user);
    }

    private void addQuery7(ActionEvent e) {
        String query = "SELECT pe.id_profissional as 'ID do profissional', CONCAT(u.nome, u.apelido) 'Nome Profissional', e.nome AS 'Nome Empresa' FROM prof_empresa pe \n" +
                "INNER JOIN empresa e ON e.NIF_empresa = pe.NIF_empresa \n" +
                "INNER JOIN utilizador u ON pe.id_profissional = u.id_utilizador\n" +
                "INNER JOIN chefia c ON id_profissional = c.id_funcionario\n" +
                "WHERE c.id_chefe IS NULL;";

        executeAndPrint(query, 7);
    }


    private void addQuery8(ActionEvent e) {
        Object input = OptionPaneObject("SELECT nome FROM empresa", "Erro a selecionar empresas", "Selecione a empresa", "Query 8");

        String query = String.format("SELECT e.nome as 'Empresa', c.id_chefe as 'Chefe', ROUND(SUM(vc.Subtotal),2) as Receita  FROM empresa e \n" +
                "INNER JOIN prof_empresa pe ON e.NIF_empresa = pe.NIF_empresa\n" +
                "INNER JOIN chefia c ON c.id_funcionario = pe.id_profissional \n" +
                "INNER JOIN proposta p ON p.id_profissional = pe.id_profissional \n" +
                "INNER JOIN vCustototalprosp vc ON vc.id_proposta = p.id_proposta\n" +
                "INNER JOIN fatura f ON f.id_proposta = vc.id_proposta \n" +
                "WHERE e.nome = '%s'\n" +
                "GROUP BY c.id_chefe \n" +
                "ORDER BY 3 DESC\n" +
                "LIMIT 3;", input.toString());

        executeAndPrint(query, 8);
    }

    private void addQuery9(ActionEvent e){
        Object input = OptionPaneObject("SELECT desc_concelho FROM concelho ORDER BY 1 ASC", "Erro a selecionar concelhos", "Selecione o concelho", "Query 9");
        String query = String.format("SELECT f.cod_freguesia as \"Código Freguesia\", f.desc_freguesia as 'Nome Freguesia', c.cod_concelho as 'Código concelho', c.desc_concelho as 'Nome Concelho' FROM freguesia f \n" +
                "INNER JOIN concelho c ON (c.cod_concelho  = f.cod_concelho AND c.cod_distrito = f.cod_distrito)\n" +
                "WHERE c.desc_concelho = '%s' \n" +
                "AND f.cod_freguesia NOT IN (\n" +
                "SELECT f.cod_freguesia as \"Código Freguesia\" FROM freguesia f \n" +
                "INNER JOIN concelho c ON (c.cod_concelho  = f.cod_concelho AND c.cod_distrito = f.cod_distrito)\n" +
                "INNER JOIN utilizador u2 ON u2.cod_distrito = f.cod_distrito AND u2.cod_concelho =f.cod_concelho  AND u2.cod_freguesia = f.cod_freguesia \n" +
                "INNER JOIN cliente c3 ON u2.id_utilizador = c3.id_cliente\n" +
                "WHERE c.desc_concelho  = '%s'\n" +
                ");", input.toString(), input.toString());

        executeAndPrint(query, 9);
    }

    private void addQuery10(ActionEvent e){
        String query = "SELECT id as 'ID do Profissional', nome as 'Nome do Profissional' from vTodosprofis vt2 \n" +
                "WHERE id NOT IN(\n" +
                "    SELECT vt.id  FROM vTodosprofis vt INNER JOIN proposta p ON vt.id = p.id_profissional \n" +
                "    INNER JOIN fatura f ON p.id_proposta = f.id_proposta  \n" +
                ");";
        executeAndPrint(query, 10);
    }

    private void addQuery11(ActionEvent e){
        String query = "SELECT c.cod_competencia as 'Codigo', c.designacao as 'Designacao',  COUNT(pc.id_profissional) as 'N. de profissionais' from prof_comp pc\n" +
                "RIGHT JOIN competencia c ON c.cod_competencia = pc.cod_competencia\n" +
                "GROUP BY c.cod_competencia \n" +
                "ORDER BY 3 DESC, 1 ASC;";

        executeAndPrint(query, 11);
    }

    private void addQuery12(ActionEvent e){
        String query = "SELECT e.nome as \"Nome da Empresa\", e.contacto as \"Contacto da Empresa\", COUNT(p2.id_profissional) as 'Colaboradores',\n" +
                "CONCAT(SUM(p2.salario), ' euros') as \"Encargos operacionais\", CONCAT(ROUND(SUM(p2.salario)/COUNT(p2.id_profissional),2), ' euros') as \"Média salarial\" \n" +
                "FROM utilizador u  \n" +
                "INNER JOIN profissional p ON p.id_profissional = u.id_utilizador \n" +
                "INNER JOIN prof_empresa p2 ON p.id_profissional  = p2.id_profissional \n" +
                "INNER JOIN empresa e ON e.NIF_empresa = p2.NIF_empresa\n" +
                "GROUP BY e.nome\n" +
                "ORDER BY 3 DESC;";
        executeAndPrint(query, 12);
    }

    private void addQuery13(ActionEvent e){
        String query = "SELECT um.simbolo,  m.nome as 'Material', COUNT(lp.cod_material) as 'Nº de vezes usado'  FROM proposta p\n" +
                "INNER JOIN linha_proposta lp ON lp.id_proposta  = p.id_proposta \n" +
                "RIGHT JOIN material m ON m.cod_material = lp.cod_material\n" +
                "LEFT JOIN unidade_medida um ON m.simbolo  = um.simbolo \n" +
                "GROUP BY m.nome\n" +
                "ORDER BY 3 DESC;";
        executeAndPrint(query, 13);
    }

    private void acceptProp(ActionEvent e){
        listPropostasWindow lpw = new listPropostasWindow(connection, this, user);
    }

    private void addActionListeners() {
        AddPedido.addActionListener(this::addPedido);
        AddProp.addActionListener(this::addProp);
        AddComp.addActionListener(this::addComp);
        AcceptProp.addActionListener(this::acceptProp);
        AddFatura.addActionListener(this::addQuery6);
        logoutButton.addActionListener(this::setLogoutButton);
        Q1.addActionListener(this::addQuery1);
        Q2.addActionListener(this::addQuery2);
        Q3.addActionListener(this::addQuery3);
        Q4.addActionListener(this::addQuery4);
        Q5.addActionListener(this::addQuery5);
        Q7.addActionListener(this::addQuery7);
        Q8.addActionListener(this::addQuery8);
        Q9.addActionListener(this::addQuery9);
        Q10.addActionListener(this::addQuery10);
        Q11.addActionListener(this::addQuery11);
        Q12.addActionListener(this::addQuery12);
        Q13.addActionListener(this::addQuery13);
    }

    private void setMenuBar() {
        if (!user.isIs_profis()) {
            profissional.setEnabled(false);
        }
        if (!user.isIs_cliente()) {
            clientItem.setEnabled(false);
        }
    }

    private void setLogoutButton(ActionEvent e){
        dispose();
        LoginWindow loginWindow = new LoginWindow(connection);
    }
    private void executeAndPrint(String query, int query_number) {
        try {
            ResultSet rs = QueryUtils.executeRS(connection, query);
            QueryUtils.printTable(rs, tabelaDados);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Q" + query_number + "(): Erro a representar dados");
        }
    }

    private ArrayList<String> OptionPaneComboBox(String query) throws SQLException {
        ArrayList<String> list = new ArrayList<>();

        ResultSet rs = QueryUtils.executeRS(connection, query);
        while (rs.next())
            list.add(rs.getString(1));

        return list;
    }

    private Object OptionPaneObject(String query, String error_message, String OptionMessage, String title) {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = OptionPaneComboBox(query);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, error_message);
        }
        Object input = JOptionPane.showInputDialog(null, OptionMessage, title, JOptionPane.INFORMATION_MESSAGE, null, list.toArray(), "");
        return input;
    }
}
