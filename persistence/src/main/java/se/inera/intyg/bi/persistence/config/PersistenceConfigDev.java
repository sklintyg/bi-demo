/**
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of bi (https://github.com/sklintyg/bi).
 *
 * bi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.bi.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
@Profile("dev")
@ComponentScan("se.inera.intyg.bi.persistence")
@EnableJpaRepositories(basePackages = "se.inera.intyg.bi.persistence")
public class PersistenceConfigDev extends PersistenceConfig {

    @Value("${db.driver}")
    private String databaseDriver;
    @Value("${db.url}")
    private String databaseUrl;
    @Value("${db.username}")
    private String databaseUsername;
    @Value("${db.password}")
    private String databasePassword;
    @Value("${db.httpPort}")
    private String databaseHttpPort;
    @Value("${db.tcpPort}")
    private String databaseTcpPort;

    @Bean(destroyMethod = "stop")
    Server createTcpServer() throws SQLException {
        Server server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", databaseTcpPort);
        server.start();
        return server;
    }

    @Bean(destroyMethod = "stop")
    Server createWebServer() throws SQLException {
        Server server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", databaseHttpPort);
        server.start();
        return server;
    }

    @Bean(destroyMethod = "close")
    DataSource standaloneDataSource() {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName(databaseDriver);
        dataSourceConfig.setJdbcUrl(databaseUrl);
        dataSourceConfig.setUsername(databaseUsername);
        dataSourceConfig.setPassword(databasePassword);

        return new HikariDataSource(dataSourceConfig);
    }

    @Bean(name = "dbUpdate")
    SpringLiquibase initDb(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:changelog/changelog.xml");
        return springLiquibase;
    }
}
