package dk.dbc.laesekompas.suggester.webservice;

import dk.dbc.laesekompas.suggester.webservice.solr.SolrLaesekompasSuggester;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TagSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TitleSuggestionEntity;
import org.apache.solr.client.solrj.SolrServerException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Stateless
@Path("suggest")
public class SuggestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestResource.class);
    SolrLaesekompasSuggester suggester;

    @Inject
    SolrBean solrBean;

    /**
     * MAX_NUMBER_SUGGESTIONS is the maximum number of suggestion that should be returned by all suggest endpoints.
     * Should match the number of suggestions given by the suggestion SolR, a parameter that is statically configured
     * on the SolR.
     */
    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS", defaultValue = "10")
    Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        if (solrBean == null || solrBean.getLaesekompasSolr() == null) {
            throw new RuntimeException("Configuration stuff - WRONG!");
        }
        this.suggester = new SolrLaesekompasSuggester(solrBean.getLaesekompasSolr());
    }

    @PreDestroy
    void onDestroy(){
        LOGGER.info("SuggestResource destroyed");
    }

    private Response suggest(SuggestType suggestType, String query) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }
        MDC.put("requestType", "suggest");
        MDC.put("query", query);
        MDC.put("collection", suggestType.getCollection());

        LOGGER.info("suggestion performed with query: {}, collection: {}", query, suggestType.toString());

        SuggestQueryResponse response = suggester.suggestQuery(query, suggestType);
        // Concatenate results in same order as suggester SolR proposed, preferring infix, then blended_infix,
        // then fuzzy. Duplicates are combined, by picking the "highest" suggested. LinkedHashMap is a map preserving
        // operations order when iterated through, so first inserted is first iterated.
        LinkedHashMap<String, SuggestionEntity> duplicateRemover = new LinkedHashMap<>();
        List<SuggestionEntity> analyzer = response.getAnalyzer();
        List<SuggestionEntity> infixBlended = response.getInfixBlended();
        List<SuggestionEntity> fuzzy = response.getFuzzy();
        analyzer.addAll(infixBlended);
        analyzer.addAll(fuzzy);
        for(SuggestionEntity suggestion : analyzer) {
            switch (suggestion.getType()) {
                case "TAG":
                    TagSuggestionEntity tag = (TagSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("tag_id:" + tag.getId(), suggestion);
                    break;
                case "AUTHOR":
                    AuthorSuggestionEntity author = (AuthorSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("author_name:" + author.getAuthorName(), author);
                    break;
                case "TITLE":
                    TitleSuggestionEntity title = (TitleSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("pid:" + title.getPid(), title);
                    break;
                default:
                    break;

            }
        }
        // Convert to list, keeping order
        List<SuggestionEntity> suggestions = new ArrayList<>();
        duplicateRemover.forEach((k,s) -> suggestions.add(s));

        MDC.clear();
        return Response.ok().entity(suggestions.subList(0, Integer.min(maxNumberSuggestions, suggestions.size()))).build();
    }

    /**
     * Gives suggestions for all book types
     * @param query query to give suggestions from
     * @return List of suggestions
     * @throws SolrServerException Thrown if the SolR client throws any exceptions
     * @throws IOException Thrown if the network connection to the SolR fails
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAll(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.ALL, query);
    }

    /**
     * Gives suggestions for only EBooks
     * @param query query to give suggestions from
     * @return List of suggestions
     * @throws SolrServerException Thrown if the SolR client throws any exceptions
     * @throws IOException Thrown if the network connection to the SolR fails
     */
    @GET
    @Path("/e_book")
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestEBooks(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.E_BOOK, query);
    }

    /**
     * Gives suggestions for only audio books
     * @param query query to give suggestions from
     * @return List of suggestions
     * @throws SolrServerException Thrown if the SolR client throws any exceptions
     * @throws IOException Thrown if the network connection to the SolR fails
     */
    @GET
    @Path("/audio_book")
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAudioBooks(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.AUDIO_BOOK, query);
    }
}
