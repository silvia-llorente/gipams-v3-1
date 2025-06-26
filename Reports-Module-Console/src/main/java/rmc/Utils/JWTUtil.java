package rmc.Utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTUtil {
    public String getUID(String token) {
        String[] chunks = token.split("\\.");
        String dec = new String(Base64.getDecoder().decode(chunks[1]));
        JsonObject json =  JsonParser.parseString(dec).getAsJsonObject();
        return json.get("sub").getAsString();
    }
   
    public static JsonObject getRoles(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return new JsonObject();

            String payloadJson = new String(Base64.getDecoder()
                                                 .decode(parts[1]),
                                            StandardCharsets.UTF_8);
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();

            if (payload.has("resource_access")) {
                JsonObject ra = payload.getAsJsonObject("resource_access");
                // Recorremos la primera entrada que encontremos:
                for (Map.Entry<String, JsonElement> entry : ra.entrySet()) {
                    JsonObject clientObj = entry.getValue().getAsJsonObject();
                    if (clientObj.has("roles")) {
                        return clientObj;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JWTUtil.class.getName())
                  .log(Level.WARNING, "Error parsejant JWT per extraure rols", ex);
        }
        return new JsonObject();
    }

}