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


                INSERT INTO users(login,password) values ('GraczTrona123','password123');



                delimiter //
                                        create procedure insertData(IN card_name1 varchar(60), IN card_image1 varchar(150), IN mana_cost1 varchar(9),IN converted_mana_cost1 Integer, IN card_number1 Integer, IN card_type1 varchar(40),IN rarity1 varchar(11), IN power1 varchar(4), IN toughness1 varchar(4),IN artist_name varchar(255), IN price1 Decimal, IN id_expansion1 INTEGER)
                                            begin
                                                start transaction;
                                                set transaction isolation level serializable;


                                                    insert into cards(card_name, card_image, mana_cost, converted_mana_cost, card_number, card_type, rarity, power, toughness)
                                                    values(card_name1,card_image1,mana_cost1, converted_mana_cost1, card_number1, card_type1, rarity1, power1, toughness1);


                                                    set @card_id = (select max(id_card) from cards);
                                                    set @ampLocation = Locate("&", artist_name);

                                                    if @ampLocation <> 0 then

                                                        set @firstArtist = substring(artist_name ,1 ,@ampLocation-1);
                                                        set @secondArtist = substring(artist_name, @ampLocation+1, length(@trimmed));
                                                        insert ignore into artists(name) values (@firstArtist), (@secondArtist);

                                                        set @firstArtistID = (select max(id_artist)-1 from artists);
                                                        set @secondArtistID = (select max(id_artist) from artists);

                                                        insert into cards_artist_connection(id_card, id_artist) values
                                                        (@id_card, @firstArtistID), (@id_card, @secondArtistID);

                                                    else

                                                        insert ignore into artists(name) values(artist_name);
                                                        set @artist_id = (select max(id_artist) from artists);
                                                        insert into cards_artists_connection(id_card,id_artist) values(@card_id, @artist_id);

                                                    end if;


                                                    insert into cards_expansion_connection(id_card, price, id_expansion) values
                                                    (@card_id, price1, id_expansion1);

                                                commit;
                                           end//

     COMMIT;