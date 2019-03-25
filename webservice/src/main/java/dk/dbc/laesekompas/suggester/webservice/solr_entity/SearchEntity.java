package dk.dbc.laesekompas.suggester.webservice.solr_entity;
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
 * File created: 25/03/2019
 */

public class SearchEntity {
    private String pid;
    private String workid;
    private String title;
    private String author;
    private SearchEntityType type;
    private int loans;
    private int order;

    public SearchEntity(String pid, String workid, String title, String author, SearchEntityType type, int loans, int order) {
        this.pid = pid;
        this.workid = workid;
        this.title = title;
        this.author = author;
        this.type = type;
        this.loans = loans;
        this.order = order;
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
        return (this.pid.equals(that.pid)) &&
                (this.type.equals(that.type)) &&
                (this.workid.equals(that.workid)) &&
                (this.title.equals(that.title)) &&
                (this.author.equals(that.author)) &&
                (this.loans == that.loans);
    }

    @Override
    public String toString() {
        return "{pid=\""+pid+"\""+", type="+type+", workid="+workid+", title="+title+"}";
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
