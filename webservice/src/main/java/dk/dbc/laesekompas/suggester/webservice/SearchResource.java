package dk.dbc.laesekompas.suggester.webservice;

import dk.dbc.laesekompas.suggester.webservice.solr.SuggestSolrClient;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);
    private SuggestSolrClient solr;

    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    private String suggesterSolrUrl;

    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS", defaultValue = "10")
    private Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        if(!this.suggesterSolrUrl.endsWith("/solr")) {
            this.suggesterSolrUrl = this.suggesterSolrUrl+"/solr";
        }
        this.solr = new SuggestSolrClient.Builder(suggesterSolrUrl).build();
        LOGGER.info("config/MAX_NUMBER_SUGGESTIONS: {}", maxNumberSuggestions);
    }

    private static final Function<String, SolrParams> searchParams = query -> new MapSolrParams(new HashMap<String, String>() {{
            put(CommonParams. Q,query);
            put("defType", "dismax");
            put("qf", "author^6.0 title^5.0 all abstract");
            put("bf", "log(loans)");
        }});

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAll(@QueryParam("query") String query) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }
        QueryResponse solrResponse = solr.query("search", searchParams.apply(query));
        return Response.ok().entity(solrResponse.getResults()).build();
    }
}
