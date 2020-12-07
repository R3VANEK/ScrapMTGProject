import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

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

    public Expansion(String expansionName) throws InterruptedException, IOException {

        if(!legalSets.contains(expansionName)){
            throw new IllegalArgumentException("Wpisano złą nazwę zestawu, pomijanie dodawania zestawu \""+expansionName+"\"");
        }

        this.expansionName = expansionName;
        Document doc = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?output=compact&set=[\""+this.expansionName+"\"]").get();
        int numberOfCards = Integer.parseInt(doc.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay").html().split("\\(")[1].replace("(","").replace(")",""));

        //SKORZYSTAJ Z API SCRYFALLU ZAMIAST STRONY MTG !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        System.out.println(new String(new char[50]).replace("\0", "\r\n"));

        Document doc1 = Jsoup.connect("https://gatherer.wizards.com/Pages/Search/Default.aspx?page=0&output=compact&set=[%22Amonkhet%22]").get();
        int countCards = 0; // potem to będzie po prostu zmienna i w pętli
        Elements cardsCompact = doc1.select(".cardItem");

        StringBuilder cardString = new StringBuilder();
        String cardName=cardsCompact.get(0).select(".name.top>a").html();

        String cardNameUrl = cardName.replace(' ','-');

        cardString.append(cardName).append(" ");



        Elements manaCosts =cardsCompact.get(0).select(".mana.top > img");
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

        if(cardsCompact.get(0).select(".type.top").html().contains(" ")){
            cardString.append(cardsCompact.get(0).select(".type.top").html().split("\\s+")[0]);
        }
        else{
            cardString.append(cardsCompact.get(0).select(".type.top").html());
        }
        Elements stats = cardsCompact.get(0).select(".numerical.top");
        cardString.append(stats.get(0).html());
        cardString.append(stats.get(1).html());


        String href = cardsCompact.get(0).select(".name.top>a").attr("abs:href");
        Document cardDataDetailed = Jsoup.connect(href).get();
        Elements rarityElements = cardDataDetailed.select("div.value>span");
        cardString.append(" ").append(rarityElements.get(1).html());
        cardString.append(" ").append(cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_CardNumberValue").html());
        cardString.append(" ").append(cardDataDetailed.select("#ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ArtistCredit>a").html());



        //pobieranie ceny
        Document cardMarket = Jsoup.connect("https://www.cardmarket.com/en/Magic/Products/Singles/"+expansionName+"/"+cardNameUrl).get();
        String price = cardMarket.select(".col-6.col-xl-7").get(4).html().replace("€", "").replace(",",".");
        cardString.append(" ").append(price);


        //pobieranie obrazka
        String imageUrl = cardMarket.select("div.image.card-image.is-magic>img.is-front").get(1).attr("src");
        cardString.append(" ").append(imageUrl);
        System.out.println(cardString);


        /*
        System.out.println("Importowanie kart z dodatku "+this.expansionName+" trwa...");
        for(int i = 1; i<numberOfCards+1;i++){
            System.out.print(i+"/"+numberOfCards+"\r");
            Thread.sleep(100);
        }*/
    }


}
