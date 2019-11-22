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

import java.util.Objects;

public abstract class SuggestionEntity {
    protected String matchedTerm;
    protected String type;
    protected long weight;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuggestionEntity that = (SuggestionEntity) o;
        return weight == that.weight &&
                matchedTerm.equals(that.matchedTerm) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchedTerm, type, weight);
    }

    public SuggestionEntity(String matchedTerm, String type, long weight) {
        this.matchedTerm = matchedTerm;
        this.type = type;
        this.weight = weight;
    }

    public String getMatchedTerm() {
        return matchedTerm;
    }

    public void setMatchedTerm(String matchedTerm) {
        this.matchedTerm = matchedTerm;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "{matchedTerm=\""+matchedTerm+"\""+", type="+type+", weight="+weight+"}";
    }
}
