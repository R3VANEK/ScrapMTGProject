// NIEZMIENNE INSTRUKCJE SQL
// UŻYWANE PRZY TWORZENIU NOWEJ BAZY DANYCH
// DODANE DO INTERFEJSU ŻEBY NIE ZAŚMIECAĆ KLASY DBConnect
// WSZYSTKIE ATRYBUTY INTERFEJSÓW SĄ DOMYŚLNIE USTAWIONE
// NA STATIC I FINAL WIĘC NIE POWTARZAM TUTAJ TYCH MODYFIKATORÓW


public interface Commands {


    String sqlCreateDB = "CREATE DATABASE mtg";

    String sqlCards = "CREATE TABLE cards"+
            "(id_card INTEGER NOT NULL AUTO_INCREMENT,"+
            "card_name VARCHAR(50) NOT NULL UNIQUE,"+
            "card_image VARCHAR(70) NOT NULL UNIQUE,"+
            "mana_cost VARCHAR(7) NOT NULL,"+
            "converted_mana_cost INTEGER NOT NULL,"+
            "id_artist INTEGER NOT NULL,"+ //FOREIGN KEY
            "card_number INTEGER NOT NULL,"+
            "card_type VARCHAR(15) NOT NULL,"+
            "rarity VARCHAR(8) NOT NULL,"+
            "power INTEGER NOT NULL,"+
            "toughness INTEGER NOT NULL,"+
            "price DECIMAL(8,2) NOT NULL,"+
            "PRIMARY KEY ( id_card ))";

    String sqlExpansion = "CREATE TABLE expansions"+
            "(id_expansion INTEGER NOT NULL AUTO_INCREMENT,"+
            "expansion_name VARCHAR(70) NOT NULL,"+
            "PRIMARY KEY ( id_epxansion ))";

    String sqlArtists = "CREATE TABLE artists"+
                                        "(id_artist INTEGER NOT NULL AUTO_INCREMENT,"+
                                        "firstName VARCHAR(20) NOT NULL,"+
                                        "lastName VARCHAR(40) NOT NULL,"+
                                        "PRIMARY KEY ( id_artist ))";

    String sqlCardsExpansionConnection = "CREATE TABLE cards_expansion_connection"+
            "(id_card INTEGER,"+ // FOREIGN KEY
            "id_expansion INTEGER)"; //FOREIGN KEY






    String sqlCardsFK = "ALTER TABLE cards"+
                                    "ADD CONSTRAINT cards_FK FOREIGN KEY (id_artist) REFERENCES artists (id_artist)";

    String sqlCardsExpansionConnection_FK1 = "ALTER TABLE cards_expansion_connection"+
                                                            "ADD CONSTRAINT cards_expansion_connection_FK1 FOREIGN KEY (id_card) REFERENCES cards (id_card)";

    String sqlCardsExpansionConnection_FK2 = "ALTER TABLE cards_expansion_connection"+
            "ADD CONSTRAINT cards_expansion_connection_FK2 FOREIGN KEY (id_expansion) REFERENCES expansions (id_expansion)";


}
