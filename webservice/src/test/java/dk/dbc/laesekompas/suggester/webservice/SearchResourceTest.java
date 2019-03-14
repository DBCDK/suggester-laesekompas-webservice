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

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;

public class SearchResourceTest {
    private static final int MAX_SUGGESTIONS = 4;
    private static final Logger log = LoggerFactory.getLogger(SearchResourceTest.class);
    private SearchResource searchResource;

    private class SolrParamsMatcher extends BaseMatcher<MapSolrParams> {
        private MapSolrParams toEqual;

        public SolrParamsMatcher(MapSolrParams toEqual) {
            this.toEqual = toEqual;
        }

        @Override
        public boolean matches(Object item) {
            if (toEqual == item) {
                return true;
            }
            if (item == null || toEqual.getClass() != item.getClass()) {
                return false;
            }
            return toEqual.getMap().equals(((MapSolrParams)item).getMap());
        }

        @Override
        public void describeMismatch(Object item, Description mismatchDescription) {}

        @Override
        public void describeTo(Description description) {}
    }

    @Before
    public void setupBean() throws IOException, SolrServerException {
        searchResource = new SearchResource();

        searchResource.searchSolrUrl = "http://invalid.invalid";
        Mockito.when(test.getResults()).thenReturn(testDocs);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "john");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(test);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "rows");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "20");
                        }})))
                )
        ).thenReturn(test);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "author_field");
                            put("defType", "dismax");
                            put("qf", "author");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(test);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "author_field_exact");
                            put("defType", "dismax");
                            put("qf", "author_exact");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(test);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "title_field");
                            put("defType", "dismax");
                            put("qf", "title");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(test);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "title_field_exact");
                            put("defType", "dismax");
                            put("qf", "title_exact");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(test);
        searchResource.solr = solr;
        searchResource.maxNumberSuggestions = MAX_SUGGESTIONS;
    }

    @Test
    public void getSearchReturnsResults() throws IOException, SolrServerException {
        Response response = searchResource.search("john", "", false, 10);
        SolrDocumentList result = (SolrDocumentList) response.getEntity();

        List<SolrDocument> expectedList = Arrays.asList(testDoc1);
        assertThat(result.parallelStream().collect(Collectors.toList()), IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void fieldQueryParamAuthorProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field", "author", false, 10);
    }

    @Test
    public void fieldQueryParamAuthorExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field_exact", "author", true, 10);
    }

    @Test
    public void fieldQueryParamTitleProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field", "title", false, 10);
    }

    @Test
    public void fieldQueryParamTitleExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field_exact", "title", true, 10);
    }

    @Test
    public void rowsProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("rows", "", false, 20);
    }

    private static final HttpSolrClient solr = Mockito.mock(HttpSolrClient.class);
    private static final QueryResponse test = Mockito.mock(QueryResponse.class);
    private static final SolrDocument testDoc1 = new SolrDocument() {{
           addField("pid","pid:63");
           addField("author","Cynthia Lennon");
           addField("workid","workid:73");
           addField("title","John");
           addField("type","Bog");
           addField("loans",1);
           addField("_version_","123");
       }};
    private static final SolrDocumentList testDocs = new SolrDocumentList() {{
       add(testDoc1);
    }};
}
