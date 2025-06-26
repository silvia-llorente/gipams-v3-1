package rmm.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.text.SimpleDateFormat;
import java.util.*;

public class CustomLog {

    public Date date;
    public Map<String, String> log;

    public CustomLog(Date date, String log) {
        this.date = date;
        this.log = CreateMap(log);
    }

    public CustomLog(Date date, Map<String, Object> log){
        this.date = date;
        this.log = CreateMap(log);
    }

    private Map<String, String> CreateMap(Map<String, Object> map) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                result.put(entry.getKey(), (String) entry.getValue());
            } catch (ClassCastException e) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }

    private Map<String, String> CreateMap(String str) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = str.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0].trim();
            String value = Arrays.stream(keyValue).count() == 2 ? keyValue[1].trim() : "";
            map.put(key, value);
        }
        return map;
    }

    @JsonIgnore
    public String getDateStr(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(this.date);
    }

    @JsonIgnore
    public String getDateStrLong(){
        return Long.toString(this.date.getTime());
    }

    @JsonIgnore
    public String getType() {
        return this.log.get("type");
    }

    @JsonIgnore
    public String getUsername() {
        return this.log.get("username");
    }

    @JsonIgnore
    public String getIp() {
        return this.log.get("ipAddress");
    }

    @JsonIgnore
    public String getUri() {
        return this.log.get("redirect_uri");
    }

    @JsonIgnore
    public String getError(){
        return this.log.get("error");
    }
}
