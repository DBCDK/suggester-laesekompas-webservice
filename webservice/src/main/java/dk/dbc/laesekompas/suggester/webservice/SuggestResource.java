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
        List<SuggestionEntity> anaylzer = response.getAnalyzer();
        //List<SuggestionEntity> infix = response.getInfix();
        List<SuggestionEntity> infixBlended = response.getInfixBlended();
        List<SuggestionEntity> fuzzy = response.getFuzzy();
        anaylzer.addAll(infixBlended);
        anaylzer.addAll(fuzzy);
        for(SuggestionEntity suggestion : anaylzer) {
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
                    duplicateRemover.putIfAbsent("workid:" + title.getWorkid(),title);
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
