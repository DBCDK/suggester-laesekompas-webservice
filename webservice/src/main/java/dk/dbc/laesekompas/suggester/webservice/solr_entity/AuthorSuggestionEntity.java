package dk.dbc.laesekompas.suggester.webservice.solr_entity;

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
