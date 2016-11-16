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

import mondrian.olap.MondrianServer;
import mondrian.server.DynamicContentFinder;
import mondrian.spi.impl.IdentityCatalogLocator;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.layout.RectangularCellSetFormatter;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.query.Query;
import org.olap4j.query.Selection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import se.inera.intyg.bi.web.service.dto.cube.CubeModel;
import se.inera.intyg.bi.web.service.dto.cube.DimensionModel;
import se.inera.intyg.bi.web.service.dto.cube.FilterModel;
import se.inera.intyg.bi.web.service.dto.query.QueryDimension;
import se.inera.intyg.bi.web.service.dto.query.QueryModel;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by eriklupander on 2016-10-30.
 */
@Service
@DependsOn("dbUpdate")
public class OlapServiceImpl implements OlapService {

    private static final Logger LOG = LoggerFactory.getLogger(OlapServiceImpl.class);

    public static final String DATABASE_NAME = "Provider=Mondrian;DataSource=Intyg;";
    public static final String CATALOG_NAME = "Intyg";
    public static final String MEASURES = "Measures";
    public static final String DIMENSIONS = "Dimensions";

    private OlapConnection olapConnection;

    @Value("${olap.database.name}")
    private String olapDatabaseName = DATABASE_NAME;

    @Value("${olap.catalog.name}")
    private String olapCatalogName = CATALOG_NAME;

    @Value("${olap.cube.name}")
    private String olapCubeName = CATALOG_NAME;

    @Value("${datasources.xml.file}")
    private String dataSourcesFile;

