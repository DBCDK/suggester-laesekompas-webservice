package dk.dbc.laesekompas.suggester.webservice.solr_entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum SearchEntityType implements Serializable {
    BOOK("Bog"),
    E_BOOK("Ebog"),
    AUDIO_BOOK("Lydbog (net)");
    private String type;

    SearchEntityType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    @JsonValue
    public String value() {
        return type;
    }
}
