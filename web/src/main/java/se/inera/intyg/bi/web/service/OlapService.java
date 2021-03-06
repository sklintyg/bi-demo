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
package se.inera.intyg.bi.web.service;

import org.olap4j.CellSet;
import se.inera.intyg.bi.web.service.dto.cube.CubeModel;
import se.inera.intyg.bi.web.service.dto.cube.FilterModel;
import se.inera.intyg.bi.web.service.dto.query.QueryDimension;
import se.inera.intyg.bi.web.service.dto.query.QueryModel;

import java.util.List;

/**
 * Created by eriklupander on 2016-10-30.
 */
public interface OlapService {

    CubeModel getCubeModel();

    String query(String query);

    List<FilterModel> getDimensionValues(QueryDimension queryDimension);

    CellSet executeQuery(QueryModel queryModel);
}
