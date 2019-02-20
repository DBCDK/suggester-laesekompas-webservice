package dk.dbc.laesekompas.suggester.webservice.solr;
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

import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TagSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TitleSuggestionEntity;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SolrLaeskompasSuggester {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrLaeskompasSuggester.class);
    private SolrClient solrClient;

    public SolrLaeskompasSuggester(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SuggestQueryResponse suggestQuery(String query, SuggestType suggestType) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/"+suggestType.getCollection()+"/suggest");
        solrQuery.setParam("suggest.q", query);
        QueryResponse resp = solrClient.query(solrQuery);
        SuggesterResponse suggesterResponse = resp.getSuggesterResponse();


        SuggestQueryResponse res = new SuggestQueryResponse();
        Map<String, List<Suggestion>> suggestionHandles = suggesterResponse.getSuggestions();
        try {
            List<SuggestionEntity> infixSuggestions = suggestionHandles.get("infix").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            List<SuggestionEntity> infixBlendedSuggestions = suggestionHandles.get("blended_infix").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            List<SuggestionEntity> fuzzySuggestions = suggestionHandles.get("fuzzy").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            res.setInfix(infixSuggestions);
            res.setInfixBlended(infixBlendedSuggestions);
            res.setFuzzy(fuzzySuggestions);
        } catch(RuntimeException e) {
            LOGGER.error("Error parsing SolR suggest response: {}", e);
            throw new SolrServerException("Problem parsing SolR suggest response");
        }
        return res;
    }

    public static final Function<Suggestion, SuggestionEntity> mapToSuggestionEntity = suggestion -> {
        String term = suggestion.getTerm();
        String[] suggestionPayload = suggestion.getPayload().split("\\|");
        switch (suggestionPayload[0]) {
            case "TAG":
                return new TagSuggestionEntity(
                        term,
                        suggestionPayload[1],
                        Integer.parseInt(suggestionPayload[2]),
                        suggestionPayload[3]
                );
            case "AUTHOR":
                return new AuthorSuggestionEntity(
                        term,
                        suggestionPayload[1]
                );
            case "TITLE":
                return new TitleSuggestionEntity(
                        term,
                        suggestionPayload[1],
                        suggestionPayload[2],
                        suggestionPayload[3],
                        suggestionPayload[4]
                );
            default:
                LOGGER.error("Received the following suggestion from SolR which did not follow scheme: {}",
                        suggestion.toString());
                throw new RuntimeException("Recieved suggestion which did not follow the scheme...");
        }
    };
}
