import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie karcianki Magic The Gathering!");
        //Sets.getSets();

        System.out.println();
        DBConnect.checkDB();
    }
}
