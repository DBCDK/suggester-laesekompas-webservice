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
 * File created: 20/02/2019
 */

import dk.dbc.laesekompas.suggester.webservice.solr_entity.SearchEntity;
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
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class SearchResourceIT {
    private static final Logger log = LoggerFactory.getLogger(SearchResourceIT.class);
    SearchResource searchResource;
    SolrClient solrClient;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        searchResource = new SearchResource();
        String laesekompasSolRUrl = System.getProperty("suggester.solr.url");
        String corepoSolrUrl = System.getProperty("corepo.solr.url");

        searchResource.searchSolrUrl = laesekompasSolRUrl;
        searchResource.corepoSolrUrl = corepoSolrUrl;
        log.info("We have the SolR suggester URL: {}", searchResource.searchSolrUrl);
        log.info("We have the Corepo suggester URL: {}", searchResource.corepoSolrUrl);
        searchResource.maxNumberSuggestions = 10;
        searchResource.initialize();

        solrClient = new HttpSolrClient.Builder(laesekompasSolRUrl).build();
        solrClient.deleteByQuery("search", "*:*");
        solrClient.commit("search");
    }

    @Test
    public void searchQuery() throws IOException, SolrServerException {
        solrClient.add("search", test1);
        solrClient.commit("search");
        Response response = searchResource.search("john", "", false, false, 10, false, null);
        List<SearchEntity> result = (List<SearchEntity>) response.getEntity();
        assertEquals(result.size(), 1);
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
}
