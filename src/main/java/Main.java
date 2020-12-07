import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, InterruptedException {
        /*System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();
        if(DBConnect.checkDB()){
            System.out.println("ok jest baza");
        }
        else{
            System.out.println("Hm, wygląda na to, że nie masz utworzonej bazy danych");
            System.out.println("to jest niezbędne do działania programu");
            System.out.println("poniżej wyświetlą się wszystkie dostępne zestawy kart");
            System.out.println("Wybierz te, które chcesz zaimportować do swojej bazy danych");
            System.out.println();
            System.out.println("( Wpisz nazwę dodatków po przecinku np. Amonkhet,Dominaria żeby importować wybrane zestawy )");
            System.out.println();

            Expansion.printExpansions();

            System.out.println("Jakie zestawy chcesz zaimportować? : ");
            Scanner scan = new Scanner(System.in);
            String chosenSets = scan.nextLine();
            ArrayList<String> setsArray = new ArrayList<>(Arrays.asList(chosenSets.split(",")));

            for(String set : setsArray){
                try{
                    Expansion tempObj = new Expansion(set);
                } catch(IllegalArgumentException | InterruptedException e){
                    System.out.println(e.getMessage());
                    continue;
                }


            }



        }*/
        Expansion.printExpansions();
        //nie zapomnij potem usuwać tego obiektu i aktualizoać listę leglnych dodatków po zaimportowaniu zestawu
        Expansion tempObj = new Expansion("Amonkhet");


    }
}
