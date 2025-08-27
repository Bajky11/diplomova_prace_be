package com.friends.friends.Config;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Repository.*;
import com.friends.friends.Repository.EventRepository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private LocationRepository locationRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {

        if(categoryRepository.findById(1L).isEmpty()){
            initializeCategories();
        }

        if(currencyRepository.findById(1L).isEmpty()){
            initializeCurrencies();
        }
        /*


        if(categoryRepository.findById(1L).isEmpty()){
            initializeCategories();
            initializeCurrencies();
            initializeAccounts();
            initializeLocations();
            //initializeEvents();
            //initializeLater();
            generateRandomEvents(1000); // Generating 100 random events
        }

         */

    }

    private void initializeLater(){
        Account account = accountRepository.findById(1L).get();
        account.setFavoriteEvents(List.of(eventRepository.findById(1L).get()));
        accountRepository.save(account);
    }

    private void initializeLocations() {
        Location ErnoKostala = new Location(
                "Erno Košťála 1006, 530 12 Pardubice III - Studánka, Česko",
                50.0433439, 15.8107417,
                accountRepository.findById(1L).get()
        );

        Location Upce = new Location(
                "Studentská 95, 53009 Pardubice II - Polabiny, Česko",
                50.0481603, 15.7693933,
                accountRepository.findById(1L).get()
        );

        Location Praha = new Location(
                "Pražský hrad, 119 08 Praha 1, Česko",
                50.090903, 14.400512,
                accountRepository.findById(2L).get()
        );

        Location Brno = new Location(
                "Výstaviště 405/1, 603 00 Brno, Česko",
                49.190983, 16.584563,
                accountRepository.findById(1L).get()
        );

        Location Ostrava = new Location(
                "Ruská 2993, 703 00 Ostrava-Vítkovice, Česko",
                49.802251, 18.282751,
                accountRepository.findById(2L).get()
        );

        Location Plzen = new Location(
                "Náměstí Republiky, 301 00 Plzeň, Česko",
                49.747474, 13.377987,
                accountRepository.findById(1L).get()
        );

        Location Liberec = new Location(
                "Nám. Dr. E. Beneše 1, 460 01 Liberec, Česko",
                50.769444, 15.058611,
                accountRepository.findById(2L).get()
        );

        Location Olomouc = new Location(
                "Horní náměstí, 779 00 Olomouc, Česko",
                49.595524, 17.251752,
                accountRepository.findById(1L).get()
        );

        Location Zlin = new Location(
                "Náměstí Míru, 760 01 Zlín, Česko",
                49.223553, 17.666634,
                accountRepository.findById(2L).get()
        );

        Location Hradec = new Location(
                "Eliščino nábřeží 465, 500 03 Hradec Králové, Česko",
                50.210388, 15.832578,
                accountRepository.findById(1L).get()
        );

        Location KarlovyVary = new Location(
                "Vřídelní 1, 360 01 Karlovy Vary, Česko",
                50.232438, 12.871176,
                accountRepository.findById(2L).get()
        );

        Location CeskeBudejovice = new Location(
                "Nám. Přemysla Otakara II., 370 01 České Budějovice, Česko",
                48.974472, 14.474348,
                accountRepository.findById(1L).get()
        );

        locationRepository.saveAll(List.of(ErnoKostala, Upce, Praha, Brno, Ostrava, Plzen, Liberec, Olomouc, Zlin, Hradec, KarlovyVary, CeskeBudejovice));
    }

    private void initializeAccounts() {

        Account test1 = new Account();
        test1.setName("test1");
        test1.setFavoriteCategories(List.of(categoryRepository.findById(1L).get()));
        test1.setEmail("test1@email.com");
        test1.setPasswordHash(passwordEncoder.encode("password"));
        test1.setIsBusiness(false);
        test1.setRegion("Hlavní město Praha");

        Account pce = new Account();
        pce.setName("Město Pardubice");
        pce.setEmail("pce@email.com");
        pce.setPasswordHash(passwordEncoder.encode("password"));
        pce.setIsBusiness(true);
        pce.setImageUrl("9e923241-9af2-4f70-4d2f-bedfcf9b4000");

        Account salon = new Account();
        salon.setName("Sálon cafe bar");
        salon.setEmail("salon@email.com");
        salon.setPasswordHash(passwordEncoder.encode("password"));
        salon.setIsBusiness(true);
        salon.setImageUrl("4ac1da48-1d53-4778-8581-fbb4bfb97700");

        Account instagram = new Account();
        instagram.setName("Instagram");
        instagram.setEmail("intagram@email.com");
        instagram.setPasswordHash(passwordEncoder.encode("password"));
        instagram.setIsBusiness(true);
        instagram.setImageUrl("f4f3f9cd-6e57-4459-9ac2-4db94ac6f600");

        accountRepository.saveAll(List.of(test1,pce, salon, instagram));
    }

    private void initializeEvents() {
        Event event1 = new Event();
        event1.setTitle("event1");
        event1.setDescription("Přijďte na naši první testovací událost v Pardubicích! Čeká vás příjemná atmosféra, zajímavý program a možnost poznat nové lidi. Nechte se překvapit, co jsme si pro vás připravili – tato akce je ideální pro všechny, kdo chtějí zažít něco nového a užít si skvělý den.");
        event1.setSelectedCategories(List.of(categoryRepository.findById(1L).get()));
        event1.setStartTime(LocalDateTime.now());
        event1.setCurrency(currencyRepository.findById(1L).get());
        event1.setCreatedBy(accountRepository.findById(1L).get());
        event1.setHappeningOn(locationRepository.findById(1L).get()); // Pardubice

        Event event2 = new Event();
        event2.setTitle("event2");
        event2.setDescription("Druhá testovací událost v Pardubicích je tady! Přijďte si užít den plný zábavy, inspirace a nových zážitků. Akce je vhodná pro všechny věkové kategorie a nabídne vám možnost poznat nové přátele i zajímavé aktivity.");
        event2.setSelectedCategories(List.of(categoryRepository.findById(2L).get()));
        event2.setStartTime(LocalDateTime.now());
        event2.setCurrency(currencyRepository.findById(2L).get());
        event2.setCreatedBy(accountRepository.findById(2L).get());
        event2.setHappeningOn(locationRepository.findById(2L).get()); // Pardubice

        Event event3 = new Event();
        event3.setTitle("Brněnský festival piva");
        event3.setDescription("Zažijte jedinečný festival piva v Brně! Ochutnejte desítky druhů piv z českých i zahraničních pivovarů, užijte si živou hudbu a bohatý doprovodný program. Přijďte s přáteli a objevte nové chutě i zážitky v srdci Moravy.");
        event3.setSelectedCategories(List.of(categoryRepository.findById(5L).get()));
        event3.setStartTime(LocalDateTime.now().plusDays(1));
        event3.setCurrency(currencyRepository.findById(1L).get());
        event3.setCreatedBy(accountRepository.findById(1L).get());
        event3.setHappeningOn(locationRepository.findById(4L).get()); // Brno

        Event event4 = new Event();
        event4.setTitle("Pražský maraton");
        event4.setDescription("Přijďte si zaběhnout tradiční Pražský maraton! Trasa vede historickým centrem Prahy a nabízí nezapomenutelnou atmosféru. Přihlaste se, překonejte sami sebe a užijte si den plný sportu, emocí a podpory od fanoušků.");
        event4.setSelectedCategories(List.of(categoryRepository.findById(4L).get()));
        event4.setStartTime(LocalDateTime.now().plusDays(2));
        event4.setCurrency(currencyRepository.findById(2L).get());
        event4.setCreatedBy(accountRepository.findById(2L).get());
        event4.setHappeningOn(locationRepository.findById(3L).get()); // Praha

        Event event5 = new Event();
        event5.setTitle("Olomoucké divadelní představení");
        event5.setDescription("Zveme vás na divadelní představení v Olomouci, které potěší celou rodinu! Těšit se můžete na skvělé herecké výkony, originální scénář a nezapomenutelný kulturní zážitek. Přijďte si užít večer plný smíchu i emocí.");
        event5.setSelectedCategories(List.of(categoryRepository.findById(2L).get()));
        event5.setStartTime(LocalDateTime.now().plusDays(3));
        event5.setCurrency(currencyRepository.findById(1L).get());
        event5.setCreatedBy(accountRepository.findById(1L).get());
        event5.setHappeningOn(locationRepository.findById(8L).get()); // Olomouc

        Event event6 = new Event();
        event6.setTitle("Plzeňský koncert");
        event6.setDescription("Hudební koncert v Plzni přináší vystoupení populárních kapel a nezapomenutelnou atmosféru. Přijďte si užít večer plný hudby, tance a skvělé nálady. Akce je vhodná pro všechny věkové kategorie.");
        event6.setSelectedCategories(List.of(categoryRepository.findById(1L).get()));
        event6.setStartTime(LocalDateTime.now().plusDays(4));
        event6.setCurrency(currencyRepository.findById(2L).get());
        event6.setCreatedBy(accountRepository.findById(2L).get());
        event6.setHappeningOn(locationRepository.findById(6L).get()); // Plzeň

        Event event7 = new Event();
        event7.setTitle("Liberecký networking");
        event7.setDescription("Networkingová akce v Liberci je ideální příležitostí pro podnikatele, startupisty i všechny, kdo chtějí rozšířit své kontakty. Čekají vás inspirativní přednášky, workshopy a možnost navázat nové spolupráce.");
        event7.setSelectedCategories(List.of(categoryRepository.findById(9L).get()));
        event7.setStartTime(LocalDateTime.now().plusDays(5));
        event7.setCurrency(currencyRepository.findById(3L).get());
        event7.setCreatedBy(accountRepository.findById(1L).get());
        event7.setHappeningOn(locationRepository.findById(7L).get()); // Liberec

        Event event8 = new Event();
        event8.setTitle("Zlínský filmový festival");
        event8.setDescription("Mezinárodní filmový festival ve Zlíně přináší pestrý výběr filmů pro děti i dospělé. Těšit se můžete na premiéry, setkání s tvůrci a bohatý doprovodný program. Přijďte zažít filmovou atmosféru a objevte nové filmové zážitky.");
        event8.setSelectedCategories(List.of(categoryRepository.findById(3L).get()));
        event8.setStartTime(LocalDateTime.now().plusDays(6));
        event8.setCurrency(currencyRepository.findById(1L).get());
        event8.setCreatedBy(accountRepository.findById(2L).get());
        event8.setHappeningOn(locationRepository.findById(9L).get()); // Zlín

        Event event9 = new Event();
        event9.setTitle("Hradecké vzdělávání");
        event9.setDescription("Vzdělávací seminář v Hradci Králové je určen pro všechny, kdo se chtějí rozvíjet a získat nové znalosti. Čekají vás zajímavé přednášky, workshopy a možnost konzultací s odborníky. Přijďte investovat do svého rozvoje!");
        event9.setSelectedCategories(List.of(categoryRepository.findById(7L).get()));
        event9.setStartTime(LocalDateTime.now().plusDays(7));
        event9.setCurrency(currencyRepository.findById(2L).get());
        event9.setCreatedBy(accountRepository.findById(1L).get());
        event9.setHappeningOn(locationRepository.findById(10L).get()); // Hradec Králové

        Event event10 = new Event();
        event10.setTitle("Karlovarské umění");
        event10.setDescription("Výstava umění v Karlových Varech představuje díla předních českých i zahraničních umělců. Přijďte se inspirovat, objevte nové směry v umění a užijte si jedinečnou atmosféru galerie v lázeňském městě.");
        event10.setSelectedCategories(List.of(categoryRepository.findById(6L).get()));
        event10.setStartTime(LocalDateTime.now().plusDays(8));
        event10.setCurrency(currencyRepository.findById(3L).get());
        event10.setCreatedBy(accountRepository.findById(2L).get());
        event10.setHappeningOn(locationRepository.findById(11L).get()); // Karlovy Vary

        Event event11 = new Event();
        event11.setTitle("Pardubická business konference");
        event11.setDescription("Business konference v Pardubicích je místem setkání podnikatelů, manažerů a inovátorů. Čekají vás inspirativní přednášky, panelové diskuze a možnost navázat nové obchodní kontakty. Nepropásněte tuto příležitost!");
        event11.setSelectedCategories(List.of(categoryRepository.findById(8L).get()));
        event11.setStartTime(LocalDateTime.now().plusDays(9));
        event11.setCurrency(currencyRepository.findById(1L).get());
        event11.setCreatedBy(accountRepository.findById(1L).get());
        event11.setHappeningOn(locationRepository.findById(1L).get()); // Pardubice

        Event event12 = new Event();
        event12.setTitle("Ostravský sportovní den");
        event12.setDescription("Sportovní den v Ostravě nabízí pestrý program pro všechny věkové kategorie. Přijďte si zasportovat, vyzkoušet nové disciplíny a užít si den plný pohybu a zábavy. Akce je vhodná pro rodiny i jednotlivce.");
        event12.setSelectedCategories(List.of(categoryRepository.findById(4L).get()));
        event12.setStartTime(LocalDateTime.now().plusDays(10));
        event12.setCurrency(currencyRepository.findById(2L).get());
        event12.setCreatedBy(accountRepository.findById(2L).get());
        event12.setHappeningOn(locationRepository.findById(5L).get()); // Ostrava

        Event event13 = new Event();
        event13.setTitle("Českobudějovická zábava");
        event13.setDescription("Zábavná akce v Českých Budějovicích přináší bohatý program pro malé i velké. Těšit se můžete na soutěže, hudební vystoupení a spoustu zábavy. Přijďte si užít den plný radosti a nových zážitků s rodinou i přáteli.");
        event13.setSelectedCategories(List.of(categoryRepository.findById(10L).get()));
        event13.setStartTime(LocalDateTime.now().plusDays(11));
        event13.setCurrency(currencyRepository.findById(1L).get());
        event13.setCreatedBy(accountRepository.findById(1L).get());
        event13.setHappeningOn(locationRepository.findById(12L).get()); // České Budějovice

        eventRepository.saveAll(List.of(event1, event2, event3, event4, event5, event6, event7, event8, event9, event10, event11, event12, event13));
    }

    private void initializeCategories() {
        String[] categories = {
                "Koncerty", "Divadlo", "Film", "Sport", "Jídlo a pití",
                "Umění", "Vzdělávání", "Business", "Networking", "Zábava",
                "Technologie", "Příroda", "Cestování", "Rodina", "Děti",
                "Tanec", "Hudba", "Startupy", "Věda", "Historie"
        };

        for (String categoryName : categories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = Category.builder()
                        .name(categoryName)
                        .build();
                categoryRepository.save(category);
            }
        }
    }

    private void initializeCurrencies() {
        // Czech Koruna
        if (!currencyRepository.existsByCode("CZK")) {
            Currency czk = Currency.builder()
                    .code("CZK")
                    .symbol("Kč")
                    .name("Czech koruna")
                    .build();
            currencyRepository.save(czk);
        }

        // Euro
        if (!currencyRepository.existsByCode("EUR")) {
            Currency eur = Currency.builder()
                    .code("EUR")
                    .symbol("€")
                    .name("Euro")
                    .build();
            currencyRepository.save(eur);
        }

        // US Dollar
        if (!currencyRepository.existsByCode("USD")) {
            Currency usd = Currency.builder()
                    .code("USD")
                    .symbol("$")
                    .name("US Dollar")
                    .build();
            currencyRepository.save(usd);
        }
    }

    public void generateRandomEvents(int count) {
        String[] namePrefixes = {"Super", "Mega", "Ultra", "Český", "Letní", "Zimní", "Jarní", "Podzimní", "Noční", "Den"};
        String[] nameSuffixes = {"festival", "akce", "sraz", "párty", "workshop", "konference", "den", "noc", "show", "trh"};
        List<Category> categories = categoryRepository.findAll();
        List<Account> accounts = accountRepository.findAll();
        List<Currency> currencies = currencyRepository.findAll();

        Random rand = new Random();

        for (int i = 0; i < count; i++) {
            Event event = new Event();

            // Náhodný název
            String title = namePrefixes[rand.nextInt(namePrefixes.length)] + " " +
                           nameSuffixes[rand.nextInt(nameSuffixes.length)];
            event.setTitle(title);

            //Náhodný description
            event.setDescription("Literature admiration frequently indulgence announcing are who you her. Was least quick after six. So it yourself repeated together cheerful. Neither it cordial so painful picture studied if. Sex him position doubtful resolved boy expenses. Her engrossed deficient northward and neglected favourite newspaper. But use peculiar produced concerns ten.");

            // Náhodná kategorie (1-3 kategorie)
            int catCount = 1 + rand.nextInt(5);
            Collections.shuffle(categories);
            event.setSelectedCategories(categories.subList(0, catCount));

            // Náhodný čas v příštích 30 dnech
            event.setStartTime(LocalDateTime.now().plusDays(rand.nextInt(30)));
            //event.setStartTime(LocalDateTime.now());

            // Náhodná měna
            event.setCurrency(currencies.get(rand.nextInt(currencies.size())));

            // Náhodná cena
            BigDecimal price = BigDecimal.valueOf(rand.nextInt(501)); // 0 až 500 včetně
            event.setPrice(price);

            // Náhodný uživatel
            event.setCreatedBy(accounts.get(rand.nextInt(accounts.size())));

            event.setImageUrl("a48c8361-4154-4409-760c-22a494534300");

            // Náhodné místo v ČR (lat 48.5–51.0, lon 12.0–19.0)
            double lat = 48.5 + rand.nextDouble() * (51.0 - 48.5);
            double lon = 12.0 + rand.nextDouble() * (19.0 - 12.0);
            Location randomLoc = new Location(
                "Generované adresa",
                lat, lon,
                event.getCreatedBy()
            );
            locationRepository.save(randomLoc);
            event.setHappeningOn(randomLoc);

            eventRepository.save(event);
        }
    }
}
