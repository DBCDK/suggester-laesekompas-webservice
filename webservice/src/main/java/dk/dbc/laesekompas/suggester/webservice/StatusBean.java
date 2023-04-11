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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import jakarta.ws.rs.InternalServerErrorException;

import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.ALL;
import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.AUDIO_BOOK;
import static dk.dbc.laesekompas.suggester.webservice.solr.SuggestType.E_BOOK;

/**
 * Bean containing webservice status endpoint for monitoring purposes
 */
@Singleton
@Lock(LockType.READ)
@Path("status")
public class StatusBean {

    private static final Logger log = LoggerFactory.getLogger(StatusBean.class);

    @Inject
    @ConfigProperty(name = "LAESEKOMPAS_SOLR_URL")
    String laesekompasSolrUrl;

    @Inject
    @ConfigProperty(name = "COREPO_SOLR_URL")
    String corepoSolrUrl;

    List<PingCheck> checks;

    @PostConstruct
    public void initialize() {
        if (!this.laesekompasSolrUrl.endsWith("/solr")) {
            this.laesekompasSolrUrl = this.laesekompasSolrUrl + "/solr";
        }

        checks = List.of(new PingCheck(laesekompasSolrUrl + "/" + ALL.getCollection(), "all"),
                         new PingCheck(laesekompasSolrUrl + "/" + AUDIO_BOOK.getCollection(), "audio_book"),
                         new PingCheck(laesekompasSolrUrl + "/" + E_BOOK.getCollection(), "e_book"),
                         new PingCheck(laesekompasSolrUrl + "/search", "search"),
                         new PingCheck(corepoSolrUrl + "/solr/cisterne-laesekompas-suggester-lookup", "corepo-solr"));
    }

    @PreDestroy
    public void destroy() {
        for (PingCheck check : checks) {
            check.close();
        }
        log.info("SOLR client destroyed");
    }

    /**
     * Status endpoint for monitoring, pings all SolR collections used for making search/suggest, if not all are healthy
     * returns status code 500
     *
     * @param uriInfo from context
     * @return Response: json object {success: true/false, message: "Error message/success"}
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    public Resp getStatus(@Context UriInfo uriInfo) {
        log.debug("StatusBean called...");
        checks.forEach(PingCheck::ping);
        return new Resp();
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
    }

    public static class PingCheck {

        private final Http2SolrClient solr;
        private final String solrName;

        public PingCheck(String solrUrl, String solrName) {
            this.solr = new Http2SolrClient.Builder(solrUrl).useHttp1_1(true).build();
            this.solrName = solrName;
        }

        public void ping() {
            try {
                SolrPingResponse ping = solr.ping();
                if (ping.getStatus() != 0) {
                    log.error("Error encountered when pinging solr: " + solrName);
                    throw new SolrServerException(ping.getException().getMessage());
                }
            } catch (SolrServerException | IOException ex) {
                throw new InternalServerErrorException("Error pinging: " + solrName, ex);
            }
        }

        public void close() {
            solr.close();
        }
    }
}
