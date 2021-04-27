import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ScrapingAPI {



    // funkcja zwracająca ArrayListę nazw wszystkich dodatków potrzebnych do zawołań API
    default ArrayList<String> getNamesOfAllExpansions1() throws IOException {

        URL expansionsListUrl = new URL("https://api.scryfall.com/sets");
        HttpURLConnection conn = (HttpURLConnection) expansionsListUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        }

        StringBuilder inline = new StringBuilder();
        Scanner scanner = new Scanner(expansionsListUrl.openStream());

        while (scanner.hasNext()) {
            inline.append(scanner.nextLine());
        }
        scanner.close();

        JsonObject jsonObject = new JsonParser().parse(String.valueOf(inline)).getAsJsonObject();
        JsonArray jsonSets = (JsonArray) jsonObject.get("data");

        ArrayList<String> responseSets = new ArrayList<>();

        for(int i = 0; i < jsonSets.size(); i+=1){
            JsonObject tempObj = (JsonObject) jsonSets.get(i);
            responseSets.add(String.valueOf(tempObj.get("name")));
        }

        return responseSets;
    }

    // funkcja zwracająca ArrayListę nazw dodatków obecnych w bazie danych
    default ArrayList<String> getNamesOfAllExpansions1(Statement stmt) throws SQLException {

        ArrayList<String> temp = new ArrayList<>();
        stmt.executeUpdate("use mtg;");
        ResultSet expansions = stmt.executeQuery("select expansion_name from expansions");
        while(expansions.next()){
            temp.add(expansions.getString("expansion_name"));
        }
        return temp;
    }




    default void fetchCardsFromExpansion(String givenExpansionName) throws IOException, SQLException, ClassNotFoundException {


        // TODO: może jakaś mała funkcyjka, która robi ten bloczek kodu?
        String fixedName = givenExpansionName.replace(" ", "_");
        URL expansionsListUrl = new URL("https://api.scryfall.com/cards/search?q=set%3A"+fixedName);
        HttpURLConnection conn = (HttpURLConnection) expansionsListUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responsecode = conn.getResponseCode();
        if (responsecode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responsecode);
        }

        StringBuilder inline = new StringBuilder();
        Scanner scanner = new Scanner(expansionsListUrl.openStream());

        while (scanner.hasNext()) {
            inline.append(scanner.nextLine());
        }
        scanner.close();

        JsonObject jsonObject = new JsonParser().parse(String.valueOf(inline)).getAsJsonObject();
        JsonArray cardsData = (JsonArray) jsonObject.get("data");


        int numberOfCardsToFetch = cardsData.size();
        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        System.out.println("Import kart z dodatku " + givenExpansionName);

        for(int i = 0; i<numberOfCardsToFetch; i+=1){

            String cardName, cardImage, manaCost, artists, power, toughness, price, rarity, cardType;
            int cardNumber, cmc;
            JsonObject tempObj = (JsonObject) cardsData.get(i);
            Pattern pattern = Pattern.compile("Creature|Instant|Sorcery|Artifact|Enchantment|Land|Planeswalker");


            // mdfc
            if(tempObj.get("layout").getAsString().equals("modal_dfc")){
                JsonArray cardFaces = (JsonArray) tempObj.get("card_faces");
                cardImage  = cardFaces.get(0).getAsJsonObject().get("image_uris").getAsJsonArray().get(2).getAsString().replace("\"","");
                manaCost = cardFaces.get(0).getAsJsonObject().get("mana_cost") + " // " + cardFaces.get(1).getAsJsonObject().get("mana_cost");
                power = cardFaces.get(0).getAsJsonObject().get("power").getAsString();
                toughness = cardFaces.get(0).getAsJsonObject().get("toughness").getAsString();

                // TODO: To nie dokońca poprawnie działa z mdfc, zajmij sie tym, może reusults() ?
                Matcher matcher = pattern.matcher(tempObj.get("type_line").getAsString());
                boolean matchFound = matcher.find();
                cardType = (matchFound) ? matcher.group() : null;
                //matcher = pattern.matcher(tempObj.get("type_line").getAsString());

            }
            else{
                cardImage = String.valueOf(tempObj.get("image_uris").getAsJsonObject().get("normal")).replace("\"","");
                manaCost  = tempObj.get("mana_cost").getAsString();
                power = String.valueOf(tempObj.get("power")).replace("\"","");
                toughness = String.valueOf(tempObj.get("toughness")).replace("\"","");


                Matcher matcher = pattern.matcher(tempObj.get("type_line").getAsString());
                boolean matchFound = matcher.find();
                cardType = (matchFound) ? matcher.group() : null;
            }



            cardNumber = tempObj.get("cardmarket_id").getAsInt();
            cmc = tempObj.get("cmc").getAsInt();

            artists = tempObj.get("artist").getAsString();
            rarity = tempObj.get("rarity").getAsString();

            //czasami w api scryfallu nie ma ceny w euro :/
            try{
                price = tempObj.get("prices").getAsJsonObject().get("eur").getAsString();
            } catch(UnsupportedOperationException e){
                price = null;
            }

            cardName = tempObj.get("name").getAsString();

            DBConnect.insertScrapedData(cardName, cardImage,manaCost,cmc,cardNumber,cardType,rarity,power,toughness,artists,price);
            System.out.print(i+"/"+numberOfCardsToFetch+"\r");
        }

    }



}
