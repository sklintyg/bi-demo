/**
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.intyg.bi.web.service.OlapService;
import se.inera.intyg.bi.web.service.dto.cube.CubeModel;
import se.inera.intyg.bi.web.service.dto.query.QueryDimension;
import se.inera.intyg.bi.web.service.dto.query.QueryModel;

import java.io.PrintWriter;

import static org.junit.Assert.assertNotNull;

/**
 * Created by eriklupander on 2016-11-03.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:test-context.xml")
@ActiveProfiles({"stat"})
public class StatOlapServiceTest extends BaseServiceTest {

    @Autowired
    private OlapService testee;



    @Test   @Ignore
    public void testBuildCubeModel() throws JsonProcessingException {
        CubeModel cubeModel = testee.getCubeModel();
        System.out.println("\n\n" + new ObjectMapper().writeValueAsString(cubeModel));
    }

    @Test @Ignore
    public void testExecuteQuery() {

        QueryModel queryModel = new QueryModel();

        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Measures", "Antal sjukfall"));
        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Measures", "Genomsnittl sjukskrivningslangd"));
        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Kon", "Kon", "Kon"));
        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Sjukskrivningsgrad", "Dagar", "Dagar"));
        queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Lan", "Lan", "Lan"));
        queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Alder", "Ar", "Ar"));

     //   queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Datum", "Datum", "Datum"));    // Including this kills query...

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

    @Test @Ignore
    public void testExecuteQueryForLakare() {

        QueryModel queryModel = new QueryModel();

        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Measures", "Antal sjukfall"));
        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Sjukskrivningsgrad", "Dagar", "Dagar"));
        queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Lakare", "HsaId", "HsaId"));

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }

    @Test @Ignore
    public void testExecuteQueryForDates() {

        QueryModel queryModel = new QueryModel();

        queryModel.getColumns().add(new QueryDimension(Selection.Operator.CHILDREN, "Measures", "Antal sjukfall"));
        queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Datum", "Datum", "Year"));
        //queryModel.getRows().add(new QueryDimension(Selection.Operator.CHILDREN, "Datum", "Datum", "Month"));

        CellSet cellSet = testee.executeQuery(queryModel);
        CellSetFormatter csf = new RectangularCellSetFormatter(false);
        csf.format(cellSet, new PrintWriter(System.out, true));
        assertNotNull(cellSet);
    }


}
