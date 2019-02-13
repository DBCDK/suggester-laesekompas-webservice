package dk.dbc.laesekompas.suggester.webservice.solr;

public enum SuggestType {
    ALL("suggest-all"),
    E_BOOK("suggest-e_book"),
    AUDIO_BOOK("suggest-audio_book");
    private String collection;

    private SuggestType(String collection) {
        this.collection = collection;
    }

    public String getCollection() {
        return collection;
    }
}
