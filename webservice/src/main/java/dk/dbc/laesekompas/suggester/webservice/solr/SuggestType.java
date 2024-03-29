package dk.dbc.laesekompas.suggester.webservice.solr;
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

/**
 * Enum containing all the suggest SolR collections in use.
 */
public enum SuggestType {
    ALL("suggest-all"),
    E_BOOK("suggest-e_book"),
    AUDIO_BOOK("suggest-audio_book");
    private final String collection;

    SuggestType(String collection) {
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }
}
