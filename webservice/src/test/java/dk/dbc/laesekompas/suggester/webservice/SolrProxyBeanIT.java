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
 * File created: 25/06/2019
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.glassfish.jersey.internal.guava.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SolrProxyBeanIT {
    private static final ObjectMapper O = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(SolrProxyBean.class);
    SolrProxyBean solrProxyBean;
    SolrClient solrClient;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        solrProxyBean = new SolrProxyBean();
        String solrUrl = System.getProperty("suggester.solr.url");

        solrProxyBean.solrUrl = solrUrl;
        log.info("We have the SolR suggester URL: {}", solrProxyBean.solrUrl);
        solrProxyBean.initialize();

        solrClient = new HttpSolrClient.Builder(solrUrl).build();
        solrClient.deleteByQuery("search", "*:*");
        solrClient.commit("search");
    }

    @Test
    public void proxyQueryRetrievesResults() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.commit("search");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://doesnotmatter.bob/solr-proxy?q=*:*"));
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<String, String>() {{
                this.add("q", "*:*");
            }});
        Response response = solrProxyBean.solrProxy(uriInfo);
        String result = (String) response.getEntity();
        JsonNode j1 = O.readTree(result);
        SolrQuery q = new SolrQuery();
        q.setParam("q", "*:*");
        String result2 = solrClient.query("search", q).jsonStr();
        JsonNode j2 = O.readTree(result2);
        assertEquals(j2.get("response"), j1.get("response").get("docs"));
    }

    @Test
    public void noQueryParamsDoNotFail() {
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://doesnotmatter.bob/solr-proxy?"));
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        Response response = solrProxyBean.solrProxy(uriInfo);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void proxyQueryRows() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.add("search", test2);
        solrClient.add("search", test3);
        solrClient.add("search", test4);
        solrClient.commit("search");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://doesnotmatter.bob/solr-proxy?q=*:*"));
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<String, String>() {{
            this.add("q", "*:*");
            this.add("rows", "2");
        }});
        Response response = solrProxyBean.solrProxy(uriInfo);
        String result = (String) response.getEntity();
        JsonNode j1 = O.readTree(result);
        List<JsonNode> l = Lists.newArrayList(j1.get("response").withArray("docs").elements());
        assertEquals(2, l.size());
    }

    @Test
    public void proxyQuerySpecialCharacters() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.commit("search");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://doesnotmatter.bob/solr-proxy?"));
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<String, String>() {{
            this.add("q", "hansen");
            this.add("q.alt", "jensen");
            this.add("defType", "edismax");
            this.add("qf", "author^6.0 title^5.0 all abstract");
            this.add("bf", "log(loans)");
            this.add("start", "1");
            this.add("rows", "11");
        }});
        // Should not fail or throw up
        Response response = solrProxyBean.solrProxy(uriInfo);
        String result = (String) response.getEntity();
        JsonNode j1 = O.readTree(result);
        // SolR status code 0 is a success
        assertEquals(0, j1.get("responseHeader").get("status").asInt());
    }

    @Test
    public void disallowNonJsonOutputType() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.commit("search");
        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://doesnotmatter.bob/solr-proxy?q=*:*"));
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<String, String>() {{
            this.add("q", "*:*");
            this.add("wt", "xml");
        }});
        Response response = solrProxyBean.solrProxy(uriInfo);
        assertEquals(500, response.getStatus());
    }

    private static final SolrInputDocument test1 = new SolrInputDocument() {{
        addField("author", "John Green");
        addField("title", "Looking for Alaska");
        addField("workid", "workid:12");
        addField("pid", "pid:13");
        addField("type", "Bog");
        addField("loans", 4);
        addField("a_post", false);
    }};

    private static final SolrInputDocument test2 = new SolrInputDocument() {{
        addField("author", "John Green");
        addField("title", "The Fault in our Stars");
        addField("workid", "workid:13");
        addField("pid", "pid:14");
        addField("type", "Bog");
        addField("loans", 9);
        addField("a_post", false);
    }};

    private static final SolrInputDocument test3 = new SolrInputDocument() {{
        addField("author", "John Green");
        addField("title", "Turtles all the way down");
        addField("workid", "workid:14");
        addField("pid", "pid:15");
        addField("type", "EBog");
        addField("loans", 2);
        addField("a_post", true);
    }};

    private static final SolrInputDocument test4 = new SolrInputDocument() {{
        addField("author", "John Green");
        addField("title", "Bob");
        addField("workid", "workid:15");
        addField("pid", "pid:16");
        addField("type", "LydBog");
        addField("loans", 7);
        addField("a_post", false);
    }};
}
