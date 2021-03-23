// NIEZMIENNE INSTRUKCJE SQL
// UŻYWANE PRZY TWORZENIU NOWEJ BAZY DANYCH
// DODANE DO INTERFEJSU ŻEBY NIE ZAŚMIECAĆ KLASY DBConnect
// WSZYSTKIE ATRYBUTY INTERFEJSÓW SĄ DOMYŚLNIE USTAWIONE
// NA STATIC I FINAL WIĘC NIE POWTARZAM TUTAJ TYCH MODYFIKATORÓW


public interface Commands {


    String sqlInit = """

                CREATE DATABASE mtg;
                USE mtg;
                
                START TRANSACTION;

                CREATE TABLE cards(
                    id_card INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
                    card_name VARCHAR(60) NOT NULL UNIQUE,
                    card_image VARCHAR(150) NOT NULL UNIQUE,
                    mana_cost VARCHAR(9) NOT NULL,
                    converted_mana_cost INTEGER NOT NULL,
                    card_number INTEGER NOT NULL,
                    card_type VARCHAR(40) NOT NULL,
                    rarity VARCHAR(11) NOT NULL,
                    power VARCHAR(4) NOT NULL,
                    toughness VARCHAR(4) NOT NULL,
                    uploaded_date DATETIME DEFAULT CURRENT_TIMESTAMP
                );


                CREATE TABLE expansions(
                    id_expansion INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
                    expansion_name VARCHAR(70) NOT NULL
                );


                CREATE TABLE artists(
                    id_artist INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
                    name VARCHAR(60) NOT NULL UNIQUE
                );


                CREATE TABLE users(
                    id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
                    login VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL
                );



                CREATE TABLE cards_expansion_connection(
                    id_card INTEGER,
                    price DECIMAL(10,2),
                    id_expansion INTEGER
                );


                CREATE TABLE cards_artists_connection(
                    id_card INTEGER,
                    id_artist INTEGER
                );


                ALTER TABLE cards_expansion_connection ADD CONSTRAINT cards_expansion_connection_FK1 FOREIGN KEY (id_card) REFERENCES cards(id_card);
                ALTER TABLE cards_expansion_connection ADD CONSTRAINT cards_expansion_connection_FK2 FOREIGN KEY (id_expansion) REFERENCES expansions(id_expansion);
                ALTER TABLE cards_artists_connection ADD CONSTRAINT cards_artists_connection_FK1 FOREIGN KEY (id_card) REFERENCES cards(id_card);
                ALTER TABLE cards_artists_connection ADD CONSTRAINT cards_artists_connection_FK2 FOREIGN KEY (id_artist) REFERENCES artists(id_artist);


                COMMIT;

            """;
}
