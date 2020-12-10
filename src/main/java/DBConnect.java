import java.sql.*;
import java.util.Arrays;


public abstract class DBConnect implements Credentials, Commands{

    private static int last_inserted_id;

    public static void createDB() throws ClassNotFoundException, SQLException {

        Class.forName(JDBC_DRIVER);
        
        System.out.println("łączenie z xamppem...");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("tworzenie struktury bazy danych...");
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(sqlCreateDB);
        stmt.executeUpdate("use mtg;");
        for(String sqlCommand : sqlStructure){
            stmt.executeUpdate(sqlCommand);
        }

        System.out.println("Utworzone bazę danych");
    }

    public static boolean checkDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        String sql = "show databases like 'mtg'";
        ResultSet results = stmt.executeQuery(sql);
        String wynik = "";
        while(results.next()){
           wynik =  results.getString(1);
        }
        return !wynik.isBlank() || !wynik.isEmpty();
    }

    public static void insertExpansion(String expansionName) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        String sqlInsert = "INSERT INTO expansions(expansion_name)"+
                    "VALUES("+expansionName+")";
        stmt.executeUpdate(sqlInsert);
    }
    public static void insertArtist(String artists) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        //trzeba to podzielić na ewentualne dwa rekordy bo jedną kartę mogą zaprojektować dwie osoby
        if(artists.contains("&amp;")){
            String[] artistsList = artists.split("\\s&amp;\\s");


            String sqlInsert1 = "INSERT INTO artists(firstName,lastName)"+
                                "VALUES("+artistsList[0].split("\\s")[0]+","+artistsList[0].split("\\s")[1]+"),"+
                                "("+artistsList[1].split("\\s")[0]+","+artistsList[1].split("\\s")[1]+")";
            System.out.println(sqlInsert1);
        }
        else{
            String sqlInsert1 = "INSERT INTO artists(firstName,lastName)"+
                                "VALUES("+artists.split("\\s")[0]+","+artists.split("\\s")[1]+")";
            System.out.println(sqlInsert1);
        }
    }

    public static void insertCard(String cardName,String cardImage,String manaCost, int cmc, int cardNumber, String cardType, String rarity, int power, int toughness) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        //price w strukturze tabeli cards_expansions
        String sqlInsert = "INSERT INTO cards(card_name,card_image,mana_cost,converted_mana_cost,card_number,card_type,rarity,power,toughness)"+
                            "VALUES("+cardName+","+cardImage+","+manaCost+","+cmc+","+cardNumber+","+cardType+","+rarity+","+power+","+toughness+")";
    }

    public static void insertCardExpansionConnection(String expansionName, float price) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        //naprawdé powátpiewam czy to zadziała
        ResultSet indexOfExpansion = stmt.executeQuery("SELECT id_expansion FROM expansions WHERE expansion_name="+expansionName);
        System.out.println(indexOfExpansion);
    }
    public static void insertCardArtistsConnection(){

    }



}
