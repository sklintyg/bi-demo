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
package se.inera.intyg.bi.web.service.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eriklupander on 2016-11-02.
 */
public class QueryModel {

    private List<DimensionEntry> rows = new ArrayList<>();
    private List<DimensionEntry> columns = new ArrayList<>();

    private boolean includeEmpty = true;
    private boolean swapAxis = false;

    public List<DimensionEntry> getRows() {
        return rows;
    }

    public void setRows(List<DimensionEntry> rows) {
        this.rows = rows;
    }

    public List<DimensionEntry> getColumns() {
        return columns;
    }

    public void setColumns(List<DimensionEntry> columns) {
        this.columns = columns;
    }

    public boolean getIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }

    public boolean getSwapAxis() {
        return swapAxis;
    }

    public void setSwapAxis(boolean swapAxis) {
        this.swapAxis = swapAxis;
    }
}
