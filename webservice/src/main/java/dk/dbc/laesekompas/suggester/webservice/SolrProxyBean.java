package dk.dbc.laesekompas.suggester.webservice;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of suggester-laesekompas-webservice
 *
 * suggester-laesekompas-webservice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * suggester-laesekompas-webservice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * File created: 18/06/2019
 */

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Stateless
@Path("solr-proxy")
public class SolrProxyBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrProxyBean.class);
    /**
     * SUGGESTER_SOLR_URL is the URL for the suggestion SolR that this webservice uses. This service is heavily coupled
     * with this SolRs interface, see https://gitlab.dbc.dk/os-scrum/suggester-laesekompas-solr for exact SolR config
     */
    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String solrUrl;

    Client client;

    @PostConstruct
    public void initialize() {
        if(!this.solrUrl.endsWith("/solr")) {
            this.solrUrl = this.solrUrl +"/solr";
        }
        client = ClientBuilder.newClient();
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response solrProxy(@Context UriInfo uriInfo) {
        String format = uriInfo.getQueryParameters().getFirst("wt");
        if(format != null && !format.equals("json")) {
            return Response.serverError().entity("Only JSON output supported!").build();
        }
        MDC.put("requestType", "solr-proxy");
        MDC.put("query", uriInfo.getRequestUri().getRawQuery());
        LOGGER.info("solr-proxy called");
        WebTarget target = client.target(solrUrl).path("search").path("select");
        for (Map.Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            target = target.queryParam(e.getKey(), e.getValue().toArray());
        }
        Response resp = target.request(MediaType.APPLICATION_JSON).get();
        MDC.clear();
        return Response.ok(resp.readEntity(String.class)).build();
    }
}
