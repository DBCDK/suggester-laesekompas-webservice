package dk.dbc.laesekompas.suggester.webservice;

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

@Stateless
@Path("suggest")
public class SuggestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestResource.class);

    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS")
    private Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        LOGGER.info("config/MAX_NUMBER_SUGGESTIONS: {}", maxNumberSuggestions);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAll(@QueryParam("query") String query) {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }
        return Response.ok().build();
    }
}
