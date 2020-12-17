// NIEZMIENNE INSTRUKCJE SQL
// UŻYWANE PRZY TWORZENIU NOWEJ BAZY DANYCH
// DODANE DO INTERFEJSU ŻEBY NIE ZAŚMIECAĆ KLASY DBConnect
// WSZYSTKIE ATRYBUTY INTERFEJSÓW SĄ DOMYŚLNIE USTAWIONE
// NA STATIC I FINAL WIĘC NIE POWTARZAM TUTAJ TYCH MODYFIKATORÓW


public interface Commands {


    String sqlCreateDB = "CREATE DATABASE mtg";


    String sqlCards = "CREATE TABLE cards"+
            "(id_card INTEGER NOT NULL AUTO_INCREMENT,"+
            "card_name VARCHAR(60) NOT NULL UNIQUE,"+
            "card_image VARCHAR(150) NOT NULL UNIQUE,"+
            "mana_cost VARCHAR(9) NOT NULL,"+
            "converted_mana_cost INTEGER NOT NULL,"+
            "card_number INTEGER NOT NULL,"+
            "card_type VARCHAR(40) NOT NULL,"+
            "rarity VARCHAR(11) NOT NULL,"+
            "power VARCHAR(4) NOT NULL,"+
            "toughness VARCHAR(4) NOT NULL,"+
            "PRIMARY KEY ( id_card ))";

    String sqlExpansion = "CREATE TABLE expansions"+
            "(id_expansion INTEGER NOT NULL AUTO_INCREMENT,"+
            "expansion_name VARCHAR(70) NOT NULL,"+
            "PRIMARY KEY ( id_expansion ))";

    String sqlArtists = "CREATE TABLE artists"+
                                        "(id_artist INTEGER NOT NULL AUTO_INCREMENT,"+
                                        "name VARCHAR(60) NOT NULL UNIQUE,"+
                                        "PRIMARY KEY ( id_artist ))";

    String sqlCardsExpansionConnection = "CREATE TABLE cards_expansion_connection"+
            "(id_card INTEGER,"+ // FOREIGN KEY
            "price DECIMAL(10,2),"+ //karty mogą mieć różne ceny w zalezności od dodatku
            "id_expansion INTEGER)"; //FOREIGN KEY
            //"FOREIGN KEY (id_card) REFERENCES cards(id_card),"+
            //"FOREIGN KEY (id_expansion) REFERENCES expansions(id_epxanison))";

    String sqlCardsArtistsConnection = "CREATE TABLE cards_artists_connection"+
                                        "(id_card INTEGER," + //FOREIGN KEY
                                        "id_artist INTEGER)"; //FOREIGN KEY
                                        //"FOREIGN KEY (id_card) REFERENCES cards(id_card),"+
                                        //"FOREIGN KEY (id_artist) REFERENCES artists(id_artist))";


    String sqlCardsExpansionConnection_FK1 = "ALTER TABLE cards_expansion_connection ADD CONSTRAINT cards_expansion_connection_FK1 FOREIGN KEY (id_card) REFERENCES cards(id_card)";
    String sqlCardsExpansionConnection_FK2 = "ALTER TABLE cards_expansion_connection ADD CONSTRAINT cards_expansion_connection_FK2 FOREIGN KEY (id_expansion) REFERENCES expansions(id_expansion)";

    String sqlCardsArtistsConnection_FK1 = "ALTER TABLE cards_artists_connection ADD CONSTRAINT cards_artists_connection_FK1 FOREIGN KEY (id_card) REFERENCES cards(id_card)";
    String getSqlCardsArtistsConnection_FK2 = "ALTER TABLE cards_artists_connection ADD CONSTRAINT cards_artists_connection_FK2 FOREIGN KEY (id_artist) REFERENCES artists(id_artist)";


    String[] sqlStructure =  {sqlCards, sqlExpansion,sqlArtists,sqlCardsArtistsConnection,sqlCardsExpansionConnection,sqlCardsExpansionConnection_FK1,sqlCardsExpansionConnection_FK2,sqlCardsArtistsConnection_FK1,getSqlCardsArtistsConnection_FK2};


}
