package Utils;

import org.apache.commons.codec.digest.HmacUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

public class antiCSRF {

    private static final String algorithm = "HmacSHA256";
    public static final String cookieName = "csrf_token";
    public static final String headerName = "csrf_token";

    public static Cookie getCookie(String sessionID) throws UnrecoverableEntryException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        String key = KeyStoreManager.GetSecureKey();
        String randomValue = UUID.randomUUID().toString();

        String message = sessionID + "!" + randomValue;
        String hmac = new HmacUtils(algorithm, key).hmacHex(message);
        String csrfToken = hmac + "." + message;

        Cookie cookie = new Cookie(cookieName, csrfToken);
        cookie.setHttpOnly(false);
        cookie.setSecure(true);
        cookie.setPath("/UA");
        return cookie;
    }

    public static boolean validateToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> findCookie = Arrays.stream(cookies).filter(c -> c.getName().equals(cookieName)).findFirst();
        if (!findCookie.isPresent()) return false;

        Cookie cookie = findCookie.get();
        try {
            String key = KeyStoreManager.GetSecureKey();

            String csrfToken = cookie.getValue();
            String[] csrfTokenSplit = csrfToken.split("\\.");
            String hmac = csrfTokenSplit[0];
            String message = csrfTokenSplit[1];
            String realHmac = new HmacUtils(algorithm, key).hmacHex(message);
            if (!hmac.equals(realHmac)) return false;

            String headerToken = request.getHeader(headerName);
            return hmac.equals(headerToken);

        } catch (UnrecoverableEntryException | CertificateException | IOException | KeyStoreException |
                 NoSuchAlgorithmException e) {
            return false;
        }
    }

}
