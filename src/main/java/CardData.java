import java.math.BigDecimal;

public class CardData {


    public String cardName, cardImage, manaCost, artists, power, toughness, rarity, cardType;
    public int cardNumber, cmc;
    BigDecimal priceBig;






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



}
