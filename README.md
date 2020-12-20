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
Aplikacja pobiera pobiera różne parametry karty (np. nazwę, cenę, dodatek, artystę itp.) z różnych stron internetowych
aby potem zmagazynować te dane w relacyjnej bazie danych w usłudze xampp. Żeby zacząć korzystać z aplikacji należy uruchomić
usługę apache oraz mysql w panelu Xamp. Dalej poprowadzi nas interfejs konsolowy. Ogólnie rzecz biorąc, mamy dwie główne opcje : 

* Import kart z danego dodatku
* Uaktualnienie cen wszystkich kart w bazie danych

### Import kart
Po zatwierdzeniu odpowiednich opcji przed nami wyświetla się lista wszystkich [legalnych](## Uwagi) w tym momencie dodatków do zaimportowania.
Aby zainicjować akcję należy wpisać dokładną nazwę dodatku, która w pewnym momencie wyświetliła się w liście powyżej, jeżeli chce Pan zaimportować
więcej niż jeden dodatek kart na raz, proszę wpisać nazwy po przecinku i bez spacji np. "Welcome Deck 2016,Magic 2012". Spowoduje to wyświetlenie
się wskaźnika pobrania, wstawiane karty można na bierząco śledzić w xampie. Taki import wstawia dane do bazy danych nie naruszając jej relacji
(ile ja się z tym namęczyłem)

### Uaktualnienie cen wszystkich kart w bazie danych
Tak, jak wspomniałem wcześniej, karty nieustannie zmieniają swoją cenę. Aby byc na bierząco można zaktualizować ich ceny w aplikacji.
Pobierane są wtedy wszystkie rekordy z niezbędnymi informacjami z tabeli "cards_expansions_connections". Gdy program wykryje różnicę cen
wstawi akualną wartość do DB



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

* Commands : interfejs zawierający zmienne znakowe, opisujące tworzenie struktury nowej bazy danych
* Credentials : interfejs zawierający dane niezbędne do połączenia się z xampem
* Scraping : interfejs zawierający metody wykorzystujące jsoupa
* DBConnect : klasa abstrakcyjna zawierająca metody bezpośrednio łączące się z bazą danych w xampie. Wykorzystuje jsoupa
* DB : klasa, kórej obiekt jest odwzorowaniem instancji bazy danych. Zawiera podstawowe metody niewchodzące w interakcje na poziome jsoupa i jdbc


## Uwagi
Aplikacja zawiera pare niedopracowanych punktów, z których czuję się tutaj zobowiązany o nich napisać

* Pomimo wielokrotnego "przyspieszania" kodu pobierającego informację z sieci, musiałem sztucznie go spowolnić 
  ze względu na ryzyko niewczytania zawartości strony cardMarket, która potrafi byc dość obciążona

* Szybkość pobierania kart u mnie jest dosyć przyzwoita, jednak wszystko zależy od "ruchu" na pojedyńczej stronie w momencie pobierania i szybkości internetu

* Zdarzają się przypadki, kiedy w pole z ceną zostaje wpisany NULL. 
  Jest to spowodowane specjalnymi znakami w nazwie karty. Nie ładuje się wtedy poprawwny url do pobioru ceny.
  Żeby to poprawić, musiałbym dodać kolejne reguły przekształcania nazw kart, a to strasznie pracochłonne.
  Powiedziałbym, że ten błąd obowiązuje około 5% kart

* Zalecam pobieranie dodatków jednowyrazowych gdyż z powodu różnicy w nazwach 
  dodatków na [oficjalnej stronie mtg](https://gatherer.wizards.com/Pages/Default.aspx) i stronie cardMarket czasami w przypadkach nazw
  takich jak np. "Magic 2014 Core Set" nie można pobrać prawidłowo cen kart. 
  W przyszłości planuje wykorzystać specjalne [api cardmarketu](https://api.cardmarket.com/ws/documentation) żeby rozwiązać ten problem z różnicami nazw.
  Przetestowane nazwy dodatków : Amonkhet, Welcome Deck 2016, Magic 2012, Dominaria
  
* Podczas pobierania dodatków może wyświetlić się komunikat "duplicate .... for key_name".
  Jest to całkowicie normalne i obługiwane zdarzenie. Informuje nas o przypadku, kiedy natrafiliśmy na
  wydrukowaną jeszcze raz "starą" kartę
  
* Nazwa "legalne" zestawy oznacza, że apka sprawdza, czy nie mamy już takiego dodatku zaimportowanego, 
 a także pobiera pojawiające się nowe dodatki automatycznie, nazwy nie są wpisane na sztywno. 
 
* Jeżeli napotka Pan problemy z połączeniem z xampem, proszę sprawdzić dane interfejsu "Credentials".
  Domyślnie założyłem, że port mysql to 3300 a użytkownik to root bez hasła
 



## Technologie

* Java
* Biblioteka Jsoup - scraping
* Biblioteka JDBC - baza danych



## Wykorzystane strony

Obie te witryny pozwalają na pobieranie swoich danych, pod warunkiem, że nie wykorzysta
się ich komercyjnie i nie przekroczy jakiejś strasznie wygórowanej liczby zapytań na sekundę

* https://gatherer.wizards.com/Pages/Default.aspx - informacje o poszczególnych kartach
* https://www.cardmarket.com/en/Magic - ceny kart





