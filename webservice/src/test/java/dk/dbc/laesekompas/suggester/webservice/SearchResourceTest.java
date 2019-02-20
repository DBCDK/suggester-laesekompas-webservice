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
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
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
        Mockito.when(solr.query(Mockito.eq("search"), Mockito.any(SolrParams.class)))
                .thenReturn(test);
        searchResource.solr = solr;
        searchResource.maxNumberSuggestions = MAX_SUGGESTIONS;
    }

    @Test
    public void getSearchReturnsResults() throws IOException, SolrServerException {
        Response response = searchResource.search("john");
        SolrDocumentList result = (SolrDocumentList) response.getEntity();

        List<SolrDocument> expectedList = Arrays.asList(testDoc1);
        assertThat(result.parallelStream().collect(Collectors.toList()), IsIterableContainingInOrder.contains(expectedList.toArray()));
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
