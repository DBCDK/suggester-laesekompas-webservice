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

import dk.dbc.laesekompas.suggester.webservice.solr.SolrLaeskompasSuggester;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestQueryResponse;
import dk.dbc.laesekompas.suggester.webservice.solr.SuggestType;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.AuthorSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TagSuggestionEntity;
import dk.dbc.laesekompas.suggester.webservice.solr_entity.TitleSuggestionEntity;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SuggestResourceTest {
    private static final int MAX_SUGGESTIONS = 4;
    private static final Logger log = LoggerFactory.getLogger(SuggestResourceTest.class);
    private SuggestResource suggestResource;

    @Before
    public void setupBean() throws IOException, SolrServerException {
        suggestResource = new SuggestResource();

        suggestResource.suggesterSolrUrl = "http://invalid.invalid";
        HttpSolrClient solr = Mockito.mock(HttpSolrClient.class);
        SolrLaeskompasSuggester suggester = Mockito.mock(SolrLaeskompasSuggester.class);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test"), Mockito.any(SuggestType.class)))
                .thenReturn(test);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test_multiple_type"), Mockito.any(SuggestType.class)))
                .thenReturn(testMultipleType);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test_remove_duplicate_title"), Mockito.any(SuggestType.class)))
                .thenReturn(testRemoveDuplicateTitle);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test_remove_duplicate_author"), Mockito.any(SuggestType.class)))
                .thenReturn(testRemoveDuplicateAuthor);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test_remove_duplicate_tag"), Mockito.any(SuggestType.class)))
                .thenReturn(testRemoveDuplicateTag);
        Mockito.when(suggester.suggestQuery(Mockito.eq("test_max_suggestions"), Mockito.any(SuggestType.class)))
                .thenReturn(testMaxSuggestions);
        suggestResource.solr = solr;
        suggestResource.suggester = suggester;
        suggestResource.maxNumberSuggestions = MAX_SUGGESTIONS;
    }

    @Test
    public void suggestAllQueryRequireQuery() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll(null);
        assertEquals(400,response.getStatus());
    }

    @Test
    public void suggestEBookQueryRequireQuery() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks(null);
        assertEquals(400,response.getStatus());
    }

    @Test
    public void suggestAudioBookQueryRequireQuery() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks(null);
        assertEquals(400,response.getStatus());
    }

    @Test
    public void suggestAllQueryKeepSolrResponseOrder() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("test");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestEBookQueryKeepSolrResponseOrder() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("test");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAudioBookQueryKeepSolrResponseOrder() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("test");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAllQueryKeepSolrResponseOrderMultiType() throws IOException, SolrServerException {
        // Tests that the suggester orders "infix", then "blended_infix" and finally "fuzzy" suggestions.
        Response response = suggestResource.suggestAll("test_multiple_type");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));

    }

    @Test
    public void suggestEBookQueryKeepSolrResponseOrderMultiType() throws IOException, SolrServerException {
        // Tests that the suggester orders "infix", then "blended_infix" and finally "fuzzy" suggestions.
        Response response = suggestResource.suggestEBooks("test_multiple_type");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));

    }

    @Test
    public void suggestAudioBookQueryKeepSolrResponseOrderMultiType() throws IOException, SolrServerException {
        // Tests that the suggester orders "infix", then "blended_infix" and finally "fuzzy" suggestions.
        Response response = suggestResource.suggestAudioBooks("test_multiple_type");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        List<SuggestionEntity> expectedList = Arrays.asList(test1,test2,test3);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));

    }

    @Test
    public void suggestAllQueryRemoveDuplicateTitle() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("test_remove_duplicate_title");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTitle1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestEBookQueryRemoveDuplicateTitle() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("test_remove_duplicate_title");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTitle1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAudioBookQueryRemoveDuplicateTitle() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("test_remove_duplicate_title");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTitle1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAllQueryRemoveDuplicateAuthor() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("test_remove_duplicate_author");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateAuthor1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestEBookQueryRemoveDuplicateAuthor() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("test_remove_duplicate_author");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateAuthor1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAudioBookQueryRemoveDuplicateAuthor() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("test_remove_duplicate_author");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateAuthor1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAllQueryRemoveDuplicateTag() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("test_remove_duplicate_tag");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTag1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestEBookQueryRemoveDuplicateTag() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("test_remove_duplicate_tag");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTag1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAudioBookQueryRemoveDuplicateTag() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("test_remove_duplicate_tag");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        // Make sure other duplicate (same workid) is removed, only keeping the highest ranked
        List<SuggestionEntity> expectedList = Arrays.asList(testRemoveDuplicateTag1);
        assertThat(result, IsIterableContainingInOrder.contains(expectedList.toArray()));
    }

    @Test
    public void suggestAllQueryMaxNumberSuggestions() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAll("test_max_suggestions");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        assertEquals(result.size(), MAX_SUGGESTIONS);
    }

    @Test
    public void suggestEBookQueryMaxNumberSuggestions() throws IOException, SolrServerException {
        Response response = suggestResource.suggestEBooks("test_max_suggestions");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        assertEquals(result.size(), MAX_SUGGESTIONS);
    }

    @Test
    public void suggestAudioBookQueryMaxNumberSuggestions() throws IOException, SolrServerException {
        Response response = suggestResource.suggestAudioBooks("test_max_suggestions");
        List<SuggestionEntity> result = (List<SuggestionEntity>) response.getEntity();
        assertEquals(result.size(), MAX_SUGGESTIONS);
    }

    // TEST SOLR CLIENT RESPONSE DATA
    private static final TagSuggestionEntity test1 = new TagSuggestionEntity("testing","testing",1, "test_category");
    private static final AuthorSuggestionEntity test2 = new AuthorSuggestionEntity("testing", "testing");
    private static final TagSuggestionEntity test3 = new TagSuggestionEntity("tasting","tasting",2, "test_category");

    private static final SuggestQueryResponse test = new SuggestQueryResponse() {{
        setInfix(new ArrayList<SuggestionEntity>() {{
            add(test1);
            add(test2);
            add(test3);
        }});
        setInfixBlended(new ArrayList<>());
        setFuzzy(new ArrayList<>());
    }};

    private static final SuggestQueryResponse testMultipleType = new SuggestQueryResponse() {{
        setInfix(new ArrayList<SuggestionEntity>() {{
            add(test1);
        }});
        setInfixBlended(new ArrayList<SuggestionEntity>() {{
            add(test2);
        }});
        setFuzzy(new ArrayList<SuggestionEntity>() {{
            add(test3);
        }});
    }};

    private static final TitleSuggestionEntity testRemoveDuplicateTitle1 = new TitleSuggestionEntity(
            "tt",
            "title1",
            "author1",
            "workid:1",
            "pid:1");
    private static final TitleSuggestionEntity testRemoveDuplicateTitle2 = new TitleSuggestionEntity(
            "tt",
            "title1",
            "author1",
            "workid:1",
            "pid:2");
    private static final SuggestQueryResponse testRemoveDuplicateTitle = new SuggestQueryResponse() {{
        setInfix(new ArrayList<SuggestionEntity>() {{
            add(testRemoveDuplicateTitle1);
            add(testRemoveDuplicateTitle2);
        }});
        setInfixBlended(new ArrayList<>());
        setFuzzy(new ArrayList<>());
    }};

    private static final AuthorSuggestionEntity testRemoveDuplicateAuthor1 = new AuthorSuggestionEntity(
            "tt",
            "author1"
    );
    private static final AuthorSuggestionEntity testRemoveDuplicateAuthor2 = new AuthorSuggestionEntity(
            "tty",
            "author1"
    );
    private static final SuggestQueryResponse testRemoveDuplicateAuthor = new SuggestQueryResponse() {{
        setInfix(new ArrayList<SuggestionEntity>() {{
            add(testRemoveDuplicateAuthor1);
            add(testRemoveDuplicateAuthor2);
        }});
        setInfixBlended(new ArrayList<>());
        setFuzzy(new ArrayList<>());
    }};

    private static final TagSuggestionEntity testRemoveDuplicateTag1 = new TagSuggestionEntity("tag1","tag1",1, "tag_category1");
    private static final TagSuggestionEntity testRemoveDuplicateTag2 = new TagSuggestionEntity("tag2","tag1",1, "tag_category1");
    private static final SuggestQueryResponse testRemoveDuplicateTag = new SuggestQueryResponse() {{
        setInfix(new ArrayList<SuggestionEntity>() {{
            add(testRemoveDuplicateTag1);
            add(testRemoveDuplicateTag2);
        }});
        setInfixBlended(new ArrayList<>());
        setFuzzy(new ArrayList<>());
    }};
    private static final SuggestQueryResponse testMaxSuggestions = new SuggestQueryResponse() {{
       setInfix(new ArrayList<SuggestionEntity>() {{
           add(test1);
           add(test2);
           add(test3);
       }});
       setInfixBlended(new ArrayList<SuggestionEntity>() {{
           add(testRemoveDuplicateTitle1);
           add(testRemoveDuplicateTitle2);
           add(testRemoveDuplicateAuthor1);
           add(testRemoveDuplicateAuthor2);
       }});
       setFuzzy(new ArrayList<SuggestionEntity>() {{
           add(testRemoveDuplicateTag1);
           add(testRemoveDuplicateTag2);
       }});
    }};
}
