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

import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;


public class SuggestResourceIT {
    private static final Logger log = LoggerFactory.getLogger(SuggestResourceIT.class);
    SuggestResource suggestResource;
    SolrClient solrClient;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        suggestResource = new SuggestResource();
        String solrUrl = System.getProperty("suggester.solr.url");

        suggestResource.suggesterSolrUrl = solrUrl;
        log.info("We have the SolR suggester URL: {}", suggestResource.suggesterSolrUrl);
        suggestResource.maxNumberSuggestions = 10;
        suggestResource.initialize();

        solrClient = new HttpSolrClient.Builder(solrUrl).build();
        solrClient.deleteByQuery("suggest-all", "*:*");
        solrClient.commit("suggest-all");
        solrClient.deleteByQuery("suggest-audio_book", "*:*");
        solrClient.commit("suggest-audio_book");
        solrClient.deleteByQuery("suggest-e_book", "*:*");
        solrClient.commit("suggest-e_book");
    }

    @Test
    public void suggestQueryAllEmptyShouldNotFail() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("something from empty index");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        assertThat(suggestions.size(), is(equalTo(0)));
    }

    @Test
    public void suggestQueryEBookEmptyShouldNotFail() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("something from empty index");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        assertThat(suggestions.size(), is(equalTo(0)));
    }
    @Test
    public void suggestQueryAudioBookEmptyShouldNotFail() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("something from empty index");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        assertThat(suggestions.size(), is(equalTo(0)));
    }

    @Test
    public void suggestQueryAllRetrieve() throws IOException, SolrServerException {
        solrClient.add("suggest-all", authorInputDoc1);
        solrClient.add("suggest-all", authorInputDoc2);
        solrClient.commit("suggest-all");

        Response response = suggestResource.suggestAll("john");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        log.info("The suggestions list response: {}", suggestions);
        assertThat(suggestions, containsInAnyOrder(author1, author2));
    }

    @Test
    public void suggestQueryEBookRetrieve() throws IOException, SolrServerException {
        solrClient.add("suggest-e_book", authorInputDoc1);
        solrClient.add("suggest-e_book", authorInputDoc2);
        solrClient.commit("suggest-e_book");

        Response response = suggestResource.suggestEBooks("john");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        log.info("The suggestions list response: {}", suggestions);
        assertThat(suggestions, containsInAnyOrder(author1, author2));
    }

    @Test
    public void suggestQueryAudioBookRetrieve() throws IOException, SolrServerException {
        solrClient.add("suggest-audio_book", authorInputDoc1);
        solrClient.add("suggest-audio_book", authorInputDoc2);
        solrClient.commit("suggest-audio_book");

        Response response = suggestResource.suggestAudioBooks("john");
        List<SuggestionEntity> suggestions = (List<SuggestionEntity>) response.getEntity();
        log.info("The suggestions list response: {}", suggestions);
        assertThat(suggestions, containsInAnyOrder(author1, author2));
    }

    @Test(expected = SolrServerException.class)
    public void suggestQueryAllInvalidPayloadTitleMissingData() throws IOException, SolrServerException {
        solrClient.add("suggest-all", titleMissingDataInputDoc);
        solrClient.commit("suggest-all");
        suggestResource.suggestAll("kongens");
    }

    @Test(expected = SolrServerException.class)
    public void suggestQueryAllInvalidPayloadAuthorMissingData() throws IOException, SolrServerException {
        solrClient.add("suggest-all", authorMissingDataInputDoc);
        solrClient.commit("suggest-all");
        suggestResource.suggestAll("johannes");
    }

    @Test(expected = SolrServerException.class)
    public void suggestQueryAllInvalidPayloadTagMissingData() throws IOException, SolrServerException {
        solrClient.add("suggest-all", tagMissingDataInputDoc);
        solrClient.commit("suggest-all");
        suggestResource.suggestAll("drama");
    }

    private static final AuthorSuggestionEntity author1 = new AuthorSuggestionEntity("John Green", 1, "John Green");
    private static final SolrInputDocument authorInputDoc1 = new SolrInputDocument() {{
        addField("str","John Green");
        addField("type","AUTHOR");
        addField("payload","AUTHOR|John Green");
    }};
    private static final SolrInputDocument authorInputDoc2 = new SolrInputDocument() {{
        addField("str","John S. C. Abbott");
        addField("type","AUTHOR");
        addField("payload","AUTHOR|John S. C. Abbott");
    }};
    private static final AuthorSuggestionEntity author2 = new AuthorSuggestionEntity("John S. C. Abbott", 1, "John S. C. Abbott");

    private static final SolrInputDocument titleMissingDataInputDoc = new SolrInputDocument() {{
        addField("str","Kongens fald");
        addField("type","TITLE");
        // Missing last argument, pid
        addField("payload","TITLE|Kongens fald|Johannes V. Jensen|workid:1");
    }};

    private static final SolrInputDocument authorMissingDataInputDoc = new SolrInputDocument() {{
        addField("str","Johannes V. Jensen");
        addField("type","AUTHOR");
        // Missing last argument, authors name
        addField("payload","AUTHOR");
    }};

    private static final SolrInputDocument tagMissingDataInputDoc = new SolrInputDocument() {{
        addField("str","drama");
        addField("type","TAG");
        // Missing last argument, tag id
        addField("payload","TAG|drama");
    }};
}
