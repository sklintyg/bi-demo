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
package se.inera.intyg.bi.web.service.dto.cube;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eriklupander on 2016-11-03.
 */
public class DimensionModel {
    private String name;
    private List<DimensionModel> children = new ArrayList<>();

    public DimensionModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<DimensionModel> getChildren() {
        return children;
    }

    public void add(DimensionModel child) {
        this.children.add(child);
    }
}
