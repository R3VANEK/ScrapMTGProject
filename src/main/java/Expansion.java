import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public interface Expansion {

    default ArrayList<String> getExpansions(Statement stmt) throws SQLException {

        ArrayList<String> temp = new ArrayList<>();
        stmt.executeUpdate("use mtg;");
        ResultSet expansions = stmt.executeQuery("select expansion_name from expansions");
        while(expansions.next()){
            temp.add(expansions.getString("expansion_name"));
        }
        return temp;
    }

    default ArrayList<String> getExpansions() throws IOException {

        ArrayList<String> temp = new ArrayList<>();
        Document doc = Jsoup.connect("https://gatherer.wizards.com/Pages/Default.aspx").get();
        Elements setTags = doc.select("select[id=ctl00_ctl00_MainContent_Content_SearchControls_setAddText] > option[value]:not([value=\"\"])");
        for(Element option : setTags){
            temp.add(option.html());
        }
        return temp;
    }

    default void printExpansions(ArrayList<String> legalSets){
        System.out.println("---------------------------------------------------------------------");
        for(String expansion : legalSets){
            System.out.println(expansion);
        }
        System.out.println("---------------------------------------------------------------------");
        System.out.println("");
    }


    default void getSingleExpansion(String expansionName, ArrayList<String> legalSets) throws IOException, SQLException, ClassNotFoundException {

        if(!legalSets.contains(expansionName)){
            throw new IllegalArgumentException("Wpisano złą nazwę zestawu, pomijanie dodawania zestawu \""+expansionName+"\"");
        }
        DBConnect.insertExpansion(expansionName);
        //przygotowania
        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        int page = 0, cardIndex = 0;
        Document mainSite = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page="+ page +"&output=compact&set=["+expansionName+"]").get();
        Elements cardsCompact = mainSite.select(".cardItem");
        int numberOfCards = Integer.parseInt(mainSite.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay").html().split("\\(")[1].replace("(","").replace(")",""));


        System.out.println("Importowanie kart z dodatku "+expansionName+" trwa...");
        //główna pętla pobierająca wszystkie karty z jednego dodatku
        for(int i = 1; i<numberOfCards+1;i++){
            try{
                System.out.print(i+"/"+numberOfCards+"\r");
                getScrapDataCard(cardsCompact,expansionName,cardIndex);
                cardIndex+=1;
            }

            //normalny błąd wykorzystywany żeby przechodzić
            //do nowych zakładek scrapowanej strony
            catch(IndexOutOfBoundsException e){
                page+=1;
                mainSite = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page="+page+"&output=compact&set=["+expansionName+"]").get();
                cardsCompact = mainSite.select(".cardItem");
                cardIndex = 0;
                i-=1;

                //oby nigdy nie wywaliło tych błędów
            } catch (IOException | ClassNotFoundException errorFatal){
                errorFatal.getMessage();
            }
        }
    }




    //pobieranie pojedyńczych kart
    default void getScrapDataCard(Elements cardsCompact, String expansionName, int cardIndex) throws IOException, SQLException, ClassNotFoundException {

        Element compactCardDataRow = cardsCompact.get(cardIndex);
        String cardName, cardType, rarity, artists, cardImage, price = null, manaCost="", power = "0",toughness = "0";
        int cardNumber, cmc=0;

        //pobieranie nazwy karty
        cardName=compactCardDataRow.select(".name.top>a").html();
        Elements manaCosts = compactCardDataRow.select(".mana.top > img");

        //pobieranie kosztów many karty
        //jeżeli dane przejdą w tego ifa, to znaczy, że karta jest landem, a landy nie mają kosztu rzucenia
        //wydaje mi się, że takie rozwiązanie jest odrobinkę szybsze od pętli foreach bo w debugowaniu widać
        //że przy każdym powtórzeniu java i tak musi określić pojedyńczy przedmiot
        if(manaCosts.isEmpty()){
            manaCost+="0";
        }
        else{
            for (Element img : manaCosts) {
                if (!StringUtil.isNumeric(img.attr("alt"))) {
                    manaCost += img.attr("alt").charAt(0);
                    cmc += 1;
                } else {
                    manaCost += img.attr("alt");
                    cmc += Integer.parseInt(img.attr("alt"));
                }
            }
        }

        //pobieranie typu karty, np. creature, artefact, sorcery itp.
        if(compactCardDataRow.select(".type.top").html().contains("-")){
            String[] temp = compactCardDataRow.select(".type.top").html().split("\\s+");
            cardType=temp[0];
        }
        else{
            cardType=compactCardDataRow.select(".type.top").html();
        }

        //pobieranie statystyk karty, jeżeli jest to kreatura, to ma siłę i wytrzymałość
        //w przeciwnym razie obie te wartości wpisuje się tu jako 0
        Elements stats = compactCardDataRow.select(".numerical.top");
        if(cardType.contains("Creature")){
            power = stats.get(0).html();
            toughness = stats.get(1).html();
        }

        //pobieranie rzadkości karty np. Common, Uncommon, Rare
        String href = compactCardDataRow.select(".name.top>a").attr("abs:href");
        Document cardDataDetailed = Jsoup.connect(href).get();
        Elements rarityElements = cardDataDetailed.select("div.value>span");
        rarity = rarityElements.get(1).html();


        //pobieranie artysty i numeru karty
        artists = cardDataDetailed.select("[id$=\"ArtistCredit\"]>a").get(0).html();
        cardNumber = Integer.parseInt(cardDataDetailed.select("[id$=\"numberRow\"]>div.value").get(0).html().replace("a",""));
        //DBConnect.insertArtist(cardDataDetailed.select("[id$=\"ArtistCredit\"]>a").get(0).html());


        //pobieranie odpowiedniego linku do cardMarketu, storny z cenami kart
        String cardMarketUrl;
        String cardNameToTest = cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay").html();
        var replace = cardNameToTest.replace(' ', '-').replace('\'', '-').replace("//", "-");
        cardMarketUrl = (cardNameToTest.contains("//")) ? cardNameToTest.replace(" ","").replace("//","-").replace(":","") : replace;


        //pobieranie ceny karty
        Document cardMarket = Jsoup.connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardMarketUrl).get();
        Elements dd = cardMarket.select(".col-6");
        for(Element single_dd : dd){
            if(single_dd.text().contains("€")){
                price = single_dd.html().replace("€", "").replace(",",".").replace(" ","");
                break;
            }
        }

        //pobieranie linku do obrazka karty
        cardImage = cardDataDetailed.select("img[id$=\"cardImage\"]").get(0).attr("abs:src");


        //wstawianie fetchowanych danych do odpowiednich tabel w bazie danych
        //normalnie skleiłbym wszystkie dane kart z pojedyńczego dodatku w jednego inserta
        //ale uwzględniając relacyjną budowę bazy danych było koniecznie
        //żeby przy każdym takim zestawie danych robić parę insertów
        //inaczej nie dałoby się odwzorować odpowiednich powiązań między tabelami
        //w idealnym świecie, wstawiałbym te rekordu trochę bliżej klasy DB
        //ale nie mam na ten moment lepszego pomysłu jak to uporządkować
        DBConnect.insertCard(cardName,cardImage,manaCost,cmc,cardNumber,cardType,rarity,power,toughness);
        DBConnect.insertArtist(artists);
        DBConnect.insertCardExpansionConnection(expansionName,price);
        DBConnect.insertCardArtistsConnection();
    }


}
