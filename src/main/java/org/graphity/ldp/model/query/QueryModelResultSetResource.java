/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graphity.ldp.model.query;

import com.hp.hpl.jena.query.ResultSet;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.graphity.ldp.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
@Produces({org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_XML + "; charset=UTF-8", org.graphity.MediaType.APPLICATION_SPARQL_RESULTS_JSON + "; charset=UTF-8"})
public interface QueryModelResultSetResource extends Resource, org.graphity.model.query.QueryModelResultSetResource
{
    @GET @Override ResultSet getResultSet();
}
