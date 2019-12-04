package dk.dbc.laesekompas.suggester.webservice;
/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of suggester-laesekompas-webservice
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
 * File created: 15/03/2019
 */

import dk.dbc.laesekompas.suggester.webservice.solr.SolrLaesekompasSuggester;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertThat;

public class SolrLaesekompasSuggesterTest {
    private static SolrLaesekompasSuggester solrLaesekompasSuggester;
    private final static String testAppId = "OnlyUsedForTest";

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
            return ((SolrQuery)item).getParams("suggest.q")[0].equals(query);
        }

        @Override
        public void describeTo(Description description) {

        }
    }

    @Before
    public void setupBean() throws IOException, SolrServerException {
        HttpSolrClient solr = Mockito.mock(HttpSolrClient.class);
        solrLaesekompasSuggester = new SolrLaesekompasSuggester(solr);

        Mockito.when(testInfixBlendedSortWeight.getSuggesterResponse()).thenReturn(testSuggesterResponseSortWeight);
        Mockito.when(testSuggesterResponseSortWeight.getSuggestions()).thenReturn(testSuggestionHandlesSortWeight);
        Mockito.when(
                solr.query(Mockito.argThat(new QuerySolrQueryMatcher("test_infix_blended_sort_by_weight")))
        ).thenReturn(testInfixBlendedSortWeight);
        Mockito.when(testInfixBlendedSortTerm.getSuggesterResponse()).thenReturn(testSuggesterResponseSortTerm);
        Mockito.when(testSuggesterResponseSortTerm.getSuggestions()).thenReturn(testSuggestionHandlesSortTerm);
        Mockito.when(
                solr.query(Mockito.argThat(new QuerySolrQueryMatcher("test_infix_blended_sort_by_term")))
        ).thenReturn(testInfixBlendedSortTerm);
        Mockito.when(testInfixBlendedSortBoth.getSuggesterResponse()).thenReturn(testSuggesterResponseSortBoth);
        Mockito.when(testSuggesterResponseSortBoth.getSuggestions()).thenReturn(testSuggestionHandlesSortBoth);
        Mockito.when(
                solr.query(Mockito.argThat(new QuerySolrQueryMatcher("test_infix_blended_sort_by_both")))
        ).thenReturn(testInfixBlendedSortBoth);
    }

    @Test
    public void testOrderBlendedInfixByWeight() throws IOException, SolrServerException {
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_weight", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byWeight1, byWeight2, byWeight3));
    }

    @Test
    public void testOrderBlendedInfixByTermWhenWeightIsEqual() throws IOException, SolrServerException {
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_term", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byTerm1, byTerm2, byTerm3));
    }

    @Test
    public void testOrderBlendedInfixByTermAndWeight() throws IOException, SolrServerException {
        SuggestQueryResponse response = solrLaesekompasSuggester
                .suggestQuery("test_infix_blended_sort_by_both", SuggestType.ALL);
        List<SuggestionEntity> infixBlendedSuggestions = response.getInfixBlended();
        assertThat(infixBlendedSuggestions, IsIterableContainingInOrder.contains(byWeightTerm1, byWeightTerm2,
                byWeightTerm3, byWeightTerm4, byWeightTerm5, byWeightTerm6));
    }

    private static final QueryResponse testInfixBlendedSortWeight = Mockito.mock(QueryResponse.class);
    private static final SuggesterResponse testSuggesterResponseSortWeight = Mockito.mock(SuggesterResponse.class);
    private static final Map<String, List<Suggestion>> testSuggestionHandlesSortWeight = new HashMap<String, List<Suggestion>>() {{
        put("analyzer", Collections.emptyList());
        put("infix", Collections.emptyList());
        put("blended_infix", Arrays.asList(
                new Suggestion("jannett", 2, "AUTHOR|Jannett"),
                new Suggestion("scott", 1, "AUTHOR|Scott"),
                new Suggestion("astrid", 3, "AUTHOR|Astrid")));
        put("fuzzy", Collections.emptyList());
    }};

    private static final QueryResponse testInfixBlendedSortTerm = Mockito.mock(QueryResponse.class);
    private static final SuggesterResponse testSuggesterResponseSortTerm = Mockito.mock(SuggesterResponse.class);
    private static final Map<String, List<Suggestion>> testSuggestionHandlesSortTerm = new HashMap<String, List<Suggestion>>() {{
        put("analyzer", Collections.emptyList());
        put("infix", Collections.emptyList());
        put("blended_infix", Arrays.asList(
                new Suggestion("c", 1, "AUTHOR|c"),
                new Suggestion("b", 1, "AUTHOR|b"),
                new Suggestion("a", 1, "AUTHOR|a")));
        put("fuzzy", Collections.emptyList());
    }};

    private static final QueryResponse testInfixBlendedSortBoth = Mockito.mock(QueryResponse.class);
    private static final SuggesterResponse testSuggesterResponseSortBoth = Mockito.mock(SuggesterResponse.class);
    private static final Map<String, List<Suggestion>> testSuggestionHandlesSortBoth = new HashMap<String, List<Suggestion>>() {{
        put("analyzer", Collections.emptyList());
        put("infix", Collections.emptyList());
        put("blended_infix", Arrays.asList(
                new Suggestion("b", 1, "AUTHOR|b"),
                new Suggestion("b", 3, "AUTHOR|b"),
                new Suggestion("a", 1, "AUTHOR|a"),
                new Suggestion("c", 3, "AUTHOR|c"),
                new Suggestion("a", 3, "AUTHOR|a"),
                new Suggestion("g", 2, "AUTHOR|g")
        ));
        put("fuzzy", Collections.emptyList());
    }};

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
