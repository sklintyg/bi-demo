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
package se.inera.intyg.bi.persistence.liquibase;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class DbChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DbChecker.class);

    private DataSource dataSource;

    private String script;

    public DbChecker(DataSource dataSource, String script) {
        super();
        this.dataSource = dataSource;
        this.script = script;
    }

    @PostConstruct
    public void checkDb() {
        DatabaseConnection connection = null;
        try {
            connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            Liquibase liquibase = new Liquibase(script, new ClassLoaderResourceAccessor(), database);
            LOG.info("Checking database: {} URL:{}", database.getDatabaseProductName(), database.getConnection().getURL());
            List<ChangeSet> changeSets = liquibase.listUnrunChangeSets(null, null);
            if (!changeSets.isEmpty()) {
                StringBuilder errors = new StringBuilder();
                for (ChangeSet changeSet : changeSets) {
                    errors.append('>').append(changeSet.toString()).append('\n');
                }
                throw new Error("Database version mismatch. Check liquibase status. Errors:\n" + errors.toString() + database.getDatabaseProductName()
                        + ", " + database);
            }
        } catch (liquibase.exception.LiquibaseException e) {
            throw new Error("Database not ok, aborting startup.", e);
        } catch (SQLException e) {
            throw new Error("Database not ok, aborting startup.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    LOG.info("Could not close DatabaseConnection in DbChecker, {}", e);
                    throw new Error("Could not close DatabaseConnection in DbChecker", e);
                }
            }
        }
        LOG.info("Liquibase ok");
    }
}
