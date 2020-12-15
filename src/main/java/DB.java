import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB extends DBConnect{

    public Statement stmt = null;
    public Connection conn = null;

    public DB() throws ClassNotFoundException, SQLException {

        System.out.println("Łączenie z xampem...");
        Class.forName(JDBC_DRIVER);
        this.conn = DriverManager.getConnection(DB_URL, USER, PASS);
        this.stmt = conn.createStatement();
        this.createDB(this.stmt);
    }

    public void updatePrices(){
        System.out.println("tutaj coś będzie");
    }

    public void getExpansion(){
        System.out.println("tutaj coś będzie");
    }
}
