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
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
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
    Client client;
    WebTarget target;
    private static final Logger log = LoggerFactory.getLogger(StatusBean.class);

    @Inject
    @ConfigProperty(name = "LAESEKOMPAS_SOLR_URL")
    String laesekompasSolrUrl;

    @Inject
    @ConfigProperty(name = "COREPO_SOLR_URL")
    String corepoSolrUrl;

    @PostConstruct
    public void initialize() {
        if(!this.laesekompasSolrUrl.endsWith("/solr")) {
            this.laesekompasSolrUrl = this.laesekompasSolrUrl +"/solr";
        }
        this.client = ClientBuilder.newClient();
    }

    @PreDestroy
    void onDestroy(){
        log.info("SOLR client destroyed");
    }

    /**
     * Status endpoint for monitoring, pings all SolR collections used for making search/suggest, if not all are healthy
     * returns status code 500
     * @param uriInfo from context
     * @return Response: json object {success: true/false, message: "Error message/success"}
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Response getStatus(@Context UriInfo uriInfo) {
        log.debug("StatusBean called...");
        try {
            checkPing(laesekompasSolrUrl + "/" + ALL.getCollection(), "all");
            checkPing(laesekompasSolrUrl + "/" + AUDIO_BOOK.getCollection(), "audio_book");
            checkPing(laesekompasSolrUrl + "/" + E_BOOK.getCollection(), "e_book");
            checkPing(laesekompasSolrUrl + "/search", "search");
            checkPing(corepoSolrUrl + "/solr/cisterne-laesekompas-suggester-lookup", "corepo-solr");
        } catch (SolrServerException|IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Resp(ex.getMessage())).build();
        }
        final List<List<String>> endpoints =
                Arrays.asList(
                        Arrays.asList("search"),
                        Arrays.asList("suggest"),
                        Arrays.asList("suggest", "audio_book"),
                        Arrays.asList("suggest", "e_book"));
        for (List<String> endpointPath : endpoints) {
            String s = checkEndpoint(uriInfo, endpointPath, "eventyr");
            if (s != null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                   .entity(new Resp(s)).build();
            }
        }
        return Response.ok().entity(new Resp()).build();
    }

    /**
     * Check one of the endpoints search, suggest, suggest/e_book, suggest/audio_book.
     * Path is represented by a list, which is the "/"-split of it.
     * Return null if everything is fine, otherwise return an error that we can return as response.
     * @param uriInfo context object giving "current url"
     * @param endpointPath path represented by a list, split by slash
     * @param query some query we expect will return data on all the endpoints.
     * @return null if all is good, an error message otherwise.
     */
    private String checkEndpoint(UriInfo uriInfo, List<String> endpointPath, String query) {
        UriBuilder uBuilder = uriInfo.getBaseUriBuilder();
        for (String pathPart : endpointPath) {
            uBuilder.path(pathPart);
        }
        URI uri = uBuilder.queryParam("query", query).build();
        this.target = client.target(uri);
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
        if (response.getStatus() != HttpStatus.SC_OK) {
            String res = String.format("Laesekompas self call went bad with endpoint %s and query %s",
                    String.join("/", endpointPath), query);
            log.error(res);
            return res;
        }
        List<Object> entityList = response.readEntity(new GenericType<List<Object>>() {});
        if (entityList == null || entityList.size() == 0) {
            String res = String.format("Laesekompas self call with endpoint %s and query %s did not return data",
                    String.join("/", endpointPath), query);
            return res;
        }
        return null;
    }

    /**
     * Send a ping to the given solr.
     * @param solrName name of the solr
     * @throws IOException
     * @throws SolrServerException
     */
    private void checkPing(String solrUrl, String solrName) throws IOException, SolrServerException {
        try(Http2SolrClient solr = new Http2SolrClient.Builder(solrUrl).useHttp1_1(true).build()) {
            SolrPingResponse ping = solr.ping();
            if (ping.getStatus() != 0) {
                log.error("Error encountered when pinging solr: " + solrName);
                throw new SolrServerException(ping.getException().getMessage());
            }
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
