package rmm.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public static void StringBuilderAppend(StringBuilder sb, String sbKey, JSONObject jsonObject, String key) {
        sb.append(sbKey);
        try {
            sb.append(jsonObject.getString(key));
        } catch (JSONException ignored) {
        }
    }
}
