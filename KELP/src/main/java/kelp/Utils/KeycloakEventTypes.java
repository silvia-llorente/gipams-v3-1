package kelp.Utils;

import org.keycloak.events.EventType;

import java.util.ArrayList;

public class KeycloakEventTypes {

    public static String LOGIN_OK = EventType.LOGIN.toString();
    public static String LOGIN_ERR = EventType.LOGIN_ERROR.toString();


    public static ArrayList<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.LOGIN);
        add(EventType.LOGIN_ERROR);
        // add(EventType.LOGOUT);
        // add(EventType.LOGOUT_ERROR);
        // add(EventType.REGISTER);
        // add(EventType.REGISTER_ERROR);
        // add(EventType.DELETE_ACCOUNT);
        // add(EventType.DELETE_ACCOUNT_ERROR);
        // add(EventType.RESET_PASSWORD);
        // add(EventType.RESET_PASSWORD_ERROR);
        // add(EventType.UPDATE_PASSWORD);
        // add(EventType.UPDATE_PASSWORD_ERROR);
        // add(EventType.CLIENT_REGISTER);
        // add(EventType.CLIENT_REGISTER_ERROR);
        // add(EventType.CLIENT_LOGIN);
        // add(EventType.CLIENT_LOGIN_ERROR);
        // add(EventType.CLIENT_DELETE);
        // add(EventType.CLIENT_DELETE_ERROR);
    }};
}
