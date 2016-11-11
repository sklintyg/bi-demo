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
package se.inera.intyg.bi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olap4j.CellSet;
import org.olap4j.layout.CellSetFormatter;
import org.olap4j.layout.RectangularCellSetFormatter;
import org.olap4j.query.Selection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.intyg.bi.web.service.OlapService;
import se.inera.intyg.bi.web.service.dto.CubeModel;
import se.inera.intyg.bi.web.service.dto.DimensionEntry;
import se.inera.intyg.bi.web.service.dto.QueryModel;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by eriklupander on 2016-11-03.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:test-context.xml")
public class OlapServiceTest {


    @Autowired
    private OlapService testee;

    @Test @Ignore
    public void testBuildCubeModel() throws JsonProcessingException {
        CubeModel cubeModel = testee.getCubeModel();
        System.out.println("\n\n" + new ObjectMapper().writeValueAsString(cubeModel));
    }

    @Test  @Ignore
    public void testGetDimensionValues() {
        DimensionEntry dimEntry = new DimensionEntry(null, "Datum", "Datum", "Ar");
        List<String> dimensionValues = testee.getDimensionValues(dimEntry);
        assertTrue(dimensionValues.size() > 0);
    }

    @Test  @Ignore
    public void testExecuteQuery() {

        QueryModel queryModel = new QueryModel();

        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Measures", "Genomsnittlig sjukskrivningslangd"));
        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Intygstyp","Typ", "Typ"));

        queryModel.getRows().add(new DimensionEntry(Selection.Operator.CHILDREN, "Kon", "Kon", "Kon"));
        queryModel.getRows().add(new DimensionEntry(Selection.Operator.CHILDREN, "Datum", "Datum", "Ar"));

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

    @Test @Ignore
    public void testExecuteQueryWithFilter() {

        QueryModel queryModel = new QueryModel();

        DimensionEntry konDimension = new DimensionEntry(Selection.Operator.CHILDREN, "Kon", "Kon", "Kon");
        konDimension.getFilterValues().add(new DimensionEntry(null, "Kon", "Kon", "F"));

        DimensionEntry arDimension = new DimensionEntry(Selection.Operator.CHILDREN, "Datum", "Datum", "Ar");
        arDimension.getFilterValues().add(new DimensionEntry(null, "Datum", "Datum", "2013"));
        arDimension.getFilterValues().add(new DimensionEntry(null, "Datum", "Datum", "2014"));

        queryModel.getRows().add(konDimension);
        queryModel.getColumns().add(arDimension);

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

 //   @Test
    public void testExecuteQueryWithMeasures() {

        QueryModel queryModel = new QueryModel();
        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Measures", "Antal intyg"));
        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Measures", "Genomsnittlig sjukskr. langd"));
        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Kon", "Kon"));
        queryModel.getRows().add(new DimensionEntry(Selection.Operator.CHILDREN, "Ar", "Ar"));

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

  //  @Test
    public void testComplexQueryWithMeasures() {

        QueryModel queryModel = new QueryModel();
        queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Measures", "Antal intyg"));
       // queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Measures", "Genomsnittlig sjukskr. langd"));
     //   queryModel.getColumns().add(new DimensionEntry(Selection.Operator.CHILDREN, "Kon", "Kon"));
        queryModel.getRows().add(new DimensionEntry(Selection.Operator.CHILDREN, "Ar", "Ar"));
        queryModel.getRows().add(new DimensionEntry(Selection.Operator.CHILDREN, "Vardenhet", "Namn"));

        try {
            System.out.println(new ObjectMapper().writeValueAsString(queryModel));
        } catch (JsonProcessingException e) {

        }

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

    @Test @Ignore
    public void test() {
        assertTrue(true);
    }
}
