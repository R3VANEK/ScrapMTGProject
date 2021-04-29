import com.google.gson.JsonArray;
import org.json.JSONObject;

import java.math.BigDecimal;

public class CardData {

    public String expansionName;
    public String cardName, cardImage, manaCost, artists, power, toughness, rarity, cardType;
    public int cardNumber, cmc;
    BigDecimal priceBig;





    // konstruktor używany przy pobieraniu danych z API
    public CardData(String cardName, String cardImage, String manaCost, String artists, String power, String toughness, String price, String rarity, String cardType, int cardNumber, int cmc){

        this.cardName = cardName;
        this.cardImage = cardImage;
        this.manaCost = manaCost;
        this.artists = artists;
        this.power = power;
        this.toughness = toughness;
        this.rarity = rarity;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.cmc = cmc;

        Double temp;
        try{ temp = Double.parseDouble(price);}
        catch(NullPointerException e){ temp= null; }

        try{ this.priceBig = BigDecimal.valueOf(temp); }
        catch(NullPointerException e){ this.priceBig = null; }
    }


    //kosntruktor uzywany przy wyciąganiu danych z bazy danych
    public CardData(String expansionName, int cardNumber, String cardType, String cardName, String cardImage, String rarity, String power, String toughness, int cmc, String manaCost, BigDecimal price, String artists){
        this.expansionName = expansionName;
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.cardName = cardName;
        this.cardImage = cardImage;
        this.rarity = rarity;
        this.power = power;
        this.toughness = toughness;
        this.cmc = cmc;
        this.manaCost = manaCost;
        this.priceBig = price;
        this.artists = artists;
    }


    public String toJsonString(){
        return String.format("{\"expansion_name\" : \"%s\", \"card_number\" : %d, \"card_name\" : \"%s\", \"card_image\" : \"%s\", \"card_type\" : \"%s\", \"rarity\" : \"%s\", \"power\" : \"%s\", \"toughness\" : \"%s\", \"cmc\" : %d, \"converted_mana_cost\" : \"%s\", \"price\" : "+ this.priceBig +", \"artists\": \"%s\"}", this.expansionName, this.cardNumber, this.cardName, this.cardImage, this.cardType, this.rarity, this.power, this.toughness, this.cmc, this.manaCost, this.artists);
    }
}
