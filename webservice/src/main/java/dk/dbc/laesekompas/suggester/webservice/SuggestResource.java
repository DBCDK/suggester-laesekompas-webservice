package dk.dbc.laesekompas.suggester.webservice;

import dk.dbc.laesekompas.suggester.webservice.solr.SolrLaeskompasSuggester;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TagSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TitleSuggestionEntity;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Stateless
@Path("suggest")
public class SuggestResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestResource.class);
    HttpSolrClient solr;
    SolrLaeskompasSuggester suggester;

    @Inject
    @ConfigProperty(name = "SUGGESTER_SOLR_URL")
    String suggesterSolrUrl;

    @Inject
    @ConfigProperty(name = "MAX_NUMBER_SUGGESTIONS", defaultValue = "10")
    Integer maxNumberSuggestions;

    @PostConstruct
    public void initialize() {
        if(!this.suggesterSolrUrl.endsWith("/solr")) {
            this.suggesterSolrUrl = this.suggesterSolrUrl+"/solr";
        }
        this.solr = new HttpSolrClient.Builder(suggesterSolrUrl).build();
        this.suggester = new SolrLaeskompasSuggester(this.solr);
        LOGGER.info("config/MAX_NUMBER_SUGGESTIONS: {}", maxNumberSuggestions);
    }

    private Response suggest(SuggestType suggestType, String query) throws SolrServerException, IOException {
        // We require a query
        if (query == null) {
            return Response.status(400).build();
        }

        SuggestQueryResponse response = suggester.suggestQuery(query, suggestType);
        // Concatenate results in same order as suggester SolR proposed, preferring infix, then blended_infix,
        // then fuzzy. Duplicates are combined, by picking the "highest" suggested. LinkedHashMap is a map preserving
        // operations order when iterated through, so first inserted is first iterated.
        LinkedHashMap<String, SuggestionEntity> duplicateRemover = new LinkedHashMap<>();
        List<SuggestionEntity> infix = response.getInfix();
        List<SuggestionEntity> infixBlended = response.getInfixBlended();
        List<SuggestionEntity> fuzzy = response.getFuzzy();
        infix.addAll(infixBlended);
        infix.addAll(fuzzy);
        for(SuggestionEntity suggestion : infix) {
            int index = infix.indexOf(suggestion);
            switch (suggestion.getType()) {
                case "TAG":
                    TagSuggestionEntity tag = (TagSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("tag_id:"+tag.getId(), suggestion);
                    break;
                case "AUTHOR":
                    AuthorSuggestionEntity author = (AuthorSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("author_name:"+author.getAuthorName(), author);
                    break;
                case "TITLE":
                    TitleSuggestionEntity title = (TitleSuggestionEntity) suggestion;
                    duplicateRemover.putIfAbsent("workid:"+title.getWorkid(),title);
                    break;
            }
        }
        // Convert to list, keeping order
        List<SuggestionEntity> suggestions = new ArrayList<>();
        duplicateRemover.forEach((k,s) -> suggestions.add(s));

        return Response.ok().entity(suggestions.subList(0, Integer.min(maxNumberSuggestions, suggestions.size()))).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAll(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.ALL, query);
    }

    @GET
    @Path("/e_book")
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestEBooks(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.E_BOOK, query);
    }

    @GET
    @Path("/audio_book")
    @Produces(MediaType.APPLICATION_JSON)
    public Response suggestAudioBooks(@QueryParam("query") String query) throws SolrServerException, IOException {
        return suggest(SuggestType.AUDIO_BOOK, query);
    }
}
