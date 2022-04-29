package dk.dbc.laesekompas.suggester.webservice.solr;

/**
 * Enum containing all the Solr suggest-collections in use.
 */
public enum SuggestType {
    ALL("suggest-all"),
    E_BOOK("suggest-e_book"),
    AUDIO_BOOK("suggest-audio_book");
    private String collection;

    SuggestType(String collection) {
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }
}
