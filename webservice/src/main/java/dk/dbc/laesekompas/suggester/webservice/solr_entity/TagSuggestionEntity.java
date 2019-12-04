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
 * File created: 20/02/2019
 */

import java.util.Objects;

public class TagSuggestionEntity extends SuggestionEntity {
    private String tag;
    private Integer id;
    private String category;

    public TagSuggestionEntity(String matchedTerm, long weight, String tag, Integer id, String category) {
        super(matchedTerm, "TAG", weight);
        this.tag = tag;
        this.id = id;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, id, category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagSuggestionEntity that = (TagSuggestionEntity) o;
        return this.matchedTerm.equals(that.matchedTerm) &&
                this.type.equals(that.type) &&
                this.tag.equals(that.tag) &&
                this.id.equals(that.id);
    }
}
