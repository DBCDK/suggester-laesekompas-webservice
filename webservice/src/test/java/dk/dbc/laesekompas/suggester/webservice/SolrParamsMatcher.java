package dk.dbc.laesekompas.suggester.webservice;
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
 * File created: 15/03/2019
 */

import org.apache.solr.common.params.MapSolrParams;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class SolrParamsMatcher extends BaseMatcher<MapSolrParams> {
    private MapSolrParams toEqual;

    public SolrParamsMatcher(MapSolrParams toEqual) {
        this.toEqual = toEqual;
    }

    @Override
    public boolean matches(Object item) {
        if (toEqual == item) {
            return true;
        }
        if (item == null || toEqual.getClass() != item.getClass()) {
            return false;
        }
        return toEqual.getMap().equals(((MapSolrParams)item).getMap());
    }

    @Override
    public void describeMismatch(Object item, Description mismatchDescription) {}

    @Override
    public void describeTo(Description description) {}
}
