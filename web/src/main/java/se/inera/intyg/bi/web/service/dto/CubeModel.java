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

/**
 * Created by eriklupander on 2016-11-03.
 */
public class CubeModel {

    private DimensionModel measures;
    private DimensionModel dimensions;

    public DimensionModel getMeasures() {
        return measures;
    }

    public void setMeasures(DimensionModel measures) {
        this.measures = measures;
    }

    public DimensionModel getDimensions() {
        return dimensions;
    }

    public void setDimensions(DimensionModel dimensions) {
        this.dimensions = dimensions;
    }
}
