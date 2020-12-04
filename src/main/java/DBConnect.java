import com.mysql.cj.protocol.Resultset;

import java.sql.*;


public abstract class DBConnect {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3300/";

    static final String USER = "root";
    static final String PASS = "";

    public static void createDB() throws ClassNotFoundException, SQLException {

        Class.forName("com.mysql.cj.jdbc.Driver");
        
        System.out.println("Connecting to database...");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

        System.out.println("Creating database...");
        Statement stmt = conn.createStatement();


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
