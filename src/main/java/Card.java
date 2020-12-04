public class Card {

    public enum cardTypes{
        Creature, Enchantment, Artifact,
        Sorcery, Instant, Land, Conspiracy,
        Phenomenon, Plane, Planeswalker, Scheme, Tribal, Vanguard
    }

    private String cardName;
    private String manaCost; //10GG
    private String cardType;
    private String imageLink;
    private String rarity;
    private String artist;
    private String expansion;
    private int convertedManaCost;
    private int cardNumber;
    private int power;
    private int toughness;
    private float price;



    public String toString(){
        System.out.println("reprezentacja obiektu jako wartości do polecenia insert w sqlu");
        return "";
    }
}
