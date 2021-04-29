import javax.swing.plaf.nimbus.State;
import javax.xml.transform.Result;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public abstract class DBConnect implements Credentials{

    private static Integer LAST_INSERTED_ID_EXPANSION=null;

    //------------------------------------------------------------------------------------------------------------------------
    //METODY PODSTAWOWE W PRACY Z BAZĄ DANYCH

    protected void createDB(Connection conn) throws SQLException, IOException {
        System.out.println("Wygląda na to, że nie masz utworzonej wcześniej bazy danych, zaraz coś na to poradzimy :)");
        System.out.println("Tworzenie bazy danych i jej struktury...");

        ScriptRunner runner = new ScriptRunner(conn, false, false);
        String file = System.getProperty("user.dir")+ File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"init.sql";
        runner.runScript(new BufferedReader(new FileReader(file)));
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


    public static void printAllCardsDB(Connection conn) throws SQLException {
        String sql = "select cards.uploaded_date as \"data pobrania karty\", cards.card_name as \"nazwa karty\", artists.name as \"ilustrator\", expansions.expansion_name as \"dodatek\", cards_expansion_connection.price as 'cena' from cards inner join cards_artists_connection on cards.id_card = cards_artists_connection.id_card inner join artists on cards_artists_connection.id_artist = artists.id_artist inner join cards_expansion_connection on cards.id_card = cards_expansion_connection.id_card inner join expansions on cards_expansion_connection.id_expansion = expansions.id_expansion;";
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery(sql);
        while(results.next()){
            System.out.println(results.getDate(1)+"|"+results.getString(2)+"|"+results.getString(3)+"|"+results.getString(4)+"|"+results.getBigDecimal(5));
        }
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


    public static void insertScrapedData(String cardName, String cardImage, String manaCost, int cmc, int cardNumber, String cardType, String rarity, String power, String toughness, String artists, String price) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();

        stmt.executeUpdate("use mtg;");
        String sql = "{call insertData(?, ? , ? , ?, ? , ? , ? , ? , ? ,? , ? , ?)}";
        BigDecimal priceBig;
        Double temp;


        try{ temp = Double.parseDouble(price);}
        catch(NullPointerException e){ temp= null; }


        try{ priceBig = BigDecimal.valueOf(temp); }
        catch(NullPointerException e){ priceBig = null; }


        CallableStatement insert = conn.prepareCall(sql);


        insert.setString(1,cardName);
        insert.setString(2,cardImage);
        insert.setString(3,manaCost);
        insert.setInt(4,cmc);
        insert.setInt(5,cardNumber);
        insert.setString(6,cardType);
        insert.setString(7,rarity);
        insert.setString(8,power);
        insert.setString(9,toughness);
        insert.setString(10,artists);
        insert.setBigDecimal(11, priceBig);
        insert.setInt(12,LAST_INSERTED_ID_EXPANSION);

        insert.execute();
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
