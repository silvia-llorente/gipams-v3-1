package rmm.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jongo.marshall.jackson.oid.ObjectId;

public class DetectionParam {

    @JsonProperty("_id")
    @ObjectId
    @JsonSerialize(using = NoObjectIdSerializer.class)
    public String _id;

    @JsonProperty("variable")
    public String variable;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("value")
    public String value;

    public DetectionParam() {
    }

    public DetectionParam(String variable, String name, String description, String value) {
        this.variable = variable;
        this.name = name;
        this.description = description;
        this.value = value;
    }

    public DetectionParam(String _id, String variable, String name, String description, String value) {
        this._id = _id;
        this.variable = variable;
        this.name = name;
        this.description = description;
        this.value = value;
    }
}

