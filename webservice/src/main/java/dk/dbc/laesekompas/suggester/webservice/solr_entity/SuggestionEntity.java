package dk.dbc.laesekompas.suggester.webservice.solr_entity;

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
