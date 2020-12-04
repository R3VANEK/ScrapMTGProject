import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.Scanner;

public abstract class Sets {

    public static void printSets() throws IOException {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        Document doc = Jsoup.connect("https://gatherer.wizards.com/Pages/Default.aspx").get();
        Elements setTags = doc.select("select[id=ctl00_ctl00_MainContent_Content_SearchControls_setAddText] > option[value]:not([value=\"\"])");
        for(Element option : setTags){
            System.out.println(option.html());
        }
        System.out.println("-------------------------------------------");
        System.out.println("Wpisz dokładną nazwę interesującego Cię zestawu : ");
        Scanner scan = new Scanner(System.in);
        String chosenSet = scan.next();
        downloadSets(chosenSet);
    }

    public static void downloadSets(String setName){
        System.out.println(new String(new char[50]).replace("\0", "\r\n"));
        System.out.println("jej");
    }






}
