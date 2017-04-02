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
package se.inera.intyg.bi.web.service.dto.query;

import org.olap4j.query.Selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by eriklupander on 2016-11-03.
 */
public class QueryDimension {

    private Selection.Operator operator = Selection.Operator.MEMBER;
    private List<String> parts = new ArrayList<>();

    // TODO use some kind of interface here so we don't allow values within values.
    private List<QueryDimension> filterValues = new ArrayList<>();

    public QueryDimension() {

    }

    public QueryDimension(Selection.Operator operator, String ... parts) {
        if (parts == null || parts.length == 0) {
            throw new IllegalArgumentException("Cannot create QueryDimension without at least one part.");
        }
        this.operator = operator;
        this.parts = Arrays.asList(parts);
    }

    public Selection.Operator getOperator() {
        return operator;
    }

    public List<String> getParts() {
        return parts;
    }

    public String nth(int i) {
        if (i + 1 > parts.size()) {
            throw new IndexOutOfBoundsException("Parts only contains " + parts.size() + " elems. Asked for " + i + "th");
        }
        return parts.get(i);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String part : parts) {
            buf.append("[").append(part).append("].");
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    public String nonLeaf() {
        StringBuilder buf = new StringBuilder();
        for (int a = 0; a < parts.size() - 1; a++) {
            buf.append(parts.get(a)).append(".");
        }
        buf.setLength(buf.length() - 1);
        return buf.toString();
    }

    public List<QueryDimension> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<QueryDimension> filterValues) {
        this.filterValues = filterValues;
    }

    //    public String first() {
//        return parts.get(0);
//    }
//
//    public String second() {
//        return parts.get(1);
//    }
}
