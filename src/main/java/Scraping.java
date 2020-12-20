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
import java.util.concurrent.TimeUnit;

public interface Scraping {


    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
    //2 METODY NA POZYSKIWANIE INFORMACJI O DODATKACH
    //CO PRAWDA JEDNA Z NICH WYKORZYSTUJE POŁĄCZENIE Z DB
    //ALE UZNAŁEM, ŻE BARDZIEJ CZYTELNE BĘDZIE POZOSTAWIENIE JEJ TUTAJ
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
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------


    default void printExpansions(ArrayList<String> legalSets){
        System.out.println("---------------------------------------------------------------------");
        for(String expansion : legalSets){
            System.out.println(expansion);
        }
        System.out.println("---------------------------------------------------------------------");
        System.out.println();
    }







    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
    //GŁÓWNA METODA "HUB" DO PĘTLI POZYSKIWANIA INFORMAJCI O KARTACH Z DANEGO DODATKU
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
                TimeUnit.SECONDS.sleep(1);
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
    //POBIERANIE WSZYSTKICH NIEZBEDNYCH INFORMAJCI O POJEDYŃCZEJ AKRCIE, A TAKŻE POD KONIEC WSTAWIANIE INFORMACJI DO DB
    default void getScrapDataCard(Elements cardsCompact, String expansionName, int cardIndex) throws IOException, SQLException, ClassNotFoundException {

        Element compactCardDataRow = cardsCompact.get(cardIndex);
        String cardName, cardType, rarity, artists, cardImage, price, manaCost="", power = "0",toughness = "0";
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


        //pobieranie ceny karty
        //jedyna odilozowana metoda do tego
        //powód : wykorzystuje ten sam kod do uaktualniania cen, więc
        //przydaje sie tez poza tą specyficzną metodą
        price = Scraping.fetchUpdatePrice(cardName,expansionName);



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
        DBConnect.insertCardExpansionConnection(price);
        DBConnect.insertCardArtistsConnection();
    }







    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------
    //METODA DO PONOWNEGO SCRAPOWANIA CENY KARTY, UŻYWANA W UAKTUALNIANIU CEN
    static String fetchUpdatePrice(String cardName, String expansionName) throws IOException {

        //pobieranie odpowiedniego linku do cardMarketu, strony z cenami kart
        String cardMarketUrl,price = null;
        var replace = cardName.replace(' ', '-').replace('\'', '-').replace("//", "-");
        cardMarketUrl = (cardName.contains("//")) ? cardName.replace(" ","").replace("//","-").replace(":","") : replace;

        Document cardMarket = Jsoup
                .connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardMarketUrl)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .get();

        Elements dd = cardMarket.select(".col-6");

        for(Element single_dd : dd){
            if(single_dd.text().contains("€")){
                price = single_dd.html().replace("€", "").replace(",",".").replace(" ","");
                break;
            }
        }
        return price;
    }
}
