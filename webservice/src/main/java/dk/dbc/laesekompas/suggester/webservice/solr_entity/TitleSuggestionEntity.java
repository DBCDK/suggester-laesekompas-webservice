package dk.dbc.laesekompas.suggester.webservice.solr_entity;

public class TitleSuggestionEntity extends SuggestionEntity {
    private String title;
    private String authorName;
    private String workid;
    private String pid;

    public TitleSuggestionEntity(String matchedTerm, String title, String authorName, String workid, String pid) {
        super(matchedTerm, "TITLE");
        this.title = title;
        this.authorName = authorName;
        this.workid = workid;
        this.pid = pid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }
}
