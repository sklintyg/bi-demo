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
package se.inera.intyg.bi.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.bi.web.service.OlapService;

import javax.ws.rs.core.Response;

/**
 * Created by eriklupander on 2016-10-30.
 */
@RestController
@RequestMapping("/api")
public class MdxController {

    @Autowired
    OlapService olapService;

    @RequestMapping(method = RequestMethod.POST, value = "/mdx")
    public Response query(@RequestBody String mdxQuery) {
        System.out.println("Query: " +  mdxQuery);
        String result = olapService.query(mdxQuery);
        return Response.ok()
                .entity(result)
                .build();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/mdx/dimensions", produces = "application/json")
    public Response getDimensions() {
        String json = olapService.getDimensions();
        return Response.ok()
                .entity(json)
                .build();
    }

}
