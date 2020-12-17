import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class DBConnect implements Credentials, Commands{

    private static Integer LAST_INSERTED_ID_EXPANSION=null;
    private static Integer LAST_INSERTED_ID_CARD=null;
    private static ArrayList<Integer> LAST_INSERTED_ID_ARTIST=new ArrayList<>();




    protected void createDB(Statement stmt) throws SQLException {

        System.out.println("Wygląda na to, że nie masz utworzonej wcześniej bazy danych, zaraz coś na to poradzimy :)");
        System.out.println("Tworzenie bazy danych i jej struktury...");
        stmt.executeUpdate(sqlCreateDB);
        stmt.executeUpdate("use mtg;");
        for(String sqlCommand : sqlStructure){
            stmt.executeUpdate(sqlCommand);
        }
        System.out.println("Utworzono bazę danych");
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
        conn.close();
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
        conn.close();
    }


    public static void insertArtist(String artists) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        LAST_INSERTED_ID_ARTIST.clear();
        //dwóch artystów pracujących na kartą
        if(artists.contains("&amp;")){
            String[] artistsList = artists.split("\\s&amp;\\s");

            try{
                stmt.executeUpdate("INSERT INTO artists(name) VALUES(\""+artistsList[0]+"\")");
            }
            catch(SQLException e){
                ResultSet result =  stmt.executeQuery("SELECT id_artist AS id FROM artists WHERE name=\""+artistsList[0]+"\"");
                while(result.next()){
                    LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
                }
            }

            try {
                stmt.executeUpdate("INSERT INTO artists(name) VALUES(\""+artistsList[1]+"\")");

            }
            catch(SQLException e){
                ResultSet result =  stmt.executeQuery("SELECT id_artist AS id FROM artists WHERE name=\""+artistsList[1]+"\"");
                while(result.next()){
                    LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
                }
            }

            //jeżeli kod wszedł w tego ifa, to znaczy, że oba inserty się udały
            //i potrzebujemy do artists_connection dwóch id
            if(LAST_INSERTED_ID_ARTIST.size() == 0){
                ResultSet result =  stmt.executeQuery("SELECT id_artist as id from artists order by id_artist desc limit 2");
                while(result.next()){
                    LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
                }
            }

        }
        //pojedyńczy artysta pracujący nad kartą
        else{
                stmt.executeUpdate("INSERT IGNORE INTO artists(name) VALUES(\""+artists+"\")");
                ResultSet result =  stmt.executeQuery("SELECT id_artist as id FROM artists WHERE name=\""+artists+"\"");
                while(result.next()){
                    LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
                }
        }
        conn.close();


        //ustalanie właściwych id wstawionych artystów
        //do późniejszego użycia w cards_artists_connection
        //życie byłoby piękne gdybym mógl to zrobić, ale z powodu jdbc musze za każdym razem tworzyć nowy ResultSet
        //i nie mogę utworzyć ArrayLista z jego wynikami
       /* LAST_INSERTED_ID_ARTIST.clear();
        for(ResultSet result : last_id_artists){
            while(result.next()){
                LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
            }
        }

        conn.close();*/

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


        ResultSet indexOfLastCard;
        try{
            stmt.executeUpdate(sqlInsert);
            indexOfLastCard = stmt.executeQuery("SELECT MAX(id_card) AS id FROM cards");
        }
        //jeżeli kod wejdzie do tego catcha to znaczy, że taki rekord już istnieje
        //w takim wypadku musimy znaleźć jego właściwe id w bazie żeby prawidłowo
        //stworzyć relację
        catch (SQLException e){
            System.out.println(e.getMessage());
            indexOfLastCard = stmt.executeQuery("SELECT id_card AS id FROM cards WHERE card_name=\""+cardName+"\"");
        }


        while(indexOfLastCard.next()){
            LAST_INSERTED_ID_CARD = indexOfLastCard.getInt("id");
        }
        conn.close();


        //tutaj może sprawdznei czy selectowane id jest takie same jak last inserted
        //może to wskazać czy wstawiono nowy rekord czy nie
        // z artystami chyba też jest taki problem

    }




    public static void insertCardExpansionConnection(String expansionName, String price) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        BigDecimal priceBig;
        try{
            priceBig = new BigDecimal(price);
        }

        //to są bardzo rzadkie przypadki, kiedy nie
        //zdołano prawidłowo odczytać ceny karty z cardMarketu
        //jest to spowodowane bardzo dziwnymi nazwami kart ze znakami specjalnymi
        //url ma wtedy inną zasadę układania się, często po prostu zastępuje te znaki czymś innym
        //ten błąd powoduje często znaczek Æ w nazwach kart
        //mógłbym to naprawić, ale zajęłoby mi to za dużo czasu, na razie dopuszczam w bazie
        //możliwość ustawiania nulla w cenach kart gdyby prawidłowo nie odczytano ceny

        //kolejna możliwość dlaczego w bazie jest tyle nulli jest po prostu zakorkowanie zstrony cardMarket
        //gdy czas na odpowiedź zapytania jest przekorszona automatycznie zostaje tam null
        //przed tym nie da się uchronić, ale zawsze można próbowac uaktualnić cenę kart
        catch(NullPointerException e){
            priceBig = null;
        }
        String sqlInsert = "INSERT INTO cards_expansion_connection(id_card,price,id_expansion) VALUES("+LAST_INSERTED_ID_CARD+","+priceBig+","+LAST_INSERTED_ID_EXPANSION+")";
        stmt.executeUpdate(sqlInsert);
    }

    public static void insertCardArtistsConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");


        // to może spowodować błędy jeżeli mamy już takiego artystę
        String sqlInsert1 = "INSERT INTO cards_artists_connection(id_card,id_artist) VALUES("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(0)+")";
        if(LAST_INSERTED_ID_ARTIST.size() > 1){
            sqlInsert1 += ",("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(1)+")";
        }
        stmt.executeUpdate(sqlInsert1);
        conn.close();
    }


    public ResultSet getCardsToUpdatePrice(Statement stmt) throws SQLException {
        return stmt.executeQuery("SELECT cards.card_name,cards_expansion_connection.price,expansions.expansion_name from cards inner join cards_expansion_connection on cards.id_card = cards_expansion_connection.id_card inner join expansions on cards_expansion_connection.id_expansion = expansions.id_expansion");
    }




}
