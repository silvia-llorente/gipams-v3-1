package rmc.servlet;

import org.json.JSONObject;
import rmc.Utils.Utils;

import javax.net.ssl.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Properties;

@WebServlet("/reports/getCertificateStatus")
public class getCertificateStatus extends HttpServlet {

    private String WEB_URL = "";

    public getCertificateStatus() {
        Properties props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            WEB_URL = props.getProperty("ua.baseUrl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        if (!Utils.checkAuth(req)) {
            System.out.println("Permission denied!");
            Utils.resetToken(null);
            String scheme = "https" + "://";
            String serverName = req.getServerName();
            String serverPort = (req.getServerPort() == 80) ? "" : ":" + req.getServerPort();
            String contextPath = req.getContextPath();
            String fin = scheme + serverName + serverPort + contextPath;
            resp.sendRedirect(fin + "/home.jsp");
            return;
        }

        try {
            // NOT VALID FOR PRODUCTION
            disableSSLValidation();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        PrintWriter out = resp.getWriter();
        try {
            URL url = new URL(WEB_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.getResponseCode();

            Date notBefore = null;
            Date notAfter = null;
            boolean ok = false;

            Certificate[] certificates = connection.getServerCertificates();
            for (Certificate certificate : certificates) {
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509Cert = (X509Certificate) certificate;
                    if (parseDomainName(x509Cert.getSubjectDN().toString()).equals(url.getHost())) {
                        notBefore = x509Cert.getNotBefore();
                        notAfter = x509Cert.getNotAfter();
                        Date currentDate = new Date();
                        if (currentDate.after(notBefore) && currentDate.before(notAfter))
                            ok = true;
                        break;
                    }
                }
            }

            if (notBefore != null && notAfter != null) {
                if (ok) {
                    out.println("<div id=\"certOK\" class=\"card\">");
                    out.println("<div class=\"icon\">");
                    out.println("<i class=\"fa fa-lock\" style=\"color: #4caf50;\"></i>");
                    out.println("</div>");
                    out.println("<div class=\"content\">");
                    out.println("<h2>Valid Digital Certificate</h2>");
                    out.println("<p>The certificate for " + WEB_URL + " is valid and secure.</p>");
                    addExpirationInfo(notBefore, notAfter, out);
                    out.println("</div>");
                    out.println("</div>");
                } else {
                    out.println("<div id=\"certERR\" class=\"card\">");
                    out.println("<div class=\"icon\">");
                    out.println("<div>");
                    out.println("<i class=\"fa fa-exclamation-triangle\" style=\"color: #af4c4c;\"></i>");
                    out.println("<i class=\"fa fa-unlock-alt\" style=\"color: #af4c4c;\"></i>");
                    out.println("</div>");
                    out.println("</div>");
                    out.println("<div class=\"content\">");
                    out.println("<h2>Invalid Digital Certificate</h2>");
                    out.println("<p>The certificate for " + WEB_URL + " is not valid or has expired.</p>");
                    addExpirationInfo(notBefore, notAfter, out);
                    out.println("</div>");
                    out.println("</div>");
                }
                return;
            }

        } catch (Exception e) {
        }

        out.println("<div id=\"certMissing\" class=\"card\">");
        out.println("<div class=\"icon\">");
        out.println("<div>");
        out.println("<i class=\"fa fa-question\" style=\"color: #eed202;\"></i>");
        out.println("</div>");
        out.println("</div>");
        out.println("<div class=\"content\">");
        out.println("<h2>Digital Certificate Not Found</h2>");
        out.println("<p>The certificate for " + WEB_URL + " couldn't be received.</p>");
        out.println("</div>");
        out.println("</div>");

    }

    private void addExpirationInfo(Date notBefore, Date notAfter, PrintWriter out) {
        out.println("<div class=\"card-column\">");
        out.println("<div class=\"card-row\">");
        out.println("<p class=\"card-label\">Issued:</p>");
        out.println("<p class=\"card-value\">" + notBefore + "</p>");
        out.println("</div>");
        out.println("<div class=\"card-row\">");
        out.println("<p class=\"card-label\">Expiration:</p>");
        out.println("<p class=\"card-value\">" + notAfter + "</p>");
        out.println("</div>");
        out.println("</div>");
    }

    private String parseDomainName(String subjectDN) {
        String domainName = null;
        if (subjectDN != null && !subjectDN.isEmpty()) {
            String[] parts = subjectDN.split(",\\s*");
            for (String part : parts) {
                if (part.startsWith("CN=")) {
                    domainName = part.substring(3); // Remove "CN="
                    break;
                }
            }
        }
        return domainName;
    }

    public static void disableSSLValidation() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
