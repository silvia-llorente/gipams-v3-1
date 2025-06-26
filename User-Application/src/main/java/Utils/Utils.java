package Utils;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    private static LoginApplication APP = null;
    private static String TOKEN = null;
    private static String REFRESH_TOKEN = null;
    private static String ID_TOKEN = null;
    private static String SESSION_STATE = null;

    public static LoginApplication getApplication() {
        return APP;
    }

    public static void setToken(String resp, LoginApplication app) {
        if (app == null) {
            JsonObject json = JsonParser.parseString(resp).getAsJsonObject();
            TOKEN = json.get("access_token").getAsString();
            REFRESH_TOKEN = json.get("refresh_token").getAsString();
            ID_TOKEN = json.get("id_token").getAsString();
            SESSION_STATE = json.get("session_state").getAsString();
        } else {
            TOKEN = resp;
            APP = app;
        }
    }

    public static String getToken() {
        return TOKEN;
    }

    public static boolean checkAuth(HttpServletRequest request) {
        String tok = getAuthCookie(request);
        if (tok == null || tok.isEmpty()) return false;
        TOKEN = tok; //TODO: This is temporary

        if (APP == null) {
            try {
                String[] chunks = tok.split("\\.");
                String dec = new String(Base64.getDecoder().decode(chunks[0]));
                JsonObject json = JsonParser.parseString(dec).getAsJsonObject();
                if (!json.get("alg").getAsString().equals("RS256")) return false;
                String kid = json.get("kid").getAsString();
                Properties props = new Properties();
                props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
                JwkProvider provider = new UrlJwkProvider(new URL(props.getProperty("authUrl") + "/realms/" + props.getProperty("realm") + "/protocol/openid-connect/certs"));
                Jwk jwk = provider.get(kid);

                RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();

                Jwts.parser().setSigningKey(publicKey).parseClaimsJws(tok);
                return true;

            } catch (NullPointerException | ExpiredJwtException | MalformedJwtException | UnsupportedJwtException |
                     IllegalArgumentException e) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Access denied!", e);
            } catch (JwkException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "JWT comprovation exception", ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Malformed URL", ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "File not found", ex);
            } catch (IOException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "IO Exception", ex);
            }
        } else if (APP == LoginApplication.GOOGLE) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
                Object token = GoogleUtils.VerifyToken(props, tok);
                if (token != null) {
                    return true;
                } else {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Access denied!");
                }
            } catch (GeneralSecurityException gex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Access denied!", gex);
            } catch (IOException ioex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "IO Exception", ioex);
            }
        }

        return false;
    }

    public static String getUsername(HttpServletRequest request) {
        if (APP == null) {
            String payload = TOKEN.split("\\.")[1];
            JsonObject json = JsonParser.parseString(new String(Base64.getDecoder().decode(payload))).getAsJsonObject();
            return json.get("preferred_username").getAsString();
        } else if (APP == LoginApplication.GOOGLE) {
            GoogleUserCredentials cred = GoogleUtils.GetUserInfo(TOKEN, request);
            return cred == null ? "" : cred.name;
        }

        return "";
    }

    public static String getRefreshToken() {
        return REFRESH_TOKEN;
    }

    public static void resetToken(String tok) {
        TOKEN = tok;
    }

    public static List<Map<String, String>> JSON_to_ListMap(String data) {
        List<Map<String, String>> ret = new ArrayList();
        String[] position = data.split("}");
        for (String position1 : position) {
            Map<String, String> aux = new HashMap<>();
            String[] str = position1.split("\",\""); // Hem de posar les cometes (") perquè els keywords estan separats per comes (,)...
            for (String str1 : str) {
                String clean = str1.replaceAll("\\[", "").replaceAll(",\\{", "").replaceAll("\\{", "").replaceAll("\\]", "").replaceAll("\"", "");
                if (!clean.equals("")) {
                    String[] last = clean.split(":");
                    // A last hem de tenir només dos Strings: el nom del caràcter i el seu valor.
                    if (last.length == 2) aux.put(last[0], last[1]);
                }
            }
            if (!aux.isEmpty()) ret.add(aux);
        }
        return ret;
    }

    public static String getValueFromBodyUrlencoded(HttpServletRequest request, String valueName) {
        String value = request.getParameter(valueName);
        return value;
    }

    public static String getAuthCookie(HttpServletRequest request) {
        Optional<Cookie> cookie = getCookie(request, "GIPAMS-auth");

        if (cookie != null && cookie.isPresent())
            return cookie.get().getValue();

        return null;
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies).filter((c ->
                    c.getName().equals(cookieName)
            )).findFirst();

            return cookie;
        }

        return null;
    }
}
