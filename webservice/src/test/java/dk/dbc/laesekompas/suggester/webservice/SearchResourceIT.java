package dk.dbc.laesekompas.suggester.webservice;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SearchResourceIT {
    private static final Logger log = LoggerFactory.getLogger(SearchResourceIT.class);
    SearchResource searchResource;
    SolrClient solrClient;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        searchResource = new SearchResource();
        String solrUrl = System.getProperty("suggester.solr.url");

        searchResource.searchSolrUrl = solrUrl;
        log.info("We have the SolR suggester URL: {}", searchResource.searchSolrUrl);
        searchResource.maxNumberSuggestions = 10;
        searchResource.initialize();

        solrClient = new HttpSolrClient.Builder(solrUrl).build();
        solrClient.deleteByQuery("search", "*:*");
        solrClient.commit("search");
    }

    @Test
    public void searchQuery() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.commit("search");
        Response response = searchResource.search("john");
        SolrDocumentList result = (SolrDocumentList) response.getEntity();
        assertEquals(result.getNumFound(),1);

    }

    private static final SolrInputDocument test1 = new SolrInputDocument() {{
        addField("author", "John Green");
        addField("title", "Looking for Alaska");
        addField("workid", "workid:12");
        addField("pid", "pid:13");
        addField("type", "Bog");
        addField("loans", 4);
    }};
}