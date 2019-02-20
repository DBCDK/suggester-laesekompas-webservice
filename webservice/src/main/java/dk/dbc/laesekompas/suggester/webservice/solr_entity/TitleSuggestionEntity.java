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
 * File created: 20/02/2019
 */

public class TitleSuggestionEntity extends SuggestionEntity {
    private String title;
    private String authorName;
    private String workid;
    private String pid;

    public TitleSuggestionEntity(String matchedTerm, String title, String authorName, String workid, String pid) {
        super(matchedTerm, "TITLE");
        this.title = title;
        this.authorName = authorName;
        this.workid = workid;
        this.pid = pid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TitleSuggestionEntity that = (TitleSuggestionEntity) o;
        return (this.matchedTerm.equals(that.matchedTerm)) &&
                (this.type.equals(that.type)) &&
                (this.title.equals(that.title)) &&
                (this.authorName.equals(that.authorName)) &&
                (this.pid.equals(that.pid)) &&
                (this.workid.equals(that.workid));
    }
}
