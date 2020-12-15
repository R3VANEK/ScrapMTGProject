import java.io.IOException;
import java.sql.SQLException;

public class MainTest {

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie do karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();

        DB database = new DB();

        if(database.freshlyCreated){
            //obowiązkowy import dodatków
            System.out.println("pusta baza danych");
        }
        else{
            //tutaj albo zaimportowanie nowego dodatku albo zaktualizowanie cen albo jeżeli się uda wpisywanie selektów
            System.out.println("istniała wcześniej");
        }
    }
}
