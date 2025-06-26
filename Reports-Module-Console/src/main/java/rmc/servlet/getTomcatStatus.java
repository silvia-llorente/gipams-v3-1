package rmc.servlet;

//import com.sun.org.apache.xalan.internal.xsltc.runtime.Operators;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import rmc.Utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/reports/getTomcatStatus")
public class getTomcatStatus extends HttpServlet {

    private String TOMCAT_URL = "";
    private String SECURITY_FILE_FORMAT = "";

    public getTomcatStatus() {
        Properties props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            TOMCAT_URL = props.getProperty("tomcat.baseUrl");
            SECURITY_FILE_FORMAT = props.getProperty("tomcat.security.fileFormat");
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
            TomcatVersion tomcatVersion = getTomcatVersion();
            if (tomcatVersion != null) {
                String file = SECURITY_FILE_FORMAT.replace("#", Integer.toString(tomcatVersion.major));
                Document doc = Jsoup.connect(TOMCAT_URL + file).get();
                Element contentDiv = doc.getElementById("content");
                Elements content = contentDiv.getAllElements();
                List<TomcatVersion> versions = content.stream().filter(element -> {
                    if (!element.is("h3") || !element.id().contains("Fixed")) return false;
                    TomcatVersion tv = getTomcatVersion(element);
                    if (tv == null) return false;
                    return tomcatVersion.compare(tv) > 0;
                }).map(this::getTomcatVersion).collect(Collectors.toList());

                PrintWriter out = resp.getWriter();
                if (versions.isEmpty()) {
                    out.println("<div id=\"tomcatOK\" class=\"card\">");
                    out.println("<div class=\"icon\">");
                    out.println("<i class=\"fa fa-shield\" style=\"color: #4caf50;\"></i>");
                    out.println("</div>");
                    out.println("<div class=\"content\">");
                    out.println("<h2>Apache Tomcat Is Up To Date</h2>");
                    out.println("<p>Apache Tomcat:" + tomcatVersion + " is the last and more secure version of Apache Tomcat:" + tomcatVersion.major + ".x.</p>");
                    out.println("</div>");
                    out.println("</div>");
                } else {
                    out.println("<div id=\"tomcatERR\" class=\"card\">");
                    out.println("<div class=\"icon\">");
                    out.println("<div>");
                    out.println("<i class=\"fa fa-exclamation-triangle\" style=\"color: #af4c4c;\"></i>");
                    out.println("<i class=\"fa fa-wrench\" style=\"color: #af4c4c;\"></i>");
                    out.println("</div>");
                    out.println("</div>");
                    out.println("<div class=\"content\">");
                    out.println("<h2>Apache Tomcat Is Vulnerable</h2>");
                    out.println("<p>Apache Tomcat:" + tomcatVersion + " must be updated to a more secure version of Apache Tomcat:" + tomcatVersion.major + ".x.</p>");
                    out.println("<div class=\"card-column\">");
                    out.println("<div class=\"card-row\">");
                    out.println("<p class=\"card-label\">Newer versions:</p>");
                    out.println("</div>");
                    for (TomcatVersion newVersion : versions) {
                        out.println("<div class=\"card-row\">");
                        out.println("<p class=\"card-label\">Apache Tomcat:" + newVersion + "</p>");
                        out.println("</div>");
                    }
                    out.println("</div>");
                    out.println("</div>");
                    out.println("</div>");
                }

            }
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }

    private TomcatVersion getTomcatVersion() {
        int major = 0;
        int minor = 0;
        int release = 0;

        ServletContext context = getServletContext();
        String tomcat = context.getServerInfo();
        String[] tomcatSplit = tomcat.split("\\/");
        if (tomcatSplit.length == 2) {
            HashMap<String, Integer> version = splitVersion(tomcatSplit[1]);
            if (version.containsKey("major")) major = version.get("major");
            if (version.containsKey("minor")) minor = version.get("minor");
            if (version.containsKey("release")) release = version.get("release");
        } else {
            return null;
        }

        return new TomcatVersion(major, minor, release);
    }

    private TomcatVersion getTomcatVersion(Element h3fixed) {
        String id = h3fixed.id().toLowerCase();
        if (!id.contains("_tomcat_")) return null;
        String[] idSplit = id.split("_tomcat_");
        if (idSplit.length < 2) return null;

        int major = 0;
        int minor = 0;
        int release = 0;

        HashMap<String, Integer> version = splitVersion(idSplit[1]);
        if (version.containsKey("major")) major = version.get("major");
        if (version.containsKey("minor")) minor = version.get("minor");
        if (version.containsKey("release")) release = version.get("release");

        return new TomcatVersion(major, minor, release);

    }

    private HashMap<String, Integer> splitVersion(String versionStr) {
        HashMap<String, Integer> version = new HashMap<>();

        String[] tomcatVersionSplit = versionStr.split("\\.");
        try {
            int major = Integer.parseInt(tomcatVersionSplit[0]);
            version.put("major", major);
        } catch (Exception ignored) {
        }
        try {
            int minor = Integer.parseInt(tomcatVersionSplit[1]);
            version.put("minor", minor);
        } catch (Exception ignored) {
        }
        try {
            int release = Integer.parseInt(tomcatVersionSplit[2]);
            version.put("release", release);
        } catch (Exception ignored) {
        }

        return version;
    }

    private static class TomcatVersion {
        public final int major;
        public final int minor;
        public final int release;

        public TomcatVersion(int major, int minor, int release) {
            this.major = major;
            this.minor = minor;
            this.release = release;
        }

        /**
         * @param v2 tom compare
         * @return value < 0 if v2 smaller; value == 0 if v2 equal; value > 0 if v2 bigger;
         */
        public int compare(TomcatVersion v2) {
            if (this.major > v2.major) return -1;
            if (this.major < v2.major) return 1;
            // this.major == v2.major
            if (this.minor > v2.minor) return -1;
            if (this.minor < v2.minor) return 1;
            // this.minor == v2.minor
            if (this.release > v2.release) return -1;
            if (this.release < v2.release) return 1;
            // this.release == v2.release
            return 0;
        }

        @Override
        public String toString() {
            return String.format("%d.%d.%d", major, minor, release);
        }
    }

    private static class Vulnerability {
        private String id;
        private String description;

        public Vulnerability(String id, String description) {
            this.id = id;
            this.description = description;
        }
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
