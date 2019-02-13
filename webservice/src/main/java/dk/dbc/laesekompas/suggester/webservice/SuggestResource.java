package dk.dbc.laesekompas.suggester.webservice;

import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestSolrClient;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import org.apache.solr.client.solrj.SolrServerException;
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

@Stateless
@Path("suggest")
public class SuggestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestResource.class);
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAll(@QueryParam("query") String query) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }

        SuggestQueryResponse response = solr.suggestQuery(query, SuggestType.ALL);
        LOGGER.info(response.getInfix().toString());

        return Response.ok().entity(response.getInfix()).build();
    }
}
