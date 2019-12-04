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

public class AuthorSuggestionEntity extends SuggestionEntity {
    private String authorName;

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authorName);
    }

    public AuthorSuggestionEntity(String matchedTerm, long weight, String authorName) {
        super(matchedTerm, "AUTHOR", weight);
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorSuggestionEntity that = (AuthorSuggestionEntity) o;
        return this.matchedTerm.equals(that.matchedTerm) &&
                this.type.equals(that.type) &&
                this.authorName.equals(that.authorName);
    }
}
