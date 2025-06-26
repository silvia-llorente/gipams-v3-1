package polS.Utils;

import java.util.Base64;
import com.google.gson.*;

public class JWTUtil {
    public String getUID(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json =  JsonParser.parseString(dec).getAsJsonObject();
        return json.get("sub").getAsString();
    }
    
    public JsonObject getRoles(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
        return json.get("realm_access").getAsJsonObject();
    }
}