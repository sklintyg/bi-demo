# bi-demo
Repo för delning av filer för vår utvärdering av BI-lösningar

## Beskrivning
Består i skrivande stund av tre delar, se längre ner.

## Konfigurera lokal MySQL
1. Installera MySQL om du inte redan har gjort det...
2. Logga in som DBA/root och kör följande script:

    CREATE USER 'bi'@'localhost' IDENTIFIED BY 'bi';
    GRANT ALL PRIVILEGES ON * . * TO 'bi'@'localhost';
    
Klart!

### bi-persistence
- Persistence-konfiguration för MySQL med JDBC
- Liquibase-script för att skapa följande tabeller:
-- fact_intyg
-- dim_intygstyp
-- dim_vardenhet
-- dim_vardgivare
-- dim_icd
-- dim_date
-- Script för att populera dim_date samt vid behov skapa en tom 'bi'-databas samt användare för MySQL.

### bi-web
- REST-gränssnitt för att exekvera MDX-queries
- Spring @Service-klass för att starta Mondrian lokalt mot databasschemat från bi-persistence samt metadatafilen i /src/main/webapp/WEB-INF/intyg.xml
- Konfigurationsklass för att bootstrappa applikationen med:
-- Persistens via (bi-persistence)
-- REST-gränssnitt via Spring och CXF.
- /WEB-INF mapp med datasources.xml, intyg.xml (OLAP-metadata)

#### Konfiguration
Man måste f.n. gå in i

    /web/src/main/webapp/WEB-INF/datasources.xml
    
Och korrigera sökvägen på rad 64:

        <DataSourceInfo>Provider=mondrian;Jdbc=jdbc:mysql://localhost:3306/bi;JdbcDrivers=com.mysql.jdbc.Driver;JdbcUser=bi;JdbcPassword=bi;PoolNeeded=false;Catalog=file:/[absolut-sökväg-till-projektmappen]/web/src/main/webapp/WEB-INF/


Man behöver rimligen korrigera lite i dev.properties

    db.username=bi
    db.password=bi
    
    ## Only used with H2
    db.httpPort=8021
    db.tcpPort=8023
    
    # Not really necessary...
    hibernate.dialect=org.hibernate.dialect.H2Dialect
    hibernate.hbm2ddl.auto=
    hibernate.ejb.naming_strategy=org.hibernate.cfg.ImprovedNamingStrategy
    hibernate.show_sql=false
    hibernate.format_sql=true
    
    ### JNDI
    db.driver=com.mysql.jdbc.Driver
    db.url=jdbc:mysql://localhost:3306/bi?useCompression=true
    


#### Hibernate
Används egentligen inte alls som det är nu, men finns där ifall vi vill bootstrappa data programmatiskt eller liknande.

### tools
- .groovy-script för att transformera data från tabellen intyg.SJUKFALL_CERT till INSERT-statements för bi.fact_intyg

#### Användning
Kör först scriptet mot din lokala MySQL (som behöver ha någon form av data i intyg.SJUKFALL_CERT):

    gradle run
    
Det tar en stund, default är 500 000 poster.

Importera sedan i din MySQL-databas:

    mysql -u bi -p bi < factdata.txt
    
OBS! factdata-filen är gitignorad, skall ej pushas av hängslen-och-livrem skäl.