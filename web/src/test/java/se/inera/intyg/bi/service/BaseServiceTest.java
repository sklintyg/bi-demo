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

import org.junit.BeforeClass;

import java.net.URISyntaxException;

/**
 * Created by eriklupander on 2016-11-15.
 */
public abstract class BaseServiceTest {

    /**
     * Ugly hack to fix issue with Mondrian bootstrapping requiring absolute file URLs where we determine
     * the 'bi.resources.folder' folder using the known position of the logback.xml file.
     *
     */
    @BeforeClass
    public static void setPath() {
        try {
            // Get the absoluts path to src/main/resources/logback.xml and then chop off the logback part. Ugly as h-ll...
            String absPath = StatOlapServiceTest.class.getClassLoader().getResource("logback.xml").toURI().getPath();
            absPath = absPath.substring(0, absPath.lastIndexOf("/"));   // TODO use File.separatorChar or similar.
            System.setProperty("bi.resources.folder", absPath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
