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
import org.olap4j.query.QueryDimension;
import org.olap4j.query.Selection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import se.inera.intyg.bi.web.service.dto.CubeModel;
import se.inera.intyg.bi.web.service.dto.DimensionEntry;
import se.inera.intyg.bi.web.service.dto.DimensionModel;
import se.inera.intyg.bi.web.service.dto.QueryModel;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public CubeModel getCubeModel() {
        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get("Intyg");

            CubeModel cm = new CubeModel();
            // Dimensions
            NamedList<Dimension> dimList = cube.getDimensions();

            DimensionModel measures = new DimensionModel("Measures");

            for (Dimension d : dimList) {
                if (!d.isVisible()) {
                    continue;
                }
                if (d.getName().equals("Measures")) {

                    for (Member member : d.getDefaultHierarchy().getRootMembers()) {
                        if (!member.isVisible()) {
                            continue;
                        }
                        DimensionModel submodel = new DimensionModel(member.getName());
                        measures.add(submodel);
                    }
                }

            }

            DimensionModel dimensions = new DimensionModel("Dimensions");
            for (Dimension d : dimList) {
                if (!d.isVisible()) {
                    continue;
                }
                if (!d.getName().equals("Measures")) {
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
    public String toMdx(QueryModel queryModel) {

        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get("Intyg");
            Query q = new Query("Intyg", cube);

            buildQueryDimensions(queryModel, cube, q);
            q.validate();

            return q.getSelect().toString();
        } catch (OlapException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Cube getCube(String name) {
        try {
            return olapConnection.getOlapSchema().getCubes().get("Intyg");
        } catch (OlapException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CellSet executeQuery(QueryModel queryModel) {
        try {
            Cube cube = olapConnection.getOlapSchema().getCubes().get("Intyg");
            Query q = new Query("Intyg", cube);

            // First, handle dimensions for COLUMNS
            for (DimensionEntry col : queryModel.getColumns()) {
                // Always always start by getting hold of the root dimension.
                String root = col.nth(0);

                // The root must _always_ map to a dimension, otherwise we're out of here.
                QueryDimension rootDimension = q.getDimension(root);
                if (rootDimension == null) {
                    throw new IllegalArgumentException("Unknown root col dimension: " + root);
                }

                NamedList<Hierarchy> hierarchies = rootDimension.getDimension().getHierarchies();
                for (Hierarchy hierarchy : hierarchies) {
                    for (Level l : hierarchy.getLevels()) {
                        if (l.getUniqueName().equals(col.toString())) {
                            // Add this level

                            rootDimension.include(l);

                        }
                    }
                }
                q.getAxis(Axis.COLUMNS).addDimension(rootDimension);
            }


            for (DimensionEntry row : queryModel.getRows()) {
                // Always always start by getting hold of the root dimension.
                String root = row.nth(0);

                // The root must _always_ map to a dimension, otherwise we're out of here.
                QueryDimension rootDimension = q.getDimension(root);
                if (rootDimension == null) {
                    throw new IllegalArgumentException("Unknown root row dimension: " + root);
                }

                NamedList<Hierarchy> hierarchies = rootDimension.getDimension().getHierarchies();
                for (Hierarchy hierarchy : hierarchies) {
                    for (Level l : hierarchy.getLevels()) {
                        if (l.getUniqueName().equals(row.toString())) {
                            // Add this level
                            rootDimension.include(l);

                        }
                    }
                }
                q.getAxis(Axis.ROWS).addDimension(rootDimension);
            }

           // q.getAxis(axis).addDimension(dimension);



            //buildQueryDimensions(queryModel, cube, q);

            // Always add filter on vardgivare...
            // QueryDimension vardgivare = q.getDimension("Vardgivare");

            // TODO Kom ihåg - nedanstående räcker inte för att enbart få med vårdenheter under angiven vårdgivare.
            // Vi måste antingen lägga till vgId som attribut på vårdenhet, alternativt kanske slå samma vg och ve till en gemensam hierarki
            // där ve är underordnat vg på ett annat vis. Hur nu man gör det...
            // vardgivare.include(cube.lookupMember(IdentifierNode.ofNames("Vardgivare", "Vardgivarid", "SE162321000255-O00001").getSegmentList()));
            // q.getAxis(Axis.FILTER).addDimension(vardgivare);
           // q.getAxis(Axis.ROWS).addDimension(vardgivare);

            q.validate();

            String mdx = q.getSelect().toString();
            System.out.println("\n\n" + mdx + "\n\n");

            return q.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void buildQueryDimensions(QueryModel queryModel, Cube cube, Query q) throws OlapException {

        setupDimension(q, queryModel.getColumns(), Axis.COLUMNS);
        setupDimension(q, queryModel.getRows(), Axis.ROWS);



    }

    private void setupDimension(Query q, List<DimensionEntry> row, Axis axis) throws OlapException {

        // Gruppera per rotdimension
        Map<String, List<DimensionEntry>> perRoot = row.stream().collect(Collectors.groupingBy(dim -> dim.nth(0)));

        for (Map.Entry<String, List<DimensionEntry>> entry : perRoot.entrySet()) {
            QueryDimension dimension = q.getDimension(entry.getKey());

            if (entry.getKey().equals("Measures")) {
                handleMeasures(entry, dimension);
            } else {
                handleDimensions(entry, dimension);
            }
            q.getAxis(axis).addDimension(dimension);
        }
    }

    private void handleDimensions(Map.Entry<String, List<DimensionEntry>> entry, QueryDimension dimension) throws OlapException {
        for (DimensionEntry dimensionEntry : entry.getValue()) {
            if (dimension != null && dimension.getDimension() != null && dimension.getDimension().getHierarchies() != null) {

                NamedList<Hierarchy> hierarchies = dimension.getDimension().getHierarchies();
                for (Hierarchy h : hierarchies) {
                    if (h.getName().equalsIgnoreCase((dimensionEntry.nth(1)))) {
                        dimension.include( dimensionEntry.getOperator(), h.getDefaultMember());
                    }
                }
            }

        }
    }

    private void handleMeasures(Map.Entry<String, List<DimensionEntry>> entry, QueryDimension dimension) throws OlapException {
        for (DimensionEntry dimensionEntry : entry.getValue()) {
            NamedList<Member> rootMembers = dimension.getDimension().getDefaultHierarchy().getRootMembers();
            for (Member m : rootMembers) {
                if (dimensionEntry.nth(1).contains(m.getName())) {
                    dimension.include(Selection.Operator.MEMBER, m);
                }
            }
        }
    }
}