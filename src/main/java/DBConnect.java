import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public abstract class DBConnect implements Credentials, Commands{

    private static Integer LAST_INSERTED_ID_EXPANSION=null;
    private static Integer LAST_INSERTED_ID_CARD=null;
    private static ArrayList<Integer> LAST_INSERTED_ID_ARTIST=new ArrayList<>();


    //------------------------------------------------------------------------------------------------------------------------
    //METODY PODSTAWOWE W PRACY Z BAZĄ DANYCH

    protected void createDB(Statement stmt) throws SQLException {
        System.out.println("Wygląda na to, że nie masz utworzonej wcześniej bazy danych, zaraz coś na to poradzimy :)");
        System.out.println("Tworzenie bazy danych i jej struktury...");
        stmt.executeUpdate(sqlInit);
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
    //------------------------------------------------------------------------------------------------------------------------




    public static int checkLogin(String login, String password, Connection conn) throws SQLException {
        String sql = " Select count(*) as count FROM users where login = ? and password = ?;";
        PreparedStatement check = conn.prepareStatement(sql);
        check.setString(1,login);
        check.setString(2,password);
        check.execute();
        ResultSet result = check.getResultSet();
        int returnInt = -1;
        if(result.next()){
            returnInt = result.getInt("count");
        }
        return returnInt;
    }






    //------------------------------------------------------------------------------------------------------------------------
    //METODY WSTAWIAJĄCE DANE DO BAZY
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

            try{ stmt.executeUpdate("INSERT INTO artists(name) VALUES(\""+artistsList[0]+"\")"); }
            catch(SQLException e){
                ResultSet result =  stmt.executeQuery("SELECT id_artist AS id FROM artists WHERE name=\""+artistsList[0]+"\"");
                while(result.next()){
                    LAST_INSERTED_ID_ARTIST.add(result.getInt("id"));
                }
            }

            try { stmt.executeUpdate("INSERT INTO artists(name) VALUES(\""+artistsList[1]+"\")"); }
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
    }


    public static void insertCardExpansionConnection( String price) throws SQLException, ClassNotFoundException {
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

        String sqlInsert1 = "INSERT INTO cards_artists_connection(id_card,id_artist) VALUES("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(0)+")";
        if(LAST_INSERTED_ID_ARTIST.size() > 1){
            sqlInsert1 += ",("+LAST_INSERTED_ID_CARD+","+LAST_INSERTED_ID_ARTIST.get(1)+")";
        }
        stmt.executeUpdate(sqlInsert1);
        conn.close();
    }
    //------------------------------------------------------------------------------------------------------------------------




    //------------------------------------------------------------------------------------------------------------------------
    //METODY ZWIĄZANE Z UAKTUALNIANIEM CEN W BAZIE DANYCH
    public int getNumberCardsToUpdate() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        ResultSet results = stmt.executeQuery("select count(*) as liczba from cards_expansion_connection");
        int numberOfCards = 0;
        while(results.next()){
            numberOfCards = results.getInt("liczba");
        }
        results.close();
        stmt.close();
        conn.close();
        return  numberOfCards;
    }

    public void updatePriceDB(String price, int cardId, int expansionId) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        BigDecimal priceBig;
        try{
            priceBig = new BigDecimal(price);
        }catch(NullPointerException e){
            priceBig = null;
        }

        String sql = "UPDATE cards_expansion_connection SET price="+priceBig+" WHERE id_card="+cardId+" AND id_expansion="+expansionId;
        stmt.executeUpdate(sql);
        stmt.close();
        conn.close();
    }


    public void updatePrices() throws SQLException, InterruptedException, IOException, ClassNotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");

        ResultSet results = stmt.executeQuery("select cards.id_card, cards.card_name, cards_expansion_connection.price, expansions.expansion_name, expansions.id_expansion from cards inner join cards_expansion_connection on cards.id_card = cards_expansion_connection.id_card inner join expansions on cards_expansion_connection.id_expansion = expansions.id_expansion");

        int i = 0;

        System.out.println("Trwa aktualizowanie cen kart, proszę czekać...");
        while(results.next()){
            TimeUnit.SECONDS.sleep(2);
            System.out.print(i+"/"+getNumberCardsToUpdate()+"\r");
            i+=1;
            String price = Scraping.fetchUpdatePrice(
                    results.getString("card_name"),
                    results.getString("expansion_name"));

            String oldPrice = String.valueOf(results.getBigDecimal("price"));
            try{
                if(price.compareTo(oldPrice) != 0){
                    System.out.println("znaleziono różnice cen karty "+results.getString("card_name"));
                    this.updatePriceDB(price, results.getInt("id_card"), results.getInt("id_expansion"));
                }
            } catch(NullPointerException ignored){}

        }
    }
}
