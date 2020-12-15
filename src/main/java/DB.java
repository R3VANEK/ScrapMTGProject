import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DB extends DBConnect implements Expansion1 {

    private Statement stmt = null;
    private Connection conn = null;

    public ArrayList<String> legalSets = new ArrayList<>();

    public DB() throws ClassNotFoundException, SQLException, IOException {

        //nawiązywanie połączenia
        System.out.println("Łączenie z xampem...");
        Class.forName(JDBC_DRIVER);
        this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
        this.stmt = conn.createStatement();


        //tworzenie bazy danych
        //importowanie dostępnych dodatków
        //w przypadku nowej bazy będą to wszystkie zestawy
        //jeżeli baza istniała to tylko te, które nie istniały
        //wcześniej w bazie, unika to importowania dodatków jeszcze raz
        if(!DBConnect.checkDB()){
            this.createDB(this.stmt);
            this.legalSets = this.getExpansions();
        }
        else{
            ArrayList<String> expansionsImported = this.getExpansions(this.stmt);
            ArrayList<String> allExpansions = this.getExpansions();
            allExpansions.removeAll(expansionsImported);
            this.legalSets = allExpansions;
        }





    }

    public void updatePrices(){
        System.out.println("tutaj coś będzie");
    }

    public void getExpansion(){
        System.out.println("tutaj coś będzie");
    }
}
