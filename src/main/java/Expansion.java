import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Expansion {

    private ArrayList<Card> cards;
    private String expansionName;
    private static ArrayList<String> legalSets;

    public static void printExpansions() throws IOException {
        ArrayList<String> temp = new ArrayList<>();
        System.out.println("--------------------------------------------------------");
        Document doc = Jsoup.connect("https://gatherer.wizards.com/Pages/Default.aspx").get();
        Elements setTags = doc.select("select[id=ctl00_ctl00_MainContent_Content_SearchControls_setAddText] > option[value]:not([value=\"\"])");
        for(Element option : setTags){
            System.out.println(option.html());
            temp.add(option.html());
        }
        legalSets = temp;
        System.out.println("--------------------------------------------------------");
    }

    public Expansion(String expansionName) throws IOException {

        if(!legalSets.contains(expansionName)){
            throw new IllegalArgumentException("Wpisano złą nazwę zestawu, pomijanie dodawania zestawu \""+expansionName+"\"");
        }

        System.out.println(new String(new char[50]).replace("\0", "\r\n"));

        this.expansionName = expansionName;

        Document doc1 = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page=0&output=compact&set=["+this.expansionName+"]").get();
        int numberOfCards = Integer.parseInt(doc1.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay").html().split("\\(")[1].replace("(","").replace(")",""));


        System.out.println("Importowanie kart z dodatku "+this.expansionName+" trwa...");
        for(int i = 1; i<numberOfCards+1;i++){
            System.out.print(i+"/"+numberOfCards+"\r");
            System.out.println(getCard(doc1,expansionName,i));


        }
    }
    public static StringBuilder getCard(Document doc1, String expansionName, int i) throws IOException {
        Elements cardsCompact = doc1.select(".cardItem");
        StringBuilder cardString = new StringBuilder();
        String cardName=cardsCompact.get(i).select(".name.top>a").html();
        cardString.append(cardName).append(" ");



        Elements manaCosts =cardsCompact.get(i).select(".mana.top > img");
        int cmc = 0;
        for(Element img : manaCosts){
            if(!StringUtil.isNumeric(img.attr("alt"))){
                cardString.append(img.attr("alt").charAt(0));
                cmc+=1;
            }
            else{
                cardString.append(img.attr("alt"));
                cmc+=Integer.parseInt(img.attr("alt"));
            }
        }
        cardString.append(cmc);

        if(cardsCompact.get(i).select(".type.top").html().contains(" ")){
            cardString.append(cardsCompact.get(i).select(".type.top").html().split("\\s+")[0]);
        }
        else{
            cardString.append(cardsCompact.get(i).select(".type.top").html());
        }
        Elements stats = cardsCompact.get(i).select(".numerical.top");
        cardString.append(stats.get(0).html());
        cardString.append(stats.get(1).html());


        String href = cardsCompact.get(i).select(".name.top>a").attr("abs:href");
        Document cardDataDetailed = Jsoup.connect(href).get();
        Elements rarityElements = cardDataDetailed.select("div.value>span");
        cardString.append(" ").append(rarityElements.get(1).html());
        //dodanie artysty i numeru karty
        cardString.append(" ").append(cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_CardNumberValue").html());
        cardString.append(" ").append(cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ArtistCredit>a").html());

        String cardNameUrl;
        String cardNameToTest = cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay").html();
        if (cardNameToTest.contains("//")) {
            cardNameUrl = cardNameToTest.replace(" ","").replace("//","-");
        }
        else{
            cardNameUrl = cardNameToTest.replace(' ','-').replace('\'','-').replace("//","-");
        }


        //pobieranie ceny

        Document cardMarket = Jsoup.connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardNameUrl).get();
        Elements dd = cardMarket.select(".col-6");
        int indexOfPriceElement = 0;
        for(Element single_dd : dd){
            if(single_dd.text().contains("From")){
                indexOfPriceElement = dd.indexOf(single_dd)+1;
                break;
            }
        }
        String price = dd.get(indexOfPriceElement).html().replace("€", "").replace(",",".");
        cardString.append(" ").append(price);


        //pobieranie obrazka
        String imageUrl = cardMarket.select("div.image.card-image.is-magic>img.is-front").get(1).attr("src");
        cardString.append(" ").append(imageUrl);

        return cardString;
    }


}
