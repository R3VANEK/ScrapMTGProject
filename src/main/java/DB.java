import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class DB extends DBConnect implements Scraping, ScrapingAPI{

    private Statement stmt = null;
    private Connection conn = null;


    //nieposiadane w bazie dodatki mtg, możliwe do zaimportowania
    public ArrayList<String> legalSets;


    //zmienna potrzebna do określenia, czy pracujemy na pustej bazie danych, czy nie
    private boolean hasExpansionsInDB = false;


    // TODO: wywal tą funkcję a jej kod dodaj gdzieś w kodzie
    public void setHasExpansionsInDB(boolean newValue){
        this.hasExpansionsInDB = newValue;
    }
    public boolean getHasExpansionsInDB(){
        return this.hasExpansionsInDB;
    }

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
            this.createDB(this.conn);
            this.legalSets = this.getNamesOfAllExpansions1();
        }
        else{
            ArrayList<String> expansionsImported = this.getNamesOfAllExpansions1(this.stmt);
            ArrayList<String> allExpansions = this.getNamesOfAllExpansions1();
            if(expansionsImported.size() != 0){
                allExpansions.removeAll(expansionsImported);
                this.hasExpansionsInDB = true;
            }
            this.legalSets = allExpansions;
        }
        stmt.executeUpdate("use mtg;");
        //this.login();
    }


    public void UploadExpansions(String chosenExpansionsString) throws SQLException, ClassNotFoundException, IOException {

        ArrayList<String> setsArray = new ArrayList<>(Arrays.asList(chosenExpansionsString.split(",")));
        for(String set : setsArray){

                //pobiera pojedyńczy dodatek i zabiera go z legalSets
                //żeby nie dało się go jeszcze raz zaimportować (zdublwoać)
                //this.getSingleExpansion(set, this.legalSets);
                if(this.legalSets.contains(set)){
                    DBConnect.insertExpansion(set);
                    this.fetchCardsFromExpansion(set);
                    this.legalSets.remove(set);
                }
                else{
                    System.out.println("Ten dodatek został już kiedyś zaimportownay, pomijanie...");
                }


        }
    }

    public void login() throws SQLException {
        Scanner loginScanner = new Scanner(System.in);
        System.out.println("Proszę się zalogować na swoje konto");
        System.out.println("Login : ");
        String login = loginScanner.next();
        System.out.println("Password : ");
        String password = loginScanner.next();


        int count = checkLogin(login,password,this.conn);
        if(count == 1){
            System.out.println("Zalogowano poprawnie");
        }
        else{
            System.out.println("Niepoprawne dane logowania, wyłączanie aplikacji....");
            System.exit(0);
        }
    }

    public void printAllCards() throws SQLException {
        System.out.println("Wyświetlanie wszystkich pobranych kart");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
        DBConnect.printAllCardsDB(this.conn);
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
    }



    // TODO : dodaj logikę wyświetlania odpowiednich dodaktów albo z bazy albo z api
    //TODO: przenieś getExpansions tutaj, a ewentualne zapytnaie do api w interfejs

    public void printExpansions(){
        System.out.println("---------------------------------------------------------------------");
        for(String expansion : this.legalSets){
            System.out.println(expansion);
        }
        System.out.println("---------------------------------------------------------------------");
        System.out.println();
    }










}
