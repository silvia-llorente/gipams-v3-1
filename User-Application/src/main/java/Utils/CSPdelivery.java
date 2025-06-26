package Utils;

import java.util.UUID;

public class CSPdelivery {

    public static String getNonce() {
        return UUID.randomUUID().toString();
    }

    public static String getHeader(String nonce) {
        return "script-src 'nonce-" + nonce + "' 'strict-dynamic'; object-src 'none'; base-uri 'none';";
    }

}
