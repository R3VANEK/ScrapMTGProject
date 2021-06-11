# ScrapMTGProject
[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/cc-by.svg)](https://forthebadge.com)

## Table of Contents
* [How it works and what is "MTG"](#How-it-works-and-what-is-"MTG")
* [Functionalites of app](#Functionalites-of-app)
* [Database schema](#Database-schema)
* [Comments and adnotations](#Comments-and-adnotations)
* [Technologies used](#Technologies-used)
* [License](#License)



## How it works and what is "MTG"

This program was constructed for lecture about scraping data, JSON and SQL integration in Java at my high school. Our task was to create an application that uses web scraping (later upgraded by me to API communication for better performance) to go and grab some data, to later store it into JSON or MySQL. I've decided to use data from MTG. This acronym stands for "Magic The Gathering", a quite popular card game similiar to "Pokémon TCG" or "Yu-Gi-Oh!". Why I chose it? Becouse it is my little hobby with friends and the game itself has rich heritage, which translates to multitude of data to work with. For the sake of this program here are things to know about it :

* Cards are released periodically in named sets, for example "Kaladesh" is set containing roughly 264 cards
* Set contains new, never seen cards and some reprints of older cards
* Each card has its own price in real money, it vary from 0,01€ up to 2000€ or higher
* The same card printed in two different set may have different value, for example it may be due to more stylish artwork on it
* Prices of cards are ever-changing and are subject of laws of economy
* Cards are divided into several different types : Creature, Sorcery, Instant, Artifact, Planeswalker, Enchantment and Land


## Functionalites of app

The application creates database called 'mtg' on server and stores on it data. After successfull creating connection to it or maikng databse from scratch if it is your first time using it, on console GUI we have 2 options : 

* **Download all cards data from given set(s)** : We are prompted [legal](#Comments-and-adnotations) sets to choose from. After typing one or more sets name, program retrevies card data such as its name, price, artwork artist, set etc. from [Scryfall API](https://scryfall.com/docs/api). It then stores it in 'mtg' database

* **Export to JSON** : Program transform all data from 'mtg' database into single JSON file named 'data.json' in the project directory 'resources'. This file contains array of objects that represents cards 





## Database schema
![Schema](https://github.com/R3VANEK/ScrapMTGProject/blob/master/DB-schema.png)

Application automatically creates one user named "GraczTrona123" identified by password "password123". It was requirement from teacher reviewing projects








 
## Comments and adnotations


* Term "legal" set is used for sets that are not already imported into database and are accesible through Scryfall API
* Application is not tested for all the cards in history of game. Some cards have for example strange names containing letters such as 'Æ' or have unique properties that cause bugs in saving proccess, however these errors occurs very rarely
* Application was tested only as JetBrains Maven project, currently there isn't any release version of it
* Only cards of type "Creature" have power and toughness attributes, other such as "Instants" have value null in these properties


## Technologies used

* Java
* Maven
* JDBC library
* Gson library
* ScriptRunner.java - credits to Clinton Begin (class for running .sql files directly)
* [Scryfall API](https://scryfall.com/docs/api)


## License

[CC BY](https://creativecommons.org/licenses/by/4.0/legalcode)
