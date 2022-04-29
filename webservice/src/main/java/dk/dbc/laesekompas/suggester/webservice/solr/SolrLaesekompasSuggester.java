package dk.dbc.laesekompas.suggester.webservice.solr;

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

public class SolrLaesekompasSuggester {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrLaesekompasSuggester.class);
    private SolrClient solrClient;

    public SolrLaesekompasSuggester(SolrClient solrClient) {
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
            List<SuggestionEntity> analyzerSuggestions = suggestionHandles.get("analyzer").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            List<SuggestionEntity> infixSuggestions = suggestionHandles.get("infix").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            List<SuggestionEntity> infixBlendedSuggestions = suggestionHandles.get("blended_infix").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            List<SuggestionEntity> fuzzySuggestions = suggestionHandles.get("fuzzy").stream().map(mapToSuggestionEntity)
                    .collect(Collectors.toList());
            infixBlendedSuggestions.sort((se1,se2) -> {
                if (se1.getWeight() < se2.getWeight()) {
                    return 1;
                } else if (se1.getWeight() > se2.getWeight()) {
                    return -1;
                } else {
                    return se1.getMatchedTerm().compareTo(se2.getMatchedTerm());
                }
            });
            res.setAnalyzer(analyzerSuggestions);
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
        long weight = suggestion.getWeight();
        String[] suggestionPayload = suggestion.getPayload().split("\\|");
        switch (suggestionPayload[0]) {
            case "TAG":
                return new TagSuggestionEntity(
                        term,
                        weight,
                        suggestionPayload[1],
                        Integer.parseInt(suggestionPayload[2]),
                        suggestionPayload[3]
                );
            case "AUTHOR":
                return new AuthorSuggestionEntity(
                        term,
                        weight,
                        suggestionPayload[1]
                );
            case "TITLE":
                return new TitleSuggestionEntity(
                        term,
                        weight,
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
