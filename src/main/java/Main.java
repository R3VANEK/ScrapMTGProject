import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main{

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {

        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie do karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();

        DB database = new DB();


        //nieważne że została dopiero utworzona, ważne czy wgl ma w sobie jakieś dodatki
        if(!database.hasExpansionsInDB){
            //obowiązkowy import dodatków
            database.printExpansions(database.legalSets);
            System.out.println("Ponieważ wykryto pustą bazę danych żeby móc z niej skorzystać musisz zaimportować najpierw jakieś karty");
            System.out.println("Powyżej wyświetliły się wszystkie dostępne dodatki, z których karty możesz pobrać");
            System.out.println("Wpisz ich dokładne nazwy poniżej po przecinku np. Amonkhet,Welcome Deck 2016");
            Scanner scan = new Scanner(System.in);
            scan.nextLine();

        }
        else{
            //tutaj albo zaimportowanie nowego dodatku albo zaktualizowanie cen albo jeżeli się uda wpisywanie selektów
            System.out.println("istniała wcześniej bo ma zaimportowane dodatki");
        }
    }
}
