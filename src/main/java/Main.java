import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main{

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException, InterruptedException {

        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie do karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();

        DB database = null;
        try{
            database = new DB();
        } catch (CommunicationsException e){
            System.out.println("prosze włączyć xampa, nie można nawiązać połączenia");
        }

        String continueInput = "";

        while(!continueInput.equals("EXIT")){
            assert database != null;
            if(!database.getHasExpansionsInDB()){
                //obowiązkowy import dodatków
                database.printExpansions(database.legalSets);
                System.out.println("Ponieważ wykryto pustą bazę danych żeby móc z niej skorzystać musisz zaimportować najpierw jakieś karty");
                System.out.println("Powyżej wyświetliły się wszystkie dostępne dodatki, z których karty możesz pobrać");
                System.out.println("Wpisz ich dokładne nazwy poniżej po przecinku np. Amonkhet,Welcome Deck 2016");
                Scanner scan = new Scanner(System.in);

                database.getGivenExpansions(scan.nextLine());
                database.setHasExpansionsInDB(true);

                System.out.println(new String(new char[50]).replace("\0", "\r\n"));
                System.out.println("Aby kontynuowac użytkowanie programu wpisz cokolwiek");
                System.out.println("Jeżeli chcesz wyjść wpisz EXIT");
                continueInput = scan.nextLine();

            }
            else{

                System.out.println(new String(new char[50]).replace("\0", "\r\n"));
                System.out.println("Masz utworzoną bazę danych, wpisz numer akcji do wykonania");
                System.out.println();
                System.out.println("1. Zaimportuj nowy dodatek do istniejącej bazy");
                System.out.println("2. Zaktualizuj ceny wszystkich kart w bazie");
                System.out.println("3. Wyświetl wszystkie pobrane karty");
                Scanner scan = new Scanner(System.in);

                String userChoice = scan.nextLine();

                if(userChoice.equals("1")){
                    database.printExpansions(database.legalSets);
                    System.out.println("Powyżej wyświetliły się wszystkie dostępne dodatki, z których karty możesz pobrać");
                    System.out.println("Wpisz ich dokładne nazwy poniżej po przecinku np. Amonkhet,Welcome Deck 2016");
                    database.getGivenExpansions(scan.nextLine());
                }
                else if(userChoice.contains("2")){
                    System.out.println(new String(new char[50]).replace("\0", "\r\n"));
                    database.updatePrices();
                }
                else if(userChoice.contains("3")){
                    System.out.println(new String(new char[50]).replace("\0", "\r\n"));
                    database.printAllCards();
                }


                System.out.println();
                System.out.println("Wpisano niepoprawny wybór lub zakończono zadaną operację");
                System.out.println("Aby kontynuować użytkowanie programu wpisz cokolwiek");
                System.out.println("Jeżeli chcesz wyjść wpisz EXIT");
                continueInput = scan.nextLine();
            }
        }

    }
}
