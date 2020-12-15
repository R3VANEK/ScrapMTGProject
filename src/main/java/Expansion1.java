import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public interface Expansion1 {

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
    }
}
