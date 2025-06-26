package rmm.Management;

import rmm.Database.GipamsMongoDB;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Nginx {
    private static Nginx instance = null;

    private Properties props = null;

    private Nginx() throws IOException {
        props = new Properties();
        props.load(GipamsMongoDB.class.getClassLoader().getResourceAsStream("app.properties"));
    }

    public static Nginx GetInstance() throws IOException {
        if (instance == null) instance = new Nginx();
        return instance;
    }

    public boolean BlockIpAtProxy(String ip, double expirationTime) {
        String body = String.format("ip=%s&time=%s", ip, expirationTime);
        return QueryProxy(props.getProperty("blockIpPath"), body);
    }

    public boolean UnblockIpAtProxy(String ip) {
        String body = "ip=" + ip;
        return QueryProxy(props.getProperty("unblockIpPath"), body);
    }

    private static boolean QueryProxy(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("utf-8"));
            }

            if (conn.getResponseCode() == HttpServletResponse.SC_OK) {
                System.out.println("REQUEST OK");
                return true;
            } else {
                InputStreamReader in = new InputStreamReader(conn.getErrorStream());
                BufferedReader br = new BufferedReader(in);
                String err = "", aux;
                while ((aux = br.readLine()) != null)
                    err += aux;

                System.out.println("ERROR BAD REQUEST: " + err);
                return false;
            }
        } catch (IOException e) {
            System.out.println("ERROR BAD REQUEST");
            return false;
        }
    }
}
