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

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
@Profile("!dev")
@ComponentScan("se.inera.intyg.bi.persistence")
public class PersistenceConfigJndi extends PersistenceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceConfigJndi.class);

    // CHECKSTYLE:OFF EmptyBlock
    @Bean
    DataSource jndiDataSource() {
        DataSource dataSource = null;
        JndiTemplate jndi = new JndiTemplate();
        try {
            dataSource = (DataSource) jndi.lookup("jdbc/bi");       // java:comp/env/
        } catch (NamingException e) {
            LOG.error("Error getting DataSource from JNDI: " + e.getMessage());
        }
        return dataSource;
    }
    // CHECKSTYLE:ON EmptyBlock

    @Bean(name = "dbUpdate")
    SpringLiquibase initDb(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:changelog/changelog.xml");
        return springLiquibase;
    }
}
