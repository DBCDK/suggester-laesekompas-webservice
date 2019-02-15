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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorSuggestionEntity that = (AuthorSuggestionEntity) o;
        return (this.matchedTerm.equals(that.matchedTerm)) &&
                (this.type.equals(that.type)) &&
                (this.authorName.equals(that.authorName));
    }
}
