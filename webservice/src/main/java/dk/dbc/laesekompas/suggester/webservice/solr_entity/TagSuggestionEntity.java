package dk.dbc.laesekompas.suggester.webservice.solr_entity;

public class TagSuggestionEntity extends SuggestionEntity {
    private String tag;
    private Integer id;
    private String category;

    public TagSuggestionEntity(String matchedTerm, String tag, Integer id, String category) {
        super(matchedTerm, "TAG");
        this.tag = tag;
        this.id = id;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TagSuggestionEntity that = (TagSuggestionEntity) o;
        return (this.matchedTerm.equals(that.matchedTerm)) &&
                (this.type.equals(that.type)) &&
                (this.tag.equals(that.tag)) &&
                (this.id.equals(that.id));
    }
}
