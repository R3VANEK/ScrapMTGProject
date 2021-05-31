# ScrapMTGProject


Dzień dobry, oto mój projekt wykorzystujący scrapowanie stron, aplikacja konsolowa "MTG assistant".


## Nawigacja
* [Co to "MTG" ? ](##Co-to-"MTG"-?)
* [Funkcjonalności aplikacji](##Funkcjonalności-aplikacji)
* [Struktura DB](##Struktura-DB)
* [Struktura obiektów](##Struktura-obiektów)
* [Uwagi](##Uwagi)
* [Technologie](##Technologie)
* [Wykorzystane strony](##Wykorzystane-strony)



## Co to "MTG" ?
Mtg to popularnie używane wśród fanów skrót dla nazwy Magic The Gathering. Jest to jedna z najpopularniejszych karcianek na świecie.
Przez ponad 27 lat istnienia, gra zyskała sobie wielką popularność, skupiając wokół siebie wiele ludzi. Rozgrywka zazwyczaj opiera 
się na pojedynku 2 graczy. Każdy z nich ma własną talię kart, 20 życia, a celem rozgrywki jest zbicie tej liczby przed przeciwnikiem.
Nie wchodząc w szczegółowe zasady, wymieniłem ważne informacje w kontekście programu:



* Karty wydawane są cyklicznie w tak zwanych dodatkach
* Pojedyńczy dodatek składa się z różnej ilości kart
* Zawiera trochę nowych, niespotykanych dotąd kart, a trochę starych, jeszcze raz drukowanych
* Każda karta ma swoją określoną cenę w prawdziwych pieniądzach, cena zależy od wielu czynników
* Ta sama karta drukowana w dwóch różnych dodatkach (wersjach) może mieć różną cenę
* Tak samo ja wszystko inne podlegają one prawom ekonomii, dlatego ceny są w ciągłym ruchu
* Karty dzielimy na podstawowe typy np. Sorcery, Instant, Creature itp. 
* Karty typu "Creature" jako jedyne mają wskaźniki "ataku" i "życia"



## Funkcjonalności aplikacji
Aplikacja pobiera pobiera różne parametry karty (np. nazwę, cenę, dodatek, artystę itp.) z [Scryfall API](https://scryfall.com/docs/api)
aby potem zmagazynować te dane w relacyjnej bazie danych w usłudze xampp. Istnieje również możliwośc eksportu tak zapisanych informacji do pliku json.
Żeby zacząć korzystać z aplikacji należy uruchomićusługę apache oraz mysql w panelu Xamp. Dalej poprowadzi nas interfejs konsolowy. 
Ogólnie rzecz biorąc, mamy dwie główne opcje : 

* Import kart z danego dodatku i zapis do bazy danych
* Zapis kart z bazy danych do formatu JSON

### Import kart
Po zatwierdzeniu odpowiednich opcji przed nami wyświetla się lista wszystkich [legalnych](## Uwagi) w tym momencie dodatków do zaimportowania.
Aby zainicjować akcję należy wpisać dokładną nazwę dodatku, która w pewnym momencie wyświetliła się w liście powyżej, jeżeli chce Pan zaimportować
więcej niż jeden dodatek kart na raz, proszę wpisać nazwy po przecinku i bez spacji np. "Welcome Deck 2016,Magic 2012". Spwoodwuje to natychmiastowy import kart.
Próbowałem dodać pasek postępu, ale wszystko za szybko się dzieje :) Taki import wstawia dane do bazy danych nie naruszając jej relacji
(ile ja się z tym namęczyłem)

### Zapis kart z bazy danych do formatu JSON
Istnieje możliwość zapisu kart do formatu JSON. Program w ścieżce "resources" tworzy (lub usuwa i nadpisuje) plik o nazwie data.json. W nim znajdziemy listę o nazwie "data"
która zawieraja obiekty reprezentujące karty



## Struktura DB
Baza danych, tworzona i użytkowana w tym programie ma automatycznie przypisywaną nazwę "mtg"
Składa się z następujących tabel:

* cards : główna tabela z szczegółowymi informacjami na temat danej karty. Nie przechowuje i akceptuje duplikatów
* artists : główna tabela przechowująca nazwy artystów projektujacych grafiki kart
* expansions : główna tabela przechowująca nazwy dodatków, których karty zostały zaimportowane do DB

* cards_expansions_connection : tabela pośrednia. Przechowuje ceny kart w zależności od dodatku a także relację między tabelami cards i expansions
* cards_artists_connection : tabela pośrednia. Przechowuje relację artystów z kartami, których grafiki zaprojektowali


### Uzasadnienie relacji
Baza danych składa się z 5 tabel, 3 głównych i 2 pośrednich. Zachodzą tutaj 2 relacje typu wiele-do-wielu : 
* karty a dodatki : jedna karta w niezmienionej formie, może być wydrukowana w paru dodatkach, bez zmieniana parametrów opisanych w tabli cards. Jedyne, co może ulec zmianie na podstawie dodatku, jest cena
* karty a artyści : jedna karta może mieć zaprojektowaną grafikę przez 2 artystów, a pojedyńczy artysta może mieć wiele zaprojektowanych kart

Tabela cards nie akceptuje duplikatów rekordów, dlatego za "prawdziwą" ilość kart w bazie z uwzględnieniem dodatów powinno się brać tabelę cards_expansions_con






## Struktura obiektów

* CardData : obiekt ułatwiający przenoszenie danych o kartach
* ScrapingAPI : interfejs zawierający metody wykorzystujące API
* CommunicationMYSQL : interfejs z metodomai bezpośrednio działającymi z xampem
* DB1 : klasa, kórej obiekt jest odwzorowaniem instancji bazy danych. Zawiera podstawowe metody niewchodzące w bezpośrendi kontakt z xampem
* MTGAssistant : klasa bazowa, która łączy funkcjonalności API z bazą danych

 
## Uwagi
Aplikacja zawiera pare niedopracowanych punktów, z których czuję się tutaj zobowiązany o nich napisać

* Bardzo często karty zawierają wartości null w polach "power" i "toughness". Jest to spowodowane tym, że zaklęcia, 
  w przeciwieństwie do stworów nie maja takich wartości
 
* Nazwa "legalne" zestawy oznacza, że apka sprawdza, czy nie mamy już takiego dodatku zaimportowanego
 


## Technologie

* Java
* Biblioteka JDBC - baza danych



## Wykorzystane strony

Witryna zezwala na korzystanie z ich API za darmo, pod warunkiem, że nie przekroczymy jakiejś strasznie wysokiej liczby
zapytań na sekundę oraz nie wykorzystamy API do celów zarobkowych. Oba warunki spełnione :)

* Scryfall API : https://scryfall.com/docs/api





