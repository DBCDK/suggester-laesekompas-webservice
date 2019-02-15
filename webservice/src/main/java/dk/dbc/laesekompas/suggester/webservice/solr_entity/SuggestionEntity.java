package dk.dbc.laesekompas.suggester.webservice.solr_entity;

public abstract class SuggestionEntity {
    protected String matchedTerm;
    protected String type;

    public SuggestionEntity(String matchedTerm, String type) {
        this.matchedTerm = matchedTerm;
        this.type = type;
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

    @Override
    public String toString() {
        return "{matchedTerm=\""+matchedTerm+"\""+", type="+type+"}";
    }
}
