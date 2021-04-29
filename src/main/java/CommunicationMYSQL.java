import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public interface CommunicationMYSQL {


    default void createDB1(Connection conn) throws SQLException, IOException {


//        System.out.println("Wygląda na to, że nie masz utworzonej wcześniej bazy danych, zaraz coś na to poradzimy :)");
//        System.out.println("Tworzenie bazy danych i jej struktury...");

        ScriptRunner runner = new ScriptRunner(conn, false, false);
        String file = System.getProperty("user.dir")+ File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"init.sql";
        runner.runScript(new BufferedReader(new FileReader(file)));
        //System.out.println("Utworzono bazę danych");
    }


    static boolean checkDB(String DB_URL, String USER, String PASS) throws ClassNotFoundException, SQLException {
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


    // funkcja zwracająca ArrayListę nazw dodatków obecnych w bazie danych
    default ArrayList<String> getNamesOfAllExpansions1(Statement stmt) throws SQLException {

        ArrayList<String> temp = new ArrayList<>();
        stmt.executeUpdate("use mtg;");
        ResultSet expansions = stmt.executeQuery("select expansion_name from expansions");
        while(expansions.next()){
            temp.add(expansions.getString("expansion_name"));
        }
        return temp;
    }


    static int checkLogin(String login, String password, Connection conn) throws SQLException {
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

    default int insertExpansion(String expansionName, Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        String sqlInsert = "INSERT INTO expansions(expansion_name)"+
                "VALUES(\""+expansionName+"\")";
        stmt.executeUpdate(sqlInsert);

        int returnValue = -1;
        ResultSet indexOfExpansion = stmt.executeQuery("SELECT id_expansion FROM expansions WHERE expansion_name=\""+expansionName+"\"");
        while(indexOfExpansion.next()){
            returnValue =  indexOfExpansion.getInt("id_expansion");
        }
        return returnValue;
    }


    default void insertScrapedData(Connection conn, int LAST_INSERTED_ID_EXPANSION, String cardName, String cardImage, String manaCost, int cmc, int cardNumber, String cardType, String rarity, String power, String toughness, String artists, BigDecimal priceBig) throws SQLException, ClassNotFoundException {


        Statement stmt = conn.createStatement();
        stmt.executeUpdate("use mtg;");
        String sql = "{call insertData(?, ? , ? , ?, ? , ? , ? , ? , ? ,? , ? , ?)}";
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


    }

}
