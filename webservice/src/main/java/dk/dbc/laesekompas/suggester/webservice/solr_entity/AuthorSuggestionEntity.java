package dk.dbc.laesekompas.suggester.webservice.solr_entity;

public class AuthorSuggestionEntity extends SuggestionEntity {
    private String authorName;

    public AuthorSuggestionEntity(String matchedTerm, String authorName) {
        super(matchedTerm, "AUTHOR");
        this.authorName = authorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
