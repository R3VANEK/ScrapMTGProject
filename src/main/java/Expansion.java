import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class Expansion {

    private ArrayList<Card> cards;
    private String expansionName;
    private static ArrayList<String> legalSets = new ArrayList<>();

    public static void printExpansions() throws IOException {

        //teoretycznie nie łapie przypadku kiedy ktoś ma wszystkie dodatki zaimportowane
        //trzeba potem dodać drugiego ifa który zczytuje ekspansje z bazy danych

        if(legalSets.isEmpty() ){
            ArrayList<String> temp = new ArrayList<>();
            System.out.println("--------------------------------------------------------");
            Document doc = Jsoup.connect("https://gatherer.wizards.com/Pages/Default.aspx").get();
            Elements setTags = doc.select("select[id=ctl00_ctl00_MainContent_Content_SearchControls_setAddText] > option[value]:not([value=\"\"])");
            for(Element option : setTags){
                System.out.println(option.html());
                temp.add(option.html());
            }
            legalSets = temp;
        }
        else{
            System.out.println("--------------------------------------------------------");
            for(String expansion : legalSets){
                System.out.println(expansion);
            }
        }
        System.out.println("--------------------------------------------------------");

    }

    public Expansion(String expansionName) throws IOException {

        this.expansionName = expansionName;
        if(!legalSets.contains(expansionName)){
            throw new IllegalArgumentException("Wpisano złą nazwę zestawu, pomijanie dodawania zestawu \""+expansionName+"\"");
        }

        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        int page = 0;
        Document mainSite = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page="+ page +"&output=compact&set=["+this.expansionName+"]").get();
        Elements cardsCompact = mainSite.select(".cardItem");

        int numberOfCards = Integer.parseInt(mainSite.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay").html().split("\\(")[1].replace("(","").replace(")",""));
        int cardIndex = 0;

        System.out.println("Importowanie kart z dodatku "+this.expansionName+" trwa...");


        for(int i = 1; i<numberOfCards+1;i++){
            try{
                System.out.print(i+"/"+numberOfCards+"\r");

                insertCardData(cardsCompact,expansionName,cardIndex);
                cardIndex+=1;
            }
            catch(IndexOutOfBoundsException | SQLException | ClassNotFoundException e){
                e.getMessage();
                page+=1;
                // haha specjalne znaczki go brrrrr Aether
                mainSite = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page="+page+"&output=compact&set=["+expansionName+"]").get();
                cardsCompact = mainSite.select(".cardItem");
                cardIndex = 0;
                i-=1;
            }
        }
        legalSets.remove(expansionName);
    }


    public static StringBuilder getCard(Elements cardsCompact, String expansionName, int cardIndex) throws IOException, SQLException, ClassNotFoundException {

        StringBuilder cardString = new StringBuilder();
        cardString.append("(");
        Element compactCardDataRow = cardsCompact.get(cardIndex);

        //pobieranie nazwy karty
        String cardName=compactCardDataRow.select(".name.top>a").html();
        cardString.append(cardName).append(",");

        Elements manaCosts = compactCardDataRow.select(".mana.top > img");
        int cmc = 0;


        //pobieranie kosztów many karty
        //jeżeli dane przejdą w tego ifa, to znaczy, że karta jest landem, a landy nie mają kosztu rzucenia
        if(manaCosts.isEmpty()){
            cardString.append("0");
        }
        else{
            for (Element img : manaCosts) {
                if (!StringUtil.isNumeric(img.attr("alt"))) {
                    cardString.append(img.attr("alt").charAt(0));
                    cmc += 1;
                } else {
                    cardString.append(img.attr("alt"));
                    cmc += Integer.parseInt(img.attr("alt"));
                }
            }
        }
        cardString.append(",").append(cmc).append(",");


        //pobieranie typu karty
        String type;
        if(compactCardDataRow.select(".type.top").html().contains("-")){
            String[] temp = compactCardDataRow.select(".type.top").html().split("\\s+");
            type=temp[0];
            cardString.append(type).append(",");

        }
        else{
            type=compactCardDataRow.select(".type.top").html();
            cardString.append(type).append(",");
        }

        //pobieranie statystyk karty, jeżeli jest to kreatura, to ma siłę i wytrzymałość
        //w przeciwnym razie obie te wartości wpisuje się tu jako 0
        Elements stats = compactCardDataRow.select(".numerical.top");
        if(type.contains("Creature")){
            cardString.append(stats.get(0).html()).append(",").append(stats.get(1).html()).append(",");
        }
        else{
            cardString.append("0").append(",").append("0").append(",");
        }


        //pobieranie rzadkości karty
        String href = compactCardDataRow.select(".name.top>a").attr("abs:href");
        Document cardDataDetailed = Jsoup.connect(href).get();
        Elements rarityElements = cardDataDetailed.select("div.value>span");
        cardString.append(rarityElements.get(1).html()).append(",");

        //pobieranie artysty i numeru karty

        cardString.append(cardDataDetailed.select("[id$=\"numberRow\"]>div.value").get(0).html().replace("a","")).append(",");
        cardString.append(cardDataDetailed.select("[id$=\"ArtistCredit\"]>a").get(0).html()).append(",");
        DBConnect.insertArtist(cardDataDetailed.select("[id$=\"ArtistCredit\"]>a").get(0).html());
        //do tego momentu jest ok


        //tworzenie linku do cardmarketu, strony z cenami kart
        String cardMarketUrl;
        String cardNameToTest = cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay").html();
        var replace = cardNameToTest.replace(' ', '-').replace('\'', '-').replace("//", "-");
        cardMarketUrl = (cardNameToTest.contains("//")) ? cardNameToTest.replace(" ","").replace("//","-") : replace;

        //pobieranie ceny karty
        Document cardMarket = Jsoup.connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardMarketUrl).get();
        Elements dd = cardMarket.select(".col-6");
        String price = null;
        for(Element single_dd : dd){
            if(single_dd.text().contains("€")){
                price = single_dd.html().replace("€", "").replace(",",".").replace(" ","");
                break;
            }
        }
        cardString.append(price).append(",");


        //pobieranie url do obrazka karty
        String imageUrl = cardDataDetailed.select("img[id$=\"cardImage\"]").get(0).attr("abs:src");
        cardString.append(imageUrl).append(")");

        return cardString;
    }











    private static void insertCardData(Elements cardsCompact, String expansionName, int cardIndex) throws IOException, SQLException, ClassNotFoundException {
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
        cardMarketUrl = (cardNameToTest.contains("//")) ? cardNameToTest.replace(" ","").replace("//","-") : replace;

        //pobieranie ceny karty
        Document cardMarket = Jsoup.connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardMarketUrl).get();
        Elements dd = cardMarket.select(".col-6");
        for(Element single_dd : dd){
            if(single_dd.text().contains("€")){
                price = single_dd.html().replace("€", "").replace(",",".").replace(" ","");
                break;
            }
        }

        cardImage = cardDataDetailed.select("img[id$=\"cardImage\"]").get(0).attr("abs:src");





        //wstawianie fetchowanych danych do odpowiednich tabel w bazie danych
        //normalnie skleiłbym wszystkie dane kart z pojedyńczego dodatku w jednego inserta
        //ale uwzględniając relacyjną budowę bazy danych było koniecznie
        //żeby przy każdym takim zestawie danych robić parę insertów
        //inaczej nie dałoby się odwzorować odpowiednich powiązań między tabelami
        DBConnect.insertCard(cardName,cardImage,manaCost,cmc,cardNumber,cardType,rarity,power,toughness);
        DBConnect.insertArtist(artists);
        DBConnect.insertCardExpansionConnection(expansionName,price);
        DBConnect.insertCardArtistsConnection();
    }
}
