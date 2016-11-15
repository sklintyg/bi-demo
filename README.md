# bi-demo
PoC från projektets utvärdering av BI-lösningar okt/nov 2016.

## Beskrivning
Vi har tagit fram två separata OLAP-kuber baserat på data från:

- Intygsdata (från intygstjänsten) - identifieras 'bi' nedan.
- Sjukfallsdata (från statistiktjänsten) - identifieras 'st' nedan.

Normalfallet i den incheckade konfigurationen är att köra på 'st'-varianten.

## Konfigurera lokal MySQL
1. Installera MySQL om du inte redan har gjort det.
2. Logga in som DBA/root och kör följande script (för st):

    CREATE USER 'st'@'localhost' IDENTIFIED BY 'st';
    GRANT ALL PRIVILEGES ON * . * TO 'st'@'localhost';
    CREATE DATABASE 'st' /*!40100 DEFAULT CHARACTER SET utf8 */;
    
(Eller kör /persistence/src/main/resources/create-st-user.xml)
    
Tabeller, index, data etc. kommer skapas av Liquibase vid första uppstart.

## bi-persistence

Detta delprojekt nyttjar Liquibase för att skapa scheman (st eller bi) och populera dessa med data.

Notera att 'bi'-data inte finns incheckat då dessa har scramblade personnummer.

#### BI
Följande tabeller:
- fact_intyg
- dim_intygstyp
- dim_vardenhet
- dim_vardgivare
- dim_icd
- dim_date

#### ST
Följande tabeller:
- fact_sjukfall
- dim_date
- dim_dx_kapitel
- dim_dx_avsnitt 
- dim_dx_kategori
- dim_dx_kod
- dim_sjukskrivningsgrad
- dim_age
- dim_gender
- dim_lan
- dim_enheter
- dim_sjukfalllakare


## bi-web
Klassisk 'web'-modul för att tillhandahålla tjänstelagret.

- REST-gränssnitt för att läsa ut OLAP-kubens measures och dimensions, ställa frågor osv.
- Spring-baserad mekanism för att starta Mondrian lokalt mot databasschemat från bi-persistence samt metadatafilen i /src/main/webapp/WEB-INF/stat.xml
- Konfigurationsklasser

#### Metadatafiler
Det finns en OLAP-schemafil för respektive databasmodell, 'stat.xml' och 'intyg.xml'.

#### Datasources
För respektive datakälla behöver vi för Mondrians skull knyta ihop relationsdatabas med OLAP-schema. Detta sker i XML-filerna:
- st-datasources.xml
- bi-datasources.xml

Dessa ligger idag i WEB-INF.

TODO Dessa filer innehåller idag absoluta sökvägar, fixa det! Tyvärr verkar det bara funka att lägga stat.xml i roten av projektet...



#### Konfiguration

Man styr huruvida man vill köra med 'bi'-modellen eller 'st'-modellen genom att aktivera spring-profilen 'stat' i /web/build.gradle.

    gretty {
        httpPort = 8020
        contextConfigFile = "${projectDir}/src/main/resources/jetty-web.xml"
        contextPath = '/'
        logbackConfigFile = "${projectDir}/src/main/resources/logback.xml"
        jvmArgs = [
            '-Dspring.profiles.active=test,stat',    // <---- HÄR!
            '-Dbi.config.file=' + projectDir + '/src/main/resources/st.properties',
            '-Dcredentials.file=' + projectDir + '/src/main/resources/dev-credentials.properties',
            '-Dbi.resources.folder=/' +projectDir + '/../src/main/resources',
            '-XX:MaxPermSize=256M',
            '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8018'
        ]

### Enhetstest / Integrationstest
Det finns några integrationstester skrivna som verifierar att XML-metadata matchar underliggande relationsdatabas. Dessa är tills vidare avstängda mha @Ignore på testnivå då respektive testsvit kräver att databasen är korrekt uppsatt lokalt.

Testerna skall fungera om man sätter upp respektive databasschema (st, bi) korrekt. Det bör fixas att med H2 skapa databasschema vid integrationstest om/när någon tar denna PoC vidare.

### tools
- .groovy-script för att för 'bi'-schema transformera data från tabellen intyg.SJUKFALL_CERT till INSERT-statements för bi.fact_intyg

#### Användning
Kör först scriptet mot din lokala MySQL (som behöver ha någon form av data i intyg.SJUKFALL_CERT):

    gradle run
    
Det tar en stund, default är 500 000 poster.

Importera sedan i din MySQL-databas:

    mysql -u bi -p bi < factdata.txt
    
OBS! factdata-filen är gitignorad, skall ej pushas av hängslen-och-livrem skäl. Även om det inte är riktiga personnummer i filen som skapas så är de syntaktiskt korrekta och kan i framtiden komma att användas för verkliga personer.