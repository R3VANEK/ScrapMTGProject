import com.mysql.cj.protocol.Resultset;

import java.sql.*;


public abstract class DBConnect implements Credentials, Commands{

    public static void createDB() throws ClassNotFoundException, SQLException {

        Class.forName(JDBC_DRIVER);
        
        System.out.println("łączenie z xamppem...");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

        System.out.println("tworzenie struktury bazy danych...");
        //PreparedStatement stmt = conn.prepareStatement(sqlCreateDB);
        Statement stmt = conn.createStatement();
        //spróbuj zrobić fora po poleceniach sql w Commands i zmień je na listę
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


}
