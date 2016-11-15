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
package se.inera.intyg.bi.web.service.dto.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eriklupander on 2016-11-02.
 */
public class QueryModel {

    private List<QueryDimension> rows = new ArrayList<>();
    private List<QueryDimension> columns = new ArrayList<>();

    private boolean includeEmpty = true;

    public List<QueryDimension> getRows() {
        return rows;
    }

    public void setRows(List<QueryDimension> rows) {
        this.rows = rows;
    }

    public List<QueryDimension> getColumns() {
        return columns;
    }

    public void setColumns(List<QueryDimension> columns) {
        this.columns = columns;
    }

    public boolean getIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }
}
