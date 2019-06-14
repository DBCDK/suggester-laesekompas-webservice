package dk.dbc.laesekompas.suggester.webservice;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of microservice-sample
 *
 * microservice-sample is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * microservice-sample is distributed in the hope that it will be useful,
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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.*;

/**
 * Bean containing webservice status endpoint for monitoring purposes
 */
@Stateless
@Path("status")
public class StatusBean {
    HttpSolrClient solr;
    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String suggesterSolrUrl;

    @PostConstruct
    public void initialize() {
        if(!this.suggesterSolrUrl.endsWith("/solr")) {
            this.suggesterSolrUrl = this.suggesterSolrUrl+"/solr";
        }
        this.solr = new HttpSolrClient.Builder(suggesterSolrUrl).build();
    }

    /**
     * Status endpoint for monitoring, pings all SolR collections used for making search/suggest, if not all are healthy
     * returns status code 500
     * @return Response: json object {success: true/false, message: "Error message/success"}
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response getStatus() {
        try {
            solr.setBaseURL(suggesterSolrUrl+"/"+ ALL.getCollection());
            checkPing();
            solr.setBaseURL(suggesterSolrUrl+"/"+ AUDIO_BOOK.getCollection());
            checkPing();
            solr.setBaseURL(suggesterSolrUrl+"/"+ E_BOOK.getCollection());
            checkPing();
            solr.setBaseURL(suggesterSolrUrl+"/search");
            checkPing();
            return Response.ok().entity(new Resp()).build();
        } catch (SolrServerException|IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Resp(ex.getMessage())).build();
        }
    }

    private void checkPing() throws IOException, SolrServerException {
        SolrPingResponse ping = solr.ping();
        if (ping.getStatus() != 0) {
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