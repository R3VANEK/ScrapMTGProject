import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MTGAssistant implements ScrapingAPI{


    private DB1 database;


    public MTGAssistant() throws SQLException, IOException, ClassNotFoundException {

        try{ database = new DB1(this.getNamesOfAllExpansions1()); }
        catch (CommunicationsException e){ System.out.println("prosze włączyć xampa, nie można nawiązać połączenia"); }
        System.out.println();

    }


    public void uploadExpansions() throws SQLException, ClassNotFoundException, IOException {

        for(String set : database.legalSets){
            System.out.println(set);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println("Powyżej wyświetliły się nazwy wszystkich dodatków, ktorych nie masz w bazie danych");
        System.out.println("Wpisz nazwy tych dodatków po przecinku, które chcesz zaimportować np. Welcome Deck 2016,Dominaria");
        Scanner scanner = new Scanner(System.in);
        String inputSets = scanner.nextLine();
        System.out.println("----------------------------------------------------------------------------------------------------------");

        ArrayList<String> chosenSets = new ArrayList<>(Arrays.asList(inputSets.split(",")));

        for(String set : chosenSets){

            if(database.legalSets.contains(set)){

                int expansionId = database.insertExpansion(set, database.getConn());
                CardData[] cardsFromExpansion = this.fetchCardsFromExpansion(set);

                for(CardData CardObject : cardsFromExpansion){
                    database.insertScrapedData(database.getConn(),expansionId,CardObject.cardName, CardObject.cardImage, CardObject.manaCost, CardObject.cmc, CardObject.cardNumber, CardObject.cardType, CardObject.rarity,CardObject.power,CardObject.toughness,CardObject.artists,CardObject.priceBig);
                }

                database.legalSets.remove(set);
                System.out.println("Zaimportowano karty z dodatku " + set);
                System.out.println(new String(new char[50]).replace("\0", "\r\n"));

                if(!database.hasExpansionsInDB){
                    database.hasExpansionsInDB = true;
                }
            }
            else{
                System.out.println("Ten dodatek został już kiedyś zaimportowany, pomijanie...");
            }
        }
    }


    public boolean DBHasExpansions(){
        return database.hasExpansionsInDB;
    }



    public void expansionsToJson() throws SQLException, IOException {

        FileWriter fileWriter;
        String path = System.getProperty("user.dir")+ File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator+"data.json";
        try{
            fileWriter = new FileWriter(path);
        }
        catch(IOException e){
            File newFile = new File(path);
            fileWriter = new FileWriter(path);
        }

        fileWriter.write("{\"data\" : [");
        ArrayList<CardData> cardsFromDB =  database.getCardsFromDB(database.getConn());
        for(CardData card : cardsFromDB){
           fileWriter.write(card.toJsonString() +",");
        }
        fileWriter.write("]}");
        fileWriter.close();
    }



}
