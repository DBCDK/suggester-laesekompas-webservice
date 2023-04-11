package dk.dbc.laesekompas.suggester.webservice.solr;

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
 * File created: 20/02/2019
 */
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TagSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TitleSuggestionEntity;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.request.GenericSolrRequest;
import org.apache.solr.client.solrj.response.SimpleSolrResponse;
import org.apache.solr.common.util.NamedList;

public class SolrLaesekompasSuggester {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrLaesekompasSuggester.class);
    private final SolrClient solrClient;

    public SolrLaesekompasSuggester(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SuggestQueryResponse suggestQuery(String query, SuggestType suggestType) throws IOException, SolrServerException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/suggest");
        solrQuery.setParam("suggest.q", query);
        SolrRequest<SimpleSolrResponse> solrRequest = new GenericSolrRequest(SolrRequest.METHOD.POST, "/suggest", solrQuery);
        NamedList<Object> solrResponse = solrClient.request(solrRequest, suggestType.getCollection());
        if (solrResponse != null) {
            try {
                SuggestQueryResponse res = new SuggestQueryResponse();
                SolrReader.of(solrResponse)
                        .asMap()
                        .get("suggest")
                        .asMap()
                        .take("analyzer", analyzer -> {
                          ArrayList<SuggestionEntity> entites = solrSuggetionsToList(analyzer);
                          res.setAnalyzer(entites);
                      })
                        .take("infix", infix -> {
                          ArrayList<SuggestionEntity> entites = solrSuggetionsToList(infix);
                          res.setInfix(entites);
                      })
                        .take("blended_infix", blendedInfix -> {
                          ArrayList<SuggestionEntity> entites = solrSuggetionsToList(blendedInfix);
                          entites.sort(BLENDED_INFIX_SORT);
                          res.setInfixBlended(entites);
                      })
                        .take("fuzzy", fuzzy -> {
                          ArrayList<SuggestionEntity> entites = solrSuggetionsToList(fuzzy);
                          res.setFuzzy(entites);
                      });
                return res;
            } catch (RuntimeException e) {
                LOGGER.error("Error parsing SolR suggest response: {}", e);
                LOGGER.debug("Error parsing SolR suggest response: {}", solrResponse);
                throw new SolrServerException("Problem parsing SolR suggest response");
            }
        }
        LOGGER.error("Error getting SolR suggest response for: {}", query);
        throw new SolrServerException("Problem getting SolR suggest response");
    }

    private ArrayList<SuggestionEntity> solrSuggetionsToList(SolrReader.ObjectReader<Object> analyzer) {
        ArrayList<SuggestionEntity> entites = new ArrayList<>();
        analyzer.asMap()
                .forEach((name, suggestion) -> {
                    suggestion.asMap()
                            .get("suggestions")
                            .asList()
                            .forEach(storeEntityAt(entites::add));
                });
        return entites;
    }

    private static Consumer<SolrReader.ObjectReader<Object>> storeEntityAt(Consumer<SuggestionEntity> target) {
        return o -> {
            SolrReader.MapReader m = o.asMap();
            String term = m.get("term").as(String.class).get();
            long weight = m.get("weight").asLong();
            String[] suggestionPayload = m.get("payload").as(String.class).get().split("\\|");
            SuggestionEntity suggestion;
            switch (suggestionPayload[0]) {
                case "TAG":
                    suggestion = new TagSuggestionEntity(
                            term,
                            weight,
                            suggestionPayload[1],
                            Integer.parseInt(suggestionPayload[2]),
                            suggestionPayload[3]
                    );
                    break;
                case "AUTHOR":
                    suggestion = new AuthorSuggestionEntity(
                            term,
                            weight,
                            suggestionPayload[1]
                    );
                    break;
                case "TITLE":
                    suggestion = new TitleSuggestionEntity(
                            term,
                            weight,
                            suggestionPayload[1],
                            suggestionPayload[2],
                            suggestionPayload[3],
                            suggestionPayload[4]
                    );
                    break;
                default:
                    LOGGER.error("Received the following suggestion from SolR which did not follow scheme: {}",
                            m);
                    throw new RuntimeException("Recieved suggestion which did not follow the scheme...");
            }
            target.accept(suggestion);
        };

    }

    private static final Comparator<SuggestionEntity> BLENDED_INFIX_SORT = (se1, se2) -> {
        if (se1.getWeight() < se2.getWeight()) {
            return 1;
        } else if (se1.getWeight() > se2.getWeight()) {
            return -1;
        } else {
            return se1.getMatchedTerm().compareTo(se2.getMatchedTerm());
        }
    };

}
