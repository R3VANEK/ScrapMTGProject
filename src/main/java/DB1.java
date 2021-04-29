import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class DB1 implements CommunicationMYSQL{



    private final Statement stmt;
    private final Connection conn;

    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://localhost:3306?allowMultiQueries=true";
    private final String USER = "root";
    private final String PASS = "";


    //nieposiadane w bazie dodatki mtg, możliwe do zaimportowania
    public ArrayList<String> legalSets;

    //zmienna potrzebna do określenia, czy pracujemy na pustej bazie danych, czy nie
    public boolean hasExpansionsInDB = false;


    private static Integer LAST_INSERTED_ID_EXPANSION=null;

    public Connection getConn() {
        return conn;
    }

    public DB1(ArrayList<String> allExpansions) throws ClassNotFoundException, SQLException, IOException {


        System.out.println("Łączenie z xampem...");
        Class.forName(JDBC_DRIVER);
        this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
        this.stmt = conn.createStatement();

        if(!DBConnect.checkDB()){
            this.createDB1(this.conn);
        }
        else{
            ArrayList<String> expansionsImported = this.getNamesOfAllExpansions1(this.stmt);
            if(expansionsImported.size() != 0){
                allExpansions.removeAll(expansionsImported);
                this.hasExpansionsInDB = true;
               boolean test =  allExpansions.contains("Amonkhet");
                System.out.println(test);
            }
        }
        this.legalSets = allExpansions;
        stmt.executeUpdate("use mtg;");
        //this.login();
    }



    public void login() throws SQLException {
        Scanner loginScanner = new Scanner(System.in);
        System.out.println("Proszę się zalogować na swoje konto");
        System.out.println("Login : ");
        String login = loginScanner.next();
        System.out.println("Password : ");
        String password = loginScanner.next();


        int count = CommunicationMYSQL.checkLogin(login,password,this.conn);
        if(count == 1){
            System.out.println("Zalogowano poprawnie");
        }
        else{
            System.out.println("Niepoprawne dane logowania, wyłączanie aplikacji....");
            System.exit(0);
        }
    }



}
