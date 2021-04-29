import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Main{

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException{

        System.out.println("--------------------------------------------------------");
        System.out.println("Witamy w twoim asystencie do karcianki Magic The Gathering!");
        System.out.println("--------------------------------------------------------");
        System.out.println();




        MTGAssistant assistant = new MTGAssistant();
        String continueInput = "";
        Scanner continueScanner = new Scanner(System.in);
        Scanner chooseScanner = new Scanner(System.in);

        while(!continueInput.equals("EXIT")){


            if(assistant.DBHasExpansions()){

                System.out.println("Co chcesz zrobić? <wpisz 1 albo 2 >");
                System.out.println("1. Import nowych dodatków do bazy danych");
                System.out.println("2. Eksport wybranych dodatków z bazy danych do jsona");
                String action = chooseScanner.nextLine();

                switch (action) {
                    case "1" -> assistant.uploadExpansions();
                    case "2" -> assistant.expansionsToJson();
                    default -> System.out.println("Wpisano niepoprawny wybór");
                }

            }
            else{
                assistant.uploadExpansions();
            }



            System.out.println(new String(new char[50]).replace("\0", "\r\n"));
            System.out.println("Aby kontynuować użytkowanie programu wpisz cokolwiek");
            System.out.println("Jeżeli chcesz wyjść wpisz EXIT");
            continueInput = continueScanner.nextLine();
        }






    }
}
