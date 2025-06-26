package rmm.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static rmm.Utils.Utils.StringBuilderAppend;

public class KeycloakEventUtils {

    public static String parseEvent(JSONObject jo, Properties props) {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat(props.getProperty("logsHoursFormat"));
        long dateLong = jo.getLong("time");
        Date date = new Date(dateLong);
        String time = sdf.format(date);
        sb.append(String.format("[%s]", time));

        StringBuilderAppend(sb, " type=", jo, "type");
        StringBuilderAppend(sb, ", realmId=", jo, "realmId");
        StringBuilderAppend(sb, ", clientId=", jo, "clientId");
        StringBuilderAppend(sb, ", userId=", jo, "userId");
        StringBuilderAppend(sb, ", ipAddress=", jo, "ipAddress");
        StringBuilderAppend(sb, ", error=", jo, "error");

        try {
            JSONObject details = jo.getJSONObject("details");

            StringBuilderAppend(sb, ", username=", details, "username");
            StringBuilderAppend(sb, ", redirect_uri=", details, "redirect_uri");

        } catch (JSONException ignored) {
        }

        return sb.toString();
    }
}
