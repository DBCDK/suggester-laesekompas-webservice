package dk.dbc.laesekompas.suggester.webservice;

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
 * File created: 15/03/2019
 */
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dbc.laesekompas.suggester.webservice.solr.SolrLaesekompasSuggester;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.mockito.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;

public class SolrLaesekompasSuggesterTest {

    private static final ObjectMapper O = new ObjectMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

    private static SolrLaesekompasSuggester solrLaesekompasSuggester;
    private Http2SolrClient solrClient;

    private class QuerySolrQueryMatcher extends BaseMatcher<SolrQuery> {

        String query;

        public QuerySolrQueryMatcher(String query) {
            this.query = query;
        }

        @Override
        public boolean matches(Object item) {
            if (item == null || SolrQuery.class != item.getClass()) {
                return false;
            }
            return ( (SolrQuery) item ).getParams("suggest.q")[0].equals(query);
        }

        @Override
        public void describeTo(Description description) {

        }
    }

    @Before
    public void setupBean() throws IOException, SolrServerException {
        this.solrClient = Mockito.mock(Http2SolrClient.class);
        solrLaesekompasSuggester = new SolrLaesekompasSuggester(solrClient);
    }

    @Test
    public void testOrderBlendedInfixByWeight() throws IOException, SolrServerException {
        NamedList<Object> solrResponse = new NamedList<>(O.readValue(
                "{suggest: {" +
                " analyzer:{queryterm:{suggestions:[]}}," +
                " infix:{queryterm:{suggestions:[]}}," +
                " blended_infix:{queryterm:{suggestions:[" +
                "  {term: 'jannett', weight: 2, payload: 'AUTHOR|Jannett'}," +
                "  {term: 'scott', weight: 1, payload: 'AUTHOR|Scott'}," +
                "  {term: 'astrid', weight: 3, payload: 'AUTHOR|Astrid'}" +
                " ]}}," +
                " fuzzy:{queryterm:{suggestions:[]}}" +
                "}}", Map.class));
        Mockito.when(solrClient.request(Mockito.any(), Matchers.anyString()))
                .thenReturn(solrResponse);
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_weight", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byWeight1, byWeight2, byWeight3));
    }

    @Test
    public void testOrderBlendedInfixByTermWhenWeightIsEqual() throws IOException, SolrServerException {
        NamedList<Object> solrResponse = new NamedList<>(O.readValue(
                "{suggest: {" +
                " analyzer:{queryterm:{suggestions:[]}}," +
                " infix:{queryterm:{suggestions:[]}}," +
                " blended_infix:{queryterm:{suggestions:[" +
                "  {term: 'c', weight: 1, payload: 'AUTHOR|c'}," +
                "  {term: 'b', weight: 1, payload: 'AUTHOR|b'}," +
                "  {term: 'a', weight: 1, payload: 'AUTHOR|a'}" +
                " ]}}," +
                " fuzzy:{queryterm:{suggestions:[]}}" +
                "}}", Map.class));
        Mockito.when(solrClient.request(Mockito.any(), Matchers.anyString()))
                .thenReturn(solrResponse);
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_term", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byTerm1, byTerm2, byTerm3));
    }

    @Test
    public void testOrderBlendedInfixByTermAndWeight() throws IOException, SolrServerException {
        NamedList<Object> solrResponse = new NamedList<>(O.readValue(
                "{suggest: {" +
                " analyzer:{queryterm:{suggestions:[]}}," +
                " infix:{queryterm:{suggestions:[]}}," +
                " blended_infix:{queryterm:{suggestions:[" +
                "  {term: 'b', weight: 1, payload: 'AUTHOR|b'}," +
                "  {term: 'b', weight: 3, payload: 'AUTHOR|b'}," +
                "  {term: 'a', weight: 1, payload: 'AUTHOR|a'}," +
                "  {term: 'c', weight: 3, payload: 'AUTHOR|c'}," +
                "  {term: 'a', weight: 3, payload: 'AUTHOR|a'}," +
                "  {term: 'g', weight: 2, payload: 'AUTHOR|g'}" +
                " ]}}," +
                " fuzzy:{queryterm:{suggestions:[]}}" +
                "}}", Map.class));
        Mockito.when(solrClient.request(Mockito.any(), Matchers.anyString()))
                .thenReturn(solrResponse);
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_both", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byWeightTerm1, byWeightTerm2,
                                                                                 byWeightTerm3, byWeightTerm4, byWeightTerm5, byWeightTerm6));
    }

    private static final NamedList<Object> testInfixBlendedSortWeightNL = new SimpleOrderedMap<>(
            new AbstractMap.SimpleEntry[] {
                new AbstractMap.SimpleEntry<String, Object>(
                        "suggest", Map.of(
                                "analyzer", Map.of("suggestions", Collections.emptyList()),
                                "infix", Map.of("suggestions", Collections.emptyList()),
                                "blended_infix", Map.of("suggestions", List.of(
                                                        Map.of("term", "jannett", "weight", 2, "payload", "AUTHOR|Jannett"),
                                                        Map.of("term", "scott", "weight", 1, "payload", "AUTHOR|Scott"),
                                                        Map.of("term", "astrid", "weight", 3, "payload", "AUTHOR|Astrid"))),
                                "fuzzy", Map.of("suggestions", Collections.emptyList())
                        ))
            }
    );

    private static final SuggestionEntity byWeight1 = new AuthorSuggestionEntity("astrid", 3, "Astrid");
    private static final SuggestionEntity byWeight2 = new AuthorSuggestionEntity("jannett", 2, "Jannett");
    private static final SuggestionEntity byWeight3 = new AuthorSuggestionEntity("scott", 1, "Scott");

    private static final SuggestionEntity byTerm1 = new AuthorSuggestionEntity("a", 1, "a");
    private static final SuggestionEntity byTerm2 = new AuthorSuggestionEntity("b", 1, "b");
    private static final SuggestionEntity byTerm3 = new AuthorSuggestionEntity("c", 1, "c");

    private static final SuggestionEntity byWeightTerm1 = new AuthorSuggestionEntity("a", 3, "a");
    private static final SuggestionEntity byWeightTerm2 = new AuthorSuggestionEntity("b", 3, "b");
    private static final SuggestionEntity byWeightTerm3 = new AuthorSuggestionEntity("c", 3, "c");
    private static final SuggestionEntity byWeightTerm4 = new AuthorSuggestionEntity("g", 2, "g");
    private static final SuggestionEntity byWeightTerm5 = new AuthorSuggestionEntity("a", 1, "a");
    private static final SuggestionEntity byWeightTerm6 = new AuthorSuggestionEntity("b", 1, "b");
}
