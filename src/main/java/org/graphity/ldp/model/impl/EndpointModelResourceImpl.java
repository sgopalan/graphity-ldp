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
package org.graphity.ldp.model.impl;

import com.hp.hpl.jena.query.Query;
import javax.ws.rs.core.*;
import org.graphity.ldp.model.query.EndpointModelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class EndpointModelResourceImpl extends org.graphity.model.impl.EndpointModelResourceImpl implements EndpointModelResource
{
    private static final Logger log = LoggerFactory.getLogger(EndpointModelResourceImpl.class);

    private Request req = null;
    private UriInfo uriInfo = null;
    private MediaType mediaType = org.graphity.MediaType.APPLICATION_RDF_XML_TYPE;

    public EndpointModelResourceImpl(String endpointUri, Query query, 
	UriInfo uriInfo, Request req,
	MediaType mediaType)
    {
	super(endpointUri, query);
	this.req = req;
	this.uriInfo = uriInfo;
	if (mediaType != null) this.mediaType = mediaType;
    }

    @Override
    public UriInfo getUriInfo()
    {
	return uriInfo;
    }

    @Override
    public Request getRequest()
    {
	return req;
    }
    
    @Override
    public Response getResponse()
    {
	if (log.isDebugEnabled()) log.debug("Accept param: {}, writing SPARQL results (XML or JSON)", getMediaType());

	return Response.ok(getModel(), getMediaType()).build();
    }
    
    public MediaType getMediaType()
    {
	return mediaType;
    }

    @Override
    public EntityTag getEntityTag()
    {
	//return super.getEntityTag();
	return null;
    }

}
