import com.mysql.cj.protocol.Resultset;

import java.sql.*;


public abstract class DBConnect implements Credentials, Commands{

    public static void createDB() throws ClassNotFoundException, SQLException {

        Class.forName(JDBC_DRIVER);
        
        System.out.println("Connecting to database...");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

        System.out.println("Creating database...");
        Statement stmt = conn.createStatement();

        stmt.executeUpdate(sqlCreateDB);
        System.out.println("Database created successfully...");


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
