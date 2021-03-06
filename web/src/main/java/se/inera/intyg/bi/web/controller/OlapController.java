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
package se.inera.intyg.bi.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.olap4j.CellSet;
import org.olap4j.layout.RectangularCellSetFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.bi.web.service.OlapService;
import se.inera.intyg.bi.web.service.dto.cube.CubeModel;
import se.inera.intyg.bi.web.service.dto.cube.FilterModel;
import se.inera.intyg.bi.web.service.dto.query.QueryDimension;
import se.inera.intyg.bi.web.service.dto.query.QueryModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by eriklupander on 2016-10-30.
 */
@RestController
@RequestMapping("/api")
public class OlapController {

    @Autowired
    OlapService olapService;


    @RequestMapping(method = RequestMethod.POST, value = "/mdx")
    public String query(@RequestBody String mdxQuery) {
        String result = olapService.query(mdxQuery);
        return result;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/model", produces = "text/plain;charset=UTF-8")
    public String query(@RequestBody QueryModel queryModel) {
        CellSet cellSet = olapService.executeQuery(queryModel);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        RectangularCellSetFormatter cellSetFormatter = new RectangularCellSetFormatter(false);
        cellSetFormatter.format(cellSet, pw);
        return sw.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/mdx/dimensions", produces = "application/json;charset=utf-8")
    public String getDimensions() {
        CubeModel cm = olapService.getCubeModel();
        try {
            return  new ObjectMapper().writeValueAsString(cm);
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/mdx/dimension/values", produces = "application/json;charset=utf-8")
    public List<FilterModel> getDimensionValues(@RequestBody String uniqueName) {
        QueryDimension dimEntry = new QueryDimension(null, uniqueName.split("\\."));
        List<FilterModel> dimensionValues = olapService.getDimensionValues(dimEntry);

        return dimensionValues;
    }
}
