import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class DBConnect implements Credentials, Commands{

    private static Integer LAST_INSERTED_ID_EXPANSION=null;
    private static Integer LAST_INSERTED_ID_CARD=null;
    private static ArrayList<Integer> LAST_INSERTED_ID_ARTIST=new ArrayList<>();

    public static void createDB() throws ClassNotFoundException, SQLException {

        Class.forName(JDBC_DRIVER);
        
       // System.out.println("łączenie z xamppem...");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("tworzenie struktury bazy danych...");
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(sqlCreateDB);
        stmt.executeUpdate("use mtg;");
        for(String sqlCommand : sqlStructure){
            stmt.executeUpdate(sqlCommand);
        }

        //System.out.println("Utworzone bazę danych");
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
                    "VALUES(\""+expansionName+"\")";
        stmt.executeUpdate(sqlInsert);

        ResultSet indexOfExpansion = stmt.executeQuery("SELECT id_expansion FROM expansions WHERE expansion_name=\""+expansionName+"\"");
        while(indexOfExpansion.next()){
            LAST_INSERTED_ID_EXPANSION= indexOfExpansion.getInt("id_expansion");
        }
    }


    public static void insertArtist(String artists) throws ClassNotFoundException, SQLException {



        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        //trzeba to podzielić na ewentualne dwa rekordy bo jedną kartę mogą zaprojektować dwie osoby
        String sqlInsert1;
        String sqlSelect1;
        if(artists.contains("&amp;")){
            String[] artistsList = artists.split("\\s&amp;\\s");
            sqlInsert1 = "INSERT IGNORE INTO artists(name)"+
                                "VALUES("+"\""+artistsList[0]+"\"),"+
                                "("+"\""+artistsList[1]+"\")";

            sqlSelect1 = "SELECT id_artist as id from artists order by id_artist desc limit 2";
        }
        else{
            //niektórzy nie mają imienia i nazwiska a pseudonimy
            sqlInsert1 = "INSERT INTO artists(name)"+
                                "VALUES("+"\""+artists+"\")";
            sqlSelect1 = "SELECT MAX(id_artist) as id FROM artists";
        }


        //System.out.println(sqlInsert1);
        stmt.executeUpdate(sqlInsert1);
        ResultSet indexOfArtists = stmt.executeQuery(sqlSelect1);
        LAST_INSERTED_ID_ARTIST.clear();
        while(indexOfArtists.next()){
            LAST_INSERTED_ID_ARTIST.add(indexOfArtists.getInt("id"));
        }
        //System.out.println(LAST_INSERTED_ID_ARTIST);
    }



    public static void insertCard(String cardName, String cardImage, String manaCost, int cmc, int cardNumber, String cardType, String rarity, String power, String toughness) throws SQLException, ClassNotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        //price w strukturze tabeli cards_expansions
        String sqlInsert = "INSERT INTO cards(card_name,card_image,mana_cost,converted_mana_cost,card_number,card_type,rarity,power,toughness)"+
                            "VALUES(\""+cardName+"\",\""+cardImage+"\",\""+manaCost+"\","+cmc+","+cardNumber+",\""+cardType+"\",\""+rarity+"\",\""+power+"\",\""+toughness+"\");"+
                            "SELECT LAST_INSERT_ID() as Id;";

        stmt.executeUpdate(sqlInsert);

        ResultSet indexOfLastCard = stmt.executeQuery("SELECT MAX(id_card) AS id FROM cards");
        while(indexOfLastCard.next()){
            LAST_INSERTED_ID_CARD = indexOfLastCard.getInt("id");
        }
    }


    public static void insertCardExpansionConnection(String expansionName, String price) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");


        BigDecimal priceBig = new BigDecimal(price);
        String sqlInsert = "INSERT INTO cards_expansion_connection(id_card,price,id_expansion) VALUES("+LAST_INSERTED_ID_CARD+","+priceBig+","+LAST_INSERTED_ID_EXPANSION+")";
        stmt.executeUpdate(sqlInsert);
    }

    public static void insertCardArtistsConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        String sqlInsert1 = "INSERT INTO cards_artists_connection(id_card,id_artist) VALUES("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(0)+")";
        if(LAST_INSERTED_ID_ARTIST.size() > 1){
            sqlInsert1 += ",("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(1)+")";
        }
        stmt.executeUpdate(sqlInsert1);
    }
}
