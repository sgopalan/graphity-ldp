/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.graphity.server;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.sun.jersey.api.core.ResourceConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.server.model.GraphStoreBase;
import org.graphity.server.model.QueriedResourceBase;
import org.graphity.server.model.SPARQLEndpointBase;
import org.graphity.server.provider.*;
import org.graphity.server.vocabulary.GS;
import org.graphity.server.vocabulary.VoID;
import org.openjena.riot.SysRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graphity Server JAX-RS application base class.
 * Can be extended or used as it is (needs to be registered in web.xml).
 * Needs to register JAX-RS root resource classes and providers.
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html">JAX-RS Application</a>
 * @see <a href="http://docs.oracle.com/cd/E24329_01/web.1211/e24983/configure.htm#CACEAEGG">Packaging the RESTful Web Service Application Using web.xml With Application Subclass</a>
 */
public class ApplicationBase extends javax.ws.rs.core.Application
{
    @Context ResourceConfig resourceConfig;
    @Context ServletContext servletContext;

    private static final Logger log = LoggerFactory.getLogger(ApplicationBase.class);

    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();

    /**
     * Initializes root resource classes and provider singletons
     */
    public ApplicationBase()
    {
	classes.add(QueriedResourceBase.class); // handles all
	classes.add(SPARQLEndpointBase.class); // handles /sparql queries
	classes.add(GraphStoreBase.class); // handles /service updates

	singletons.add(new ModelProvider());
	singletons.add(new ResultSetWriter());
	singletons.add(new QueryParamProvider());
	singletons.add(new QueryFormParamProvider());
	singletons.add(new UpdateRequestFormParamProvider());
    }

    /**
     * Initializes (post construction) DataManager, its LocationMapper and Locators, and Context
     * 
     * @see org.graphity.util.manager.DataManager
     * @see org.graphity.util.locator
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/FileManager.html">FileManager</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html">LocationMapper</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html">Locator</a>
     * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/sparql/util/Context.html">Context</a>
     */
    @PostConstruct
    public void init()
    {
	if (log.isDebugEnabled()) log.debug("Application.init() with ResourceConfig: {} and SerlvetContext: {}", getResourceConfig(), getServletContext());
	SysRIOT.wireIntoJena(); // enable RIOT parser
	// WARNING! ontology caching can cause concurrency/consistency problems
	OntDocumentManager.getInstance().setCacheModels(false);
	
	if (getResourceConfig().getProperty(VoID.sparqlEndpoint.getURI()) == null)
	    throw new IllegalArgumentException("No SPARQL endpoint URI specified in web.xml");

	{
	    String endpointURI = (String)getResourceConfig().getProperty(VoID.sparqlEndpoint.getURI());
	    String authUser = (String)getResourceConfig().getProperty(Service.queryAuthUser.getSymbol());
	    String authPwd = (String)getResourceConfig().getProperty(Service.queryAuthPwd.getSymbol());
	    if (authUser != null && authPwd != null) configureServiceContext(endpointURI, authUser, authPwd);
	}
	
	if (getResourceConfig().getProperty(GS.sparqlGraphStore.getURI()) != null)
	{
	    String graphStoreURI = (String)getResourceConfig().getProperty(GS.sparqlGraphStore.getURI());
	    // reuses SPARQL query endpoint authentication properties -- not ideal
	    String authUser = (String)getResourceConfig().getProperty(Service.queryAuthUser.getSymbol());
	    String authPwd = (String)getResourceConfig().getProperty(Service.queryAuthPwd.getSymbol());
	    if (authUser != null && authPwd != null) configureServiceContext(graphStoreURI, authUser, authPwd);
	}
	else
	{
	    if (log.isWarnEnabled()) log.warn("No SPARQL Graph Store URI specified in web.xml. The server will be read-only.");
	}
    }

    /**
     * Configures HTTP Basic authentication for SPARQL endpoint context
     * 
     * @param endpointURI the endpoint to be configured
     * @param authUser username
     * @param authPwd password
     * @see <a href="http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/sparql/util/Context.html">Context</a>
     */
    public void configureServiceContext(String endpointURI, String authUser, String authPwd)
    {
	if (endpointURI == null) throw new IllegalArgumentException("SPARQL endpoint URI cannot be null");
	if (authUser == null) throw new IllegalArgumentException("SPARQL endpoint authentication username cannot be null");
	if (authPwd == null) throw new IllegalArgumentException("SPARQL endpoint authentication password cannot be null");

	if (log.isDebugEnabled()) log.debug("Setting username/password credentials for SPARQL endpoint: {}", endpointURI);
	com.hp.hpl.jena.sparql.util.Context queryContext = new com.hp.hpl.jena.sparql.util.Context();
	queryContext.put(Service.queryAuthUser, authUser);
	queryContext.put(Service.queryAuthPwd, authPwd);
	Map<String,com.hp.hpl.jena.sparql.util.Context> serviceContext = new HashMap<String,com.hp.hpl.jena.sparql.util.Context>();

	serviceContext.put(endpointURI, queryContext);
	ARQ.getContext().put(Service.serviceContext, serviceContext);
    }
    
    /**
     * Provides JAX-RS root resource classes.
     * 
     * @return set of root resource classes
     * @see org.graphity.server.model
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getClasses()">Application.getClasses()</a>
     */
    @Override
    public Set<Class<?>> getClasses()
    {	
        return classes;
    }

    /**
     * Provides JAX-RS singleton objects (e.g. resources or Providers)
     * 
     * @return set of singleton objects
     * @see org.graphity.server.provider
     * @see <a href="http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/Application.html#getSingletons()">Application.getSingletons()</a>
     */
    @Override
    public Set<Object> getSingletons()
    {
	return singletons;
    }

    /**
     * Returns resource configuration
     * 
     * @return injected ResourceConfig
     */
    public ResourceConfig getResourceConfig()
    {
	return resourceConfig;
    }

    /**
     * Returns servlet context
     * 
     * @return injected ServletContext
     */
    public ServletContext getServletContext()
    {
	return servletContext;
    }

}