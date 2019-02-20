package dk.dbc.laesekompas.suggester.webservice.solr;
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

import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import org.apache.solr.client.solrj.response.SimpleSolrResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SuggestQueryResponse extends SimpleSolrResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestQueryResponse.class);
    private List<SuggestionEntity> infix;
    private List<SuggestionEntity> infixBlended;
    private List<SuggestionEntity> fuzzy;

    public SuggestQueryResponse() {

    }

    @Override
    public NamedList<Object> getResponse() {
        LOGGER.info(this.nl.toString());
        return super.getResponse();
    }

    public List<SuggestionEntity> getInfix() {
        return infix;
    }

    public void setInfix(List<SuggestionEntity> infix) {
        this.infix = infix;
    }

    public List<SuggestionEntity> getInfixBlended() {
        return infixBlended;
    }

    public void setInfixBlended(List<SuggestionEntity> infixBlended) {
        this.infixBlended = infixBlended;
    }

    public List<SuggestionEntity> getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(List<SuggestionEntity> fuzzy) {
        this.fuzzy = fuzzy;
    }
}
