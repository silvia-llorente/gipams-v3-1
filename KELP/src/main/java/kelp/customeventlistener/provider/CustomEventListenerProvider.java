package kelp.customeventlistener.provider;

import kelp.Utils.KeycloakEventTypes;
import com.google.gson.Gson;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

public class CustomEventListenerProvider implements EventListenerProvider {

    private static final ArrayList<EventType> criticalEvents = KeycloakEventTypes.eventTypes;
    private static Properties props = LoadProps();
    private static Gson _gson = new Gson();

    public CustomEventListenerProvider() {
    }

    @Override
    public void onEvent(Event event) {
        try {
            if (criticalEvents.contains(event.getType())) {
                sendEvent(event, props.getProperty("loggingApi.userEvents.url"));
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        /*try{
            sendEvent(adminEvent, props.getProperty("loggingApi.adminEvents.url"));
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }*/
    }

    private void sendEvent(Object event, String uri) {
        System.out.println("SENDING EVENT TO API...");

        try {
            URL url = new URL(uri);
            System.out.println("URL CREATED FOR: " + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.out.println("CONNECTION OPENED");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            String body = _gson.toJson(event);
            System.out.println("BODY CREATED: " + body);

            System.out.println("SENDING BODY...");
            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            System.out.println(body);
            osw.write(body);
            osw.flush();
            osw.close();
            System.out.println("...BODY SENT");

            System.out.println("... GETTING RESPONSE ...");
            int code = conn.getResponseCode();
            System.out.println("RESPONSE STATUS CODE:" + code);

            if (code == HttpURLConnection.HTTP_OK)
                System.out.println("...EVENT SENT TO API  :) ");
            else
                System.out.println("...EVENT NOT SENT TO API  :( ");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("...ERROR SENDING EVENTS  X( ");
        }
    }

    public void close() {
    }

    private static Properties LoadProps() {
        try {
            Properties props = new Properties();
            props.load(CustomEventListenerProvider.class.getClassLoader().getResourceAsStream("app.properties"));
            return props;
        } catch (IOException e) {
            System.out.println("ERROR LOADING PROPS");
        }

        return null;
    }

}