    /**
     * Bootstrap the Mondrian server.
     */
    @PostConstruct
    public void init() {

        String absolutePathToDsFile = resolvePathToMondrianSchema();
        absolutePathToDsFile = absolutePathToDsFile.substring(0, absolutePathToDsFile.lastIndexOf("/"));


        MondrianServer server =
                MondrianServer.createWithRepository(
                        new DynamicContentFinder(absolutePathToDsFile + "/" + dataSourcesFile),
                        new IdentityCatalogLocator());

        try {
            olapConnection = server.getConnection(olapDatabaseName, olapCatalogName, null);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CubeModel getCubeModel() {
        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get(olapCubeName);

            CubeModel cm = new CubeModel();
            // Dimensions
            NamedList<Dimension> dimList = cube.getDimensions();

            DimensionModel measures = new DimensionModel(MEASURES);
            DimensionModel dimensions = new DimensionModel(DIMENSIONS);

            for (Dimension d : dimList) {
                if (!d.isVisible()) {
                    continue;
                }
                if (d.getName().equals(MEASURES)) {

                    for (Member member : d.getDefaultHierarchy().getRootMembers()) {
                        if (!member.isVisible()) {
                            continue;
                        }
                        DimensionModel submodel = new DimensionModel(member.getName());
                        measures.add(submodel);
                    }
                }

            }


            for (Dimension d : dimList) {
                if (!d.isVisible()) {
                    continue;
                }
                if (!d.getName().equals(MEASURES)) {
                    DimensionModel model = new DimensionModel(d.getName());
                    for (Hierarchy hierarchy : d.getHierarchies()) {
                        if (!hierarchy.isVisible()) {
                            continue;
                        }
                        DimensionModel submodel = new DimensionModel(hierarchy.getName());
                        NamedList<Level> levels = hierarchy.getLevels();
                        for (Level level : levels) {
                            DimensionModel levelModel = new DimensionModel(level.getName());
                            submodel.add(levelModel);
                        }
                        model.add(submodel);
                    }
                    dimensions.add(model);
                }
            }

            cm.setDimensions(dimensions);
            cm.setMeasures(measures);
            return cm;
        } catch (OlapException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
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
            StringWriter stringWriter = new StringWriter();

            PrintWriter writer = new PrintWriter(stringWriter);
            new RectangularCellSetFormatter(false).format(cs, writer);
            return stringWriter.toString();
        } catch (OlapException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<FilterModel> getDimensionValues(QueryDimension queryDimension) {
        List<FilterModel> list = new ArrayList<>();

        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get(olapCubeName);
            Query q = new Query(olapCubeName, cube);
            org.olap4j.query.QueryDimension rootDimension = q.getDimension(queryDimension.nth(0));

            NamedList<Hierarchy> hierarchies = rootDimension.getDimension().getHierarchies();
            for (Hierarchy hierarchy : hierarchies) {
                for (Level l : hierarchy.getLevels()) {
                    if (l.getUniqueName().equals(queryDimension.toString())) {
                        list.addAll(
                                l.getMembers().stream().map(m -> new FilterModel(m.getUniqueName(), m.getName())).collect(Collectors.toList())
                        );
                    }
                }
            }

            return list;
        } catch (OlapException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public CellSet executeQuery(QueryModel queryModel) {
        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get(olapCubeName);
            Query q = new Query(olapCubeName, cube);

            // Now we start building the actual Olap4j query model.
            for (QueryDimension col : queryModel.getColumns()) {
                buildQueryAxis(q, col, Axis.COLUMNS);
            }

            for (QueryDimension row : queryModel.getRows()) {
                buildQueryAxis(q, row, Axis.ROWS);
            }

            q.getAxes().values().stream().forEach(axis -> axis.setNonEmpty(queryModel.getIncludeEmpty()));
            q.validate();

            String mdx = q.getSelect().toString();
            System.out.println("\n\n" + mdx + "\n\n");

            return q.execute();
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            LOG.error(e.getCause().getMessage());
            LOG.error(e.getCause().getCause().getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    private void buildQueryAxis(Query q, QueryDimension queryDimension, Axis axis) throws OlapException {
        // Always always start by getting hold of the root dimension.
        String root = queryDimension.nth(0);

        // The root must _always_ map to a dimension, otherwise we're out of here.
        org.olap4j.query.QueryDimension rootDimension = q.getDimension(root);
        if (rootDimension == null) {
            throw new IllegalArgumentException("Unknown root row dimension: " + root);
        }

        if (rootDimension.getDimension().getUniqueName().equals("[" + MEASURES + "]")) {
            // Measures are handled through members
            buildQueryMeasure(queryDimension, rootDimension);
        } else {
            // Dimensions are handled through hierarchy -> levels
            buildQueryDimension(queryDimension, rootDimension);
        }
        if (!isPresentOnAxis(q, axis, rootDimension)) {
            q.getAxis(axis).addDimension(rootDimension);
        }

       // q.getAxis(axis).setNonEmpty(true);
    }

    private boolean isPresentOnAxis(Query q, Axis axis, org.olap4j.query.QueryDimension rootDimension) {
        for (org.olap4j.query.QueryDimension qd : q.getAxis(axis).getDimensions()) {
            if (qd.getName().equals(rootDimension.getName())) {
                return true;
            }
        }
        return false;
    }

    private void buildQueryMeasure(QueryDimension queryDimension, org.olap4j.query.QueryDimension rootDimension) throws OlapException {
        NamedList<Member> rootMembers = rootDimension.getDimension().getDefaultHierarchy().getRootMembers();
        for (Member m : rootMembers) {
            if (m.getUniqueName().equals(queryDimension.toString())) {
                rootDimension.include(Selection.Operator.MEMBER, m);
            }
        }
    }

    private void buildQueryDimension(QueryDimension queryDimension, org.olap4j.query.QueryDimension rootDimension) throws OlapException {
        NamedList<Hierarchy> hierarchies = rootDimension.getDimension().getHierarchies();
        for (Hierarchy hierarchy : hierarchies) {

            for (Level l : hierarchy.getLevels()) {

                // If our queryDimension contains values, we must use those...
                if (queryDimension.getFilterValues().size() > 0) {
                    List<Member> members = l.getMembers();
                    for (QueryDimension valueEntry : queryDimension.getFilterValues()) {
                        for (Member member : members) {
                            if (member.getUniqueName().equals(valueEntry.toString())) {
                                rootDimension.include(member);
                            }
                        }
                    }
                // Otherwise, try to add the entry normal as Datum.Datum.Ar or Datum.Datum.(All) or similar...
                } else if (l.getUniqueName().equals(queryDimension.toString())) {
                    rootDimension.include(l);
                }
            }
        }
    }

    private String resolvePathToMondrianSchema() {
        try {
            return "file://" + new File(
                    this.getClass()
                            .getClassLoader()
                            .getResource("logback.xml") // logback finns alltid på samma ställe...
                            .toURI())
                    .getPath();
        } catch (Exception e) {
            throw new RuntimeException("Data source file: " + dataSourcesFile + ": " + e.getMessage());
        }
    }

}