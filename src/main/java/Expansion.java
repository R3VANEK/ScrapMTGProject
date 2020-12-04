import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
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

        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        System.out.println("Importowanie kart z dodatku "+this.expansionName+" trwa...");
        for(int i = 1; i<numberOfCards+1;i++){
            System.out.print(i+"/"+numberOfCards+"\r");
            Thread.sleep(100);
        }
    }


}
