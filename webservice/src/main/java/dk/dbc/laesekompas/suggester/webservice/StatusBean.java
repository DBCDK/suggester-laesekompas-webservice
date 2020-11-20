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
 * File created: 30/04/2019
 */

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpStatus;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.ALL;
import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.AUDIO_BOOK;
import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.E_BOOK;

/**
 * Bean containing webservice status endpoint for monitoring purposes
 */
@Stateless
@Path("status")
public class StatusBean {
    HttpSolrClient solr;
    Client client;
    WebTarget target;
    private static final Logger log = LoggerFactory.getLogger(StatusBean.class);

    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String suggesterSolrUrl;

    @Inject
    @ConfigProperty(name = "COREPO_SOLR_URL")
    String corepoSolrUrl;

    @PostConstruct
    public void initialize() {
        if(!this.suggesterSolrUrl.endsWith("/solr")) {
            this.suggesterSolrUrl = this.suggesterSolrUrl+"/solr";
        }
        this.solr = new HttpSolrClient.Builder(suggesterSolrUrl).build();
        this.client = ClientBuilder.newClient();
    }

    /**
     * Status endpoint for monitoring, pings all SolR collections used for making search/suggest, if not all are healthy
     * returns status code 500
     * @return Response: json object {success: true/false, message: "Error message/success"}
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @SuppressWarnings("javadoc")
    public Response getStatus(@Context UriInfo uriInfo) {
        log.debug("StatusBean called...");
        try {
            solr.setBaseURL(suggesterSolrUrl+"/"+ ALL.getCollection());
            checkPing("all");
            solr.setBaseURL(suggesterSolrUrl+"/"+ AUDIO_BOOK.getCollection());
            checkPing("audio_book");
            solr.setBaseURL(suggesterSolrUrl+"/"+ E_BOOK.getCollection());
            checkPing("e_book");
            solr.setBaseURL(suggesterSolrUrl+"/search");
            checkPing("search");
            solr.setBaseURL(corepoSolrUrl+"/solr/cisterne-laesekompas-suggester-lookup");
            checkPing("corepo-solr");
            URI uri = uriInfo.getBaseUriBuilder()
                    .path("search")
                    .queryParam("query","eventyr")
                    .build();
            this.target = client.target(uri);
            Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            if (response.getStatus() != HttpStatus.SC_OK) {
                log.error("Læsekompas calling itself went bad!");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Resp("Laesekompas self call went bad")).build();
            }
            List<Object> entityList = response.readEntity(new GenericType<List<Object>>() {});
            if (entityList == null || entityList.size() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Resp("Laesekompas self call did not return data")).build();
            }
            return Response.ok().entity(new Resp()).build();
        } catch (SolrServerException|IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Resp(ex.getMessage())).build();
        }
    }

    private void checkPing(String solrName) throws IOException, SolrServerException {
        SolrPingResponse ping = solr.ping();
        if (ping.getStatus() != 0) {
            log.error("Error encountered when pinging solr: " + solrName);
            throw new SolrServerException(ping.getException().getMessage());
        }
    }

    public static class Resp {

        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public boolean ok;
        @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
        public String text;

        public Resp() {
            ok = true;
            text = "Success";
        }

        public Resp(String diag) {
            ok = false;
            text = diag;
        }
    }
}
