package rmm.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jongo.marshall.jackson.oid.ObjectId;

import java.text.SimpleDateFormat;

public class IpBlocking {

    @JsonProperty("_id")
    @ObjectId
    @JsonSerialize(using = NoObjectIdSerializer.class)
    public String _id;

    @JsonProperty("ip")
    public String ip;

    @JsonProperty("blocked")
    public boolean blocked;

    @JsonProperty("blockingDate")
    public long blockingDate;

    @JsonProperty("expirationTime")
    public double expirationTime;

    public IpBlocking() {
    }

    public IpBlocking(String _id, String ip, boolean blocked, long blockingDate, double expirationTime) {
        this._id = _id;
        this.ip = ip;
        this.blocked = blocked;
        this.blockingDate = blockingDate;
        this.expirationTime = expirationTime;
    }

    public IpBlocking(String ip) {
        this.ip = ip;
        this.blocked = false;
        this.blockingDate = -1;
        this.expirationTime = -1;
    }

    @JsonIgnore
    public String getDateStr(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(this.blockingDate);
    }
}
