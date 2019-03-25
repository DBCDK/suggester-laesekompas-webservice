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

import dk.dbc.laesekompas.suggester.webservice.solr_entity.SearchEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SearchEntityType;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertThat;

public class SearchResourceTest {
    private static final int MAX_SUGGESTIONS = 4;
    private static final Logger log = LoggerFactory.getLogger(SearchResourceTest.class);
    private SearchResource searchResource;

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
        Mockito.when(testMergeWorkID.getResults()).thenReturn(testMergeWorkIDDocs);
        Mockito.when(
                solr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "merge");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                        }})))
                )
        ).thenReturn(testMergeWorkID);
        searchResource.solr = solr;
        searchResource.maxNumberSuggestions = MAX_SUGGESTIONS;
    }

    @Test
    public void getSearchReturnsResults() throws IOException, SolrServerException {
        Response response = searchResource.search("john", "", false, false, 10);
        ArrayList<SearchEntity> result = (ArrayList<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testDocSearchEntity1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void fieldQueryParamAuthorProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field", "author", false, false, 10);
    }

    @Test
    public void fieldQueryParamAuthorExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field_exact", "author", true, false, 10);
    }

    @Test
    public void fieldQueryParamTitleProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field", "title", false, false, 10);
    }

    @Test
    public void fieldQueryParamTitleExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field_exact", "title", true, false, 10);
    }

    @Test
    public void rowsProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("rows", "", false, false, 20);
    }

    @Test
    public void mergeWorkIDParam() throws IOException, SolrServerException {
        Response response = searchResource.search("merge", "", false, true, 10);
        ArrayList<SearchEntity> result = (ArrayList<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testMergeWorkID1, testMergeWorkID2, testMergeWorkID3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
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
    private static final SearchEntity testDocSearchEntity1 = new SearchEntity("pid:63",
            "workid:73",
            "John",
            "Cynthia Lennon",
            SearchEntityType.BOOK,
            1,
            0
    );
    private static final SolrDocumentList testDocs = new SolrDocumentList() {{
       add(testDoc1);
    }};
    // Merge workID test
    private static final QueryResponse testMergeWorkID = Mockito.mock(QueryResponse.class);
    private static final SolrDocument testMergeWorkIDDoc1 = new SolrDocument() {{
        addField("pid","pid:1");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:1");
        addField("title","merge1");
        addField("type","Ebog");
        addField("loans",1);
    }};
    private static final SolrDocument testMergeWorkIDDoc2 = new SolrDocument() {{
        addField("pid","pid:2");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:1");
        addField("title","merge2");
        addField("type","Bog");
        addField("loans",1);
    }};
    private static final SolrDocument testMergeWorkIDDoc3 = new SolrDocument() {{
        addField("pid","pid:3");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:2");
        addField("title","merge3");
        addField("type","Lydbog (net)");
        addField("loans",1);
    }};
    private static final SolrDocument testMergeWorkIDDoc4 = new SolrDocument() {{
        addField("pid","pid:4");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:3");
        addField("title","merge4");
        addField("type","Lydbog (net)");
        addField("loans",1);
    }};
    private static final SolrDocument testMergeWorkIDDoc5 = new SolrDocument() {{
        addField("pid","pid:5");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:3");
        addField("title","merge5");
        addField("type","Ebog");
        addField("loans",1);
    }};
    // Test that it selects the book, even though it is ranked lower (testMergeWorkIDDoc2)
    private static final SearchEntity testMergeWorkID1 = new SearchEntity("pid:2",
            "workid:1",
            "merge2",
            "Merge Mc. Merging",
            SearchEntityType.BOOK,
            1,
            0
    );
    // Test that a non-book can be included, if no books can be picked (testMergeWorkIDDoc3)
    private static final SearchEntity testMergeWorkID2 = new SearchEntity("pid:3",
            "workid:2",
            "merge3",
            "Merge Mc. Merging",
            SearchEntityType.AUDIO_BOOK,
            1,
            0
    );
    // Test that if no book can be picked, pick the highest ranked manifestation regardless if it is
    // a audio book or E book (testMergeWorkIDDoc4)
    private static final SearchEntity testMergeWorkID3 = new SearchEntity("pid:4",
            "workid:3",
            "merge4",
            "Merge Mc. Merging",
            SearchEntityType.AUDIO_BOOK,
            1,
            0
    );
    private static final SolrDocumentList testMergeWorkIDDocs = new SolrDocumentList() {{
        add(testMergeWorkIDDoc1);
        add(testMergeWorkIDDoc2);
        add(testMergeWorkIDDoc3);
        add(testMergeWorkIDDoc4);
        add(testMergeWorkIDDoc5);
    }};
}
