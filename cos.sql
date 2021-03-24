delimiter //    
                        create procedure insertData(

                            IN card_name1 varchar(60), IN card_image1 varchar(150), 
                            IN mana_cost1 varchar(9),IN converted_mana_cost1 Integer, 
                            IN card_number1 Integer, IN card_type1 varchar(40),IN rarity1 varchar(11),
                             IN power1 varchar(4), IN toughness1 varchar(4),IN artist_name varchar(255),
                              price1 Decimal, IN id_expansion1 INTEGER
                              )
                            begin
                                start transaction;
                                
                       
                       
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