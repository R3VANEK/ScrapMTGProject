import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class DB extends DBConnect implements Expansion{

    private Statement stmt = null;
    private Connection conn = null;

    public ArrayList<String> legalSets;


    //to jest sprawdzane tylko przy tworzeniu
    private boolean hasExpansionsInDB = false;

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
            //dodać ifa sprawdzającego czy expansionsImported ma wgl jakieś expansions
            ArrayList<String> expansionsImported = this.getExpansions(this.stmt);
            ArrayList<String> allExpansions = this.getExpansions();
            if(expansionsImported.size() != 0){
                allExpansions.removeAll(expansionsImported);
                this.hasExpansionsInDB = true;
            }
            this.legalSets = allExpansions;
        }
    }


    public void setHasExpansionsInDB(boolean newValue){
        this.hasExpansionsInDB = newValue;
    }
    public boolean getHasExpansionsInDB(){
        return this.hasExpansionsInDB;
    }


    public void getGivenExpansions(String chosenExpansionsString) throws SQLException, ClassNotFoundException {

        ArrayList<String> setsArray = new ArrayList<>(Arrays.asList(chosenExpansionsString.split(",")));

        for(String set : setsArray){
            this.insertExpansion(set);
            try{
                //pobiera pojedyńczy dodatek i zabiera go z legalSets
                //żeby nie dało się go jeszcze raz zaimportować (zdublwoać)
                this.getSingleExpansion(set, this.legalSets);
                this.legalSets.remove(set);

            } catch(IllegalArgumentException | IOException e){
                System.out.println(e.getMessage());
            }
        }
    }







    public void updatePrices(){
        System.out.println("tutaj coś będzie");
    }


}
