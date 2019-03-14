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
 * File created: 20/02/2019
 */

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;

@Stateless
@Path("search")
public class SearchResource {
    public static final String SOLR_FULL_TEXT_QUERY = "author^6.0 title^5.0 all abstract";
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);
    HttpSolrClient solr;

    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String searchSolrUrl;

    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS", defaultValue = "10")
    Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        if(!this.searchSolrUrl.endsWith("/solr")) {
            this.searchSolrUrl = this.searchSolrUrl +"/solr";
        }
        this.solr = new HttpSolrClient.Builder(searchSolrUrl).build();
        LOGGER.info("config/MAX_NUMBER_SUGGESTIONS: {}", maxNumberSuggestions);
    }

    private static final Function<SearchParams, SolrParams> solrSearchParams = params -> new MapSolrParams(new HashMap<String, String>() {{
            String qf;
            switch (params.field) {
                case "author":
                    qf = params.exact ? "author_exact" : "author";
                    break;
                case "title":
                    qf = params.exact ? "title_exact" : "title";
                    break;
                case "pid":
                    qf = "pid";
                    break;
                case "workid":
                    qf = "workid";
                    break;
                default:
                    qf = SOLR_FULL_TEXT_QUERY;
                    break;
            }
            put(CommonParams.Q, params.query);
            put("defType", "dismax");
            put("qf", qf);
            put("bf", "log(loans)");
            put(CommonParams.ROWS, Integer.toString(params.rows));
        }});

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("query") String query,
                           @DefaultValue("") @QueryParam("field") String field,
                           @DefaultValue("false") @QueryParam("exact") boolean exact,
                           @DefaultValue("10") @QueryParam("rows") int rows) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }
        QueryResponse solrResponse = solr.query("search", solrSearchParams.apply(new SearchParams(query, field, exact, rows)));
        return Response.ok().entity(solrResponse.getResults()).build();
    }

    // POJO to pass 4 arguments to solr params function
    private class SearchParams {
        String query;
        String field;
        boolean exact;
        int rows;

        SearchParams(String query, String field, boolean exact, int rows) {
            this.query = query;
            this.field = field;
            this.exact = exact;
            this.rows = rows;
        }

        @Override
        public String toString() {
            return query+"|"+field+"|"+exact+"|"+rows;
        }
    }
}
