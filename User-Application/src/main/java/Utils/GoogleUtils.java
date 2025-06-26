package Utils;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

public class GoogleUtils {

    public static GoogleUserCredentials ReadUserCredentials(HttpServletRequest request) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));

            if (VerifyCrossSiteRequestForgery(request)) {
                String idToken = GetCredentialsToken(request);
                GoogleIdToken googleToken = null;
                if ((googleToken = VerifyToken(props, idToken)) != null) {
                    return GetGoogleUserCredentials(idToken, googleToken);
                }
            }
        } catch (IOException | GeneralSecurityException ignored) {
        }
        return null;
    }

    public static GoogleIdToken VerifyToken(Properties props, String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(props.getProperty("clientIdGoogle")))
                .build();

        return verifier.verify(idTokenString);
    }

    private static boolean VerifyCrossSiteRequestForgery(HttpServletRequest request) {
        Optional<Cookie> csrf_token_cookie = Utils.getCookie(request, "g_csrf_token");
        if (csrf_token_cookie == null || !csrf_token_cookie.isPresent())
            return false;

        String csrf_token_body = Utils.getValueFromBodyUrlencoded(request, "g_csrf_token");

        return csrf_token_body.equals(csrf_token_cookie.get().getValue());
    }

    private static String GetCredentialsToken(HttpServletRequest request) {
        return Utils.getValueFromBodyUrlencoded(request, "credential");
    }

    public static GoogleUserCredentials GetUserInfo(String token, HttpServletRequest request) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));

            GoogleIdToken googleToken = null;
            if ((googleToken = VerifyToken(props, token)) != null) {
                return GetGoogleUserCredentials(token, googleToken);
            }
        } catch (GeneralSecurityException | IOException ignored) {
        }

        return null;

    }

    private static GoogleUserCredentials GetGoogleUserCredentials(String token, GoogleIdToken googleToken) {
        GoogleIdToken.Payload payload = googleToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        return new GoogleUserCredentials(token, userId, email, name);
    }
}