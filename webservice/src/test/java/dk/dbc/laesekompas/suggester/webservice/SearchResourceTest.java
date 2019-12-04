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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class SearchResourceTest {
    private static final int MAX_SUGGESTIONS = 4;
    private static final Logger log = LoggerFactory.getLogger(SearchResourceTest.class);
    private SearchResource searchResource;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        searchResource = new SearchResource();
        searchResource.solrAppId = "";

        searchResource.searchSolrUrl = "http://invalid.invalid";
        searchResource.corepoSolrUrl = "http://invalid2.invalid";
        // Mocking basic test, shows results as given by SolR
        Mockito.when(testLaesekompasSolrResponse.getResults()).thenReturn(testDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "john");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking test that rows parameter works
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "rows");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "20");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking test of querying author field
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "author_field");
                            put("defType", "dismax");
                            put("qf", "author");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking test of querying author_exact field
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "author_field_exact");
                            put("defType", "dismax");
                            put("qf", "author_exact");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking test querying title field
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "title_field");
                            put("defType", "dismax");
                            put("qf", "title");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking test querying title_exact field
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "title_field_exact");
                            put("defType", "dismax");
                            put("qf", "title_exact");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking testing merging on work IDs
        Mockito.when(testMergeWorkID.getResults()).thenReturn(testMergeWorkIDDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "merge");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "30");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testMergeWorkID);
        // Mocking test merging on work IDs when there are A-posts
        Mockito.when(testMergeWorkIDAPost.getResults()).thenReturn(testMergeWorkIDAPostDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "merge a-post");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "30");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testMergeWorkIDAPost);
        // Mocking test that when merging on work IDs the correct number of rows show up
        Mockito.when(testMergeWorkIDNumRows.getResults()).thenReturn(testMergeWorkIDNumRowsDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "merge #rows");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "6");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testMergeWorkIDNumRows);
        // Mocking test when merging on work IDs when SolR cannot return enough
        Mockito.when(testMergeWorkIDFewRows.getResults()).thenReturn(testMergeWorkIDFewRowsDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "merge #rows few");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "15");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testMergeWorkIDFewRows);
        // Mocking branch_id filter test
        Mockito.when(testLaesekompasSolrResponse.getResults()).thenReturn(testDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "filter on branch");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("fq", "branch_id:\"870970/b1\"");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        // Mocking filter_status test
        Mockito.when(testLaesekompasSolrResponse.getResults()).thenReturn(testDocs);
        Mockito.when(
                laesekompasSolr.query(Mockito.eq("search"),
                        Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, "filter on status");
                            put("defType", "dismax");
                            put("qf", SearchResource.SOLR_FULL_TEXT_QUERY);
                            put("fq", "branch_id:\"870970/b2\"");
                            put("bf", "log(loans)");
                            put(CommonParams.ROWS, "10");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testLaesekompasSolrResponse);
        Mockito.when(testCorepoSolrResponse.getResults()).thenReturn(testDocsEmpty);
        Mockito.when(
                corepoSolr.query(Mockito.argThat(new SolrParamsMatcher(new MapSolrParams(new HashMap<String, String>() {{
                            put(CommonParams.Q, String.format(SearchResource.COREPO_SOLR_TEXT_QUERY, "870970", "63"));
                            put(CommonParams.ROWS, "0");
                            put("appId", "");
                        }})))
                )
        ).thenReturn(testCorepoSolrResponse);
        searchResource.laesekompasSolr = laesekompasSolr;
        searchResource.corepoSolr = corepoSolr;
        searchResource.maxNumberSuggestions = MAX_SUGGESTIONS;
    }

    @Test
    public void getSearchReturnsResults() throws IOException, SolrServerException {
        Response response = searchResource.search("john", "", false, false, 10, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testDocSearchEntity1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void fieldQueryParamAuthorProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field", "author", false, false, 10, false, null);
    }

    @Test
    public void fieldQueryParamAuthorExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("author_field_exact", "author", true, false, 10, false, null);
    }

    @Test
    public void fieldQueryParamTitleProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field", "title", false, false, 10, false, null);
    }

    @Test
    public void fieldQueryParamTitleExactProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("title_field_exact", "title", true, false, 10, false, null);
    }

    @Test
    public void rowsProperSolRParam() throws IOException, SolrServerException {
        // If proper SolrParams are not generated, result will not be mocked, and search throws exception
        searchResource.search("rows", "", false, false, 20, false, null);
    }

    @Test
    public void mergeWorkIDParam() throws IOException, SolrServerException {
        Response response = searchResource.search("merge", "", false, true, 10, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testMergeWorkID1, testMergeWorkID2, testMergeWorkID3);
        System.out.println(result);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void mergeReturnAtMostRequestedNumRows() throws IOException, SolrServerException {
        int rows = 2;
        Response response = searchResource.search("merge #rows", "", false, true, rows, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        assertEquals(result.size(), rows);
    }

    @Test
    public void mergeDontFailOnFewResults() throws IOException, SolrServerException {
        int rows = 5;
        Response response = searchResource.search("merge #rows few", "", false, true, rows, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        // Test that the test is essentially testing what it is supposed to
        assert(testMergeWorkIDFewRowsDocs.size() < rows);
        assertEquals(result.size(), testMergeWorkIDFewRowsDocs.size());
    }

    @Test
    public void mergeWorkIdPrioritizeAPost() throws IOException, SolrServerException {
        Response response = searchResource.search("merge a-post", "", false, true, 10, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testMergeWorkIDAPost1, testMergeWorkIDAPost2);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void filterBranchId() throws IOException, SolrServerException {
        Response response = searchResource.search("filter on branch", "", false, false, 10, false, "870970/b1");
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        List<SearchEntity> expectedList = Arrays.asList(testDocSearchEntity1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void filterOnStatus() throws IOException, SolrServerException {
        // Laesekompas query mocked to return 1 element, corepo solr mocked to return 0, meaning it has no holdings
        Response response = searchResource.search("filter on status", "", false, false, 10, true, "870970/b2");
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();

        assert result.isEmpty();
    }

    private static final HttpSolrClient laesekompasSolr = Mockito.mock(HttpSolrClient.class);
    private static final HttpSolrClient corepoSolr = Mockito.mock(HttpSolrClient.class);
    private static final QueryResponse testLaesekompasSolrResponse = Mockito.mock(QueryResponse.class);
    private static final QueryResponse testCorepoSolrResponse = Mockito.mock(QueryResponse.class);
    private static final SolrDocument testDoc1 = new SolrDocument() {{
           addField("pid","pid:63");
           addField("author","Cynthia Lennon");
           addField("workid","workid:73");
           addField("title","John");
           addField("type","Bog");
           addField("loans",1);
           addField("a_post",false);
           addField("bibliographic_record_id",new ArrayList<String>() {{
               add("63");
           }});
           addField("_version_","123");
       }};
    private static final SearchEntity testDocSearchEntity1 = new SearchEntity("pid:63",
            "workid:73",
            "John",
            "Cynthia Lennon",
            SearchEntityType.BOOK,
            1,
            false,
            0,
            new ArrayList<String>() {{
                add("63");
            }}
    );
    private static final SolrDocumentList testDocsEmpty = new SolrDocumentList() {{}};
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
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("1");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDDoc2 = new SolrDocument() {{
        addField("pid","pid:2");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:1");
        addField("title","merge2");
        addField("type","Bog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("2");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDDoc3 = new SolrDocument() {{
        addField("pid","pid:3");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:2");
        addField("title","merge3");
        addField("type","Lydbog (net)");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("3");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDDoc4 = new SolrDocument() {{
        addField("pid","pid:4");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:3");
        addField("title","merge4");
        addField("type","Lydbog (net)");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("4");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDDoc5 = new SolrDocument() {{
        addField("pid","pid:5");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:3");
        addField("title","merge5");
        addField("type","Ebog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("5");
        }});
        addField("a_post",false);
    }};
    // Test that it selects the book, even though it is ranked lower (testMergeWorkIDDoc2)
    private static final SearchEntity testMergeWorkID1 = new SearchEntity("pid:2",
            "workid:1",
            "merge2",
            "Merge Mc. Merging",
            SearchEntityType.BOOK,
            1,
            false,
            0,
            new ArrayList<String>() {{
                add("2");
            }}
    );
    // Test that a non-book can be included, if no books can be picked (testMergeWorkIDDoc3)
    private static final SearchEntity testMergeWorkID2 = new SearchEntity("pid:3",
            "workid:2",
            "merge3",
            "Merge Mc. Merging",
            SearchEntityType.AUDIO_BOOK,
            1,
            false,
            1,
            new ArrayList<String>() {{
                add("3");
            }}
    );
    // Test that if no book can be picked, pick the highest ranked manifestation regardless if it is
    // a audio book or E book (testMergeWorkIDDoc4)
    private static final SearchEntity testMergeWorkID3 = new SearchEntity("pid:4",
            "workid:3",
            "merge4",
            "Merge Mc. Merging",
            SearchEntityType.AUDIO_BOOK,
            1,
            false,
            2,
            new ArrayList<String>() {{
                add("4");
            }}
    );
    private static final SolrDocumentList testMergeWorkIDDocs = new SolrDocumentList() {{
        add(testMergeWorkIDDoc1);
        add(testMergeWorkIDDoc2);
        add(testMergeWorkIDDoc3);
        add(testMergeWorkIDDoc4);
        add(testMergeWorkIDDoc5);
    }};

    // Test merging and prefer A-posts
    private static final QueryResponse testMergeWorkIDAPost = Mockito.mock(QueryResponse.class);
    private static final SolrDocument testMergeWorkIDAPostDoc1 = new SolrDocument() {{
        addField("pid","pid:1");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:1");
        addField("title","merge1");
        addField("type","Ebog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("1");
        }});
        addField("a_post",true);
    }};
    private static final SolrDocument testMergeWorkIDAPostDoc2 = new SolrDocument() {{
        addField("pid","pid:2");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:1");
        addField("title","merge2");
        addField("type","Bog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("2");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDAPostDoc3 = new SolrDocument() {{
        addField("pid","pid:3");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:2");
        addField("title","merge3");
        addField("type","Lydbog (net)");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("3");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testMergeWorkIDAPostDoc4 = new SolrDocument() {{
        addField("pid","pid:4");
        addField("author","Merge Mc. Merging");
        addField("workid","workid:2");
        addField("title","merge4");
        addField("type","Lydbog (net)");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("4");
        }});
        addField("a_post",true);
    }};
    // Test that it selects the A-post, even though a book is in the same work (testMergeWorkIDDoc1)
    private static final SearchEntity testMergeWorkIDAPost1 = new SearchEntity("pid:1",
            "workid:1",
            "merge1",
            "Merge Mc. Merging",
            SearchEntityType.E_BOOK,
            1,
            true,
            0,
            new ArrayList<String>() {{
                add("1");
            }}
    );
    // Test that if A-post is ranked lower in the work, it is still picked (testMergeWorkIDDoc4)
    private static final SearchEntity testMergeWorkIDAPost2 = new SearchEntity("pid:4",
            "workid:2",
            "merge4",
            "Merge Mc. Merging",
            SearchEntityType.AUDIO_BOOK,
            1,
            true,
            0,
            new ArrayList<String>() {{
                add("4");
            }}
    );
    private static final SolrDocumentList testMergeWorkIDAPostDocs = new SolrDocumentList() {{
        add(testMergeWorkIDAPostDoc1);
        add(testMergeWorkIDAPostDoc2);
        add(testMergeWorkIDAPostDoc3);
        add(testMergeWorkIDAPostDoc4);
    }};
    // Test merge return at most #rows
    private static final QueryResponse testMergeWorkIDNumRows = Mockito.mock(QueryResponse.class);
    private static final SolrDocument testDoc2 = new SolrDocument() {{
        addField("pid","pid:45");
        addField("author","Pierre Lemaitre");
        addField("workid","workid:372");
        addField("title","Rosy & John");
        addField("type","Bog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("45");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocument testDoc3 = new SolrDocument() {{
        addField("pid","pid:91");
        addField("author","Karsten Jørgensen");
        addField("workid","workid:1282");
        addField("title","John Lennon");
        addField("type","Ebog");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("91");
        }});
        addField("a_post",true);
    }};
    private static final SolrDocument testDoc4 = new SolrDocument() {{
        addField("pid","pid:126");
        addField("author","Mark Bego");
        addField("workid","workid:8392");
        addField("title","Elton John");
        addField("type","Lydbog (net)");
        addField("loans",1);
        addField("bibliographic_record_id",new ArrayList<String>() {{
            add("126");
        }});
        addField("a_post",false);
    }};
    private static final SolrDocumentList testMergeWorkIDNumRowsDocs = new SolrDocumentList() {{
        add(testDoc1);
        add(testDoc2);
        add(testDoc3);
        add(testDoc4);
    }};
    // Test merge return few results
    private static final QueryResponse testMergeWorkIDFewRows = Mockito.mock(QueryResponse.class);
    private static final SolrDocumentList testMergeWorkIDFewRowsDocs = new SolrDocumentList() {{
        add(testDoc1);
        add(testDoc2);
    }};
}
