package dk.dbc.laesekompas.suggester.webservice.solr_entity;
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
 * File created: 25/03/2019
 */

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;

public class SearchEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEntity.class);
    private String pid;
    private String workid;
    private String title;
    private String author;
    private SearchEntityType type;
    private int loans;
    private boolean aPost;
    private int order;
    private ArrayList<String> bibIdsInWork;

    @Override
    public int hashCode() {
        return Objects.hash(pid, workid, title, author, type, loans, aPost, bibIdsInWork);
    }

    public SearchEntity(
            String pid,
            String workid,
            String title,
            String author,
            SearchEntityType type,
            int loans,
            boolean aPost,
            int order,
            ArrayList<String> bibIdsInWork) {
        this.pid = pid;
        this.workid = workid;
        this.title = title;
        this.author = author;
        this.type = type;
        this.loans = loans;
        this.aPost = aPost;
        this.order = order;
        this.bibIdsInWork = bibIdsInWork;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchEntity that = (SearchEntity) o;
        return this.pid.equals(that.pid) &&
                this.type.equals(that.type) &&
                this.workid.equals(that.workid) &&
                this.title.equals(that.title) &&
                this.author.equals(that.author) &&
                this.bibIdsInWork.equals(that.bibIdsInWork) &&
                this.aPost == that.aPost &&
                this.loans == that.loans;
    }

    @Override
    public String toString() {
        return "{pid=\""+pid+"\""+", type="+type+", workid="+workid+", title="+title+", bibInWork: "+bibIdsInWork+"}";
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public SearchEntityType getType() {
        return type;
    }

    public void setType(SearchEntityType type) {
        this.type = type;
    }

    public int getLoans() {
        return loans;
    }

    public void setLoans(int loans) {
        this.loans = loans;
    }

    public boolean getAPost() {
        return aPost;
    }

    public void setAPost(boolean aPost) {
        this.aPost = aPost;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public ArrayList<String> getBibIdsInWork() {
        return bibIdsInWork;
    }

    public void setBibIdsInWork(ArrayList<String> bibIdsInWork) {
        this.bibIdsInWork = bibIdsInWork;
    }

    public static ArrayList<SearchEntity> searchResultsIntoSearchEntities(SolrDocumentList docs, Integer order) {
        ArrayList<SearchEntity> buffer = new ArrayList<>();
        // Converting search results to SearchEntity
        for (SolrDocument doc : docs) {
            SearchEntityType type;
            String docType = (String)doc.get("type");
            switch (docType){
                case "Bog":
                    type = SearchEntityType.BOOK;
                    break;
                case "Ebog":
                    type = SearchEntityType.E_BOOK;
                    break;
                case "Lydbog (net)":
                    type = SearchEntityType.AUDIO_BOOK;
                    break;
                default:
                    // Even though SolR is being weird in this case, we do not fail
                    LOGGER.warn("SolR had a search document with the following unrecognized type: {}", docType);
                    type = SearchEntityType.BOOK;
                    break;
            }
            buffer.add(new SearchEntity(
                    (String)doc.get("pid"),
                    (String)doc.get("workid"),
                    (String)doc.get("title"),
                    (String)doc.get("author"),
                    type,
                    (int)doc.get("loans"),
                    (boolean)doc.get("a_post"),
                    order,
                    (ArrayList<String>) doc.get("bibliographic_record_id"))
            );
            order += 1;
        }
        return buffer;
    }
}
