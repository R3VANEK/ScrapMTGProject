import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie do karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();

        //to jest załatwione w klasie DB
        if(DBConnect.checkDB()){
            System.out.println("ok jest baza");

           /* DBConnect.insertCard
                    (
                    "\"Bala Ged Thief\"",
                    "\"https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=197402&type=card\"",
                    "\"3B\"",
                    4,
                    79,
                    "\"Creature — Human Rogue Ally\"",
                    "\"Rare\"",
                    2,
                    2
                    );

            DBConnect.insertArtist("Zoltan Boros &amp; John Doe");
            DBConnect.insertExpansion("\"Zendikar\"");
            DBConnect.insertCardExpansionConnection("\"Zendikar\"", "6.99");
            DBConnect.insertCardArtistsConnection();*/
        }
        else{
            System.out.println("Hm, wygląda na to, że nie masz utworzonej bazy danych");
            System.out.println("to jest niezbędne do działania programu");
            System.out.println("poniżej wyświetlą się wszystkie dostępne zestawy kart");
            System.out.println("Wybierz te, które chcesz zaimportować do swojej bazy danych");
            System.out.println();
            System.out.println("( Wpisz nazwę dodatków po przecinku np. Amonkhet,Dominaria żeby importować wybrane zestawy )");
            System.out.println();

            //DBConnect.createDB();

            DB databaseObject = new DB();

            Expansion.printExpansions();

            System.out.println("Jakie zestawy chcesz zaimportować? : ");
            Scanner scan = new Scanner(System.in);
            String chosenSets = scan.nextLine();
            ArrayList<String> setsArray = new ArrayList<>(Arrays.asList(chosenSets.split(",")));

            for(String set : setsArray){
                DBConnect.insertExpansion(set);
                try{
                    Expansion tempObj = new Expansion(set);
                } catch(IllegalArgumentException | IOException e){
                    System.out.println(e.getMessage());
                }

            }
        }

    }
}
