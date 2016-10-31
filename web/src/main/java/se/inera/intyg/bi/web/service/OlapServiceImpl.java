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
package se.inera.intyg.bi.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mondrian.olap.MondrianServer;
import mondrian.server.DynamicContentFinder;
import mondrian.spi.impl.IdentityCatalogLocator;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import se.inera.intyg.bi.web.config.TestUtil;
import se.inera.intyg.bi.web.util.ResultSetSerializer;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;

/**
 * Created by eriklupander on 2016-10-30.
 */
@Service
@DependsOn("dbUpdate")
public class OlapServiceImpl implements OlapService {

    public static final String DATABASE_NAME = "Provider=Mondrian;DataSource=Intyg;";
    public static final String CATALOG_NAME = "Intyg";

    private OlapConnection olapConnection;

    @Value("${datasources.xml.file}")
    private String dataSourcesFile;

    /**
     * Bootstrap the Mondrian server.
     */
    @PostConstruct
    public void init() {
        MondrianServer server =
                MondrianServer.createWithRepository(
                        new DynamicContentFinder(dataSourcesFile),
                        new IdentityCatalogLocator());

        try {
            olapConnection = server.getConnection(DATABASE_NAME, CATALOG_NAME, null);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String getDimensions() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new ResultSetSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);

        try {
            ResultSet dimensions = olapConnection.getMetaData().getDimensions(null, null, null, null);
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.putPOJO("results", dimensions);

            StringWriter sw = new StringWriter();
            objectMapper.writeValue(sw, objectNode);
            return sw.getBuffer().toString();
        } catch (OlapException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public String query(String queryString) {
          if (olapConnection ==  null) {
              throw new IllegalStateException("OLAP connection not started...");
          }
        try {
            OlapStatement stmt = olapConnection.createStatement();
            CellSet cs = stmt.executeOlapQuery(queryString);
            return TestUtil.toString(cs);
        } catch (OlapException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }
}
