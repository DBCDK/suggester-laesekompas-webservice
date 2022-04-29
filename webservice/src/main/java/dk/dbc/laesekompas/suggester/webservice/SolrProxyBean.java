package dk.dbc.laesekompas.suggester.webservice;

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
     * LAESEKOMPAS_SOLR_URL is the URL for the suggestion SolR that this webservice uses. This service is heavily coupled
     * with this SolRs interface, see https://gitlab.dbc.dk/os-scrum/suggester-laesekompas-solr for exact SolR config
     */
    @Inject
    @ConfigProperty(name = "LAESEKOMPAS_SOLR_URL")
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
