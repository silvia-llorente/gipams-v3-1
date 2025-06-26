package gcs.Utils;

import java.util.Base64;
import com.google.gson.*;

public class JWTUtil {
    public static String getUID(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json =  JsonParser.parseString(dec).getAsJsonObject();
        return json.get("sub").getAsString();
    }
    
    public static JsonObject getRoles(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
        return json.get("realm_access").getAsJsonObject();
    }
    
    public static String getToken(String auth){
        return auth.substring("Bearer".length()).trim();
    }
}