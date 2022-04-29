package dk.dbc.laesekompas.suggester.webservice.solr;

import dk.dbc.laesekompas.suggester.webservice.solr_entity.SuggestionEntity;
import org.apache.solr.client.solrj.response.SimpleSolrResponse;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SuggestQueryResponse extends SimpleSolrResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestQueryResponse.class);
    private List<SuggestionEntity> analyzer;
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

    public List<SuggestionEntity> getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(List<SuggestionEntity> analyzer) {
        this.analyzer = analyzer;
    }
}
