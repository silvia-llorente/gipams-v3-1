package rmc.servlet;

import org.json.JSONArray;
import org.json.JSONObject;
import rmm.Models.IpBlocking;
import rmc.Utils.*;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@WebServlet("/reports/getBlockedIPs")
public class getBlockedIPs extends HttpServlet {
    private Properties props = null;
    private String API_PATH = "";

    public getBlockedIPs() {
        props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            API_PATH = props.getProperty("reports.api.baseurl") + props.getProperty("reports.api.keycloakPath") + props.getProperty("reports.api.keycloakPath.blockedIps");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();

        if (!Utils.checkAuth(req)) {
            System.out.println("Permission denied!");
            Utils.resetToken(null);
            String scheme = "https" + "://";
            String serverName = req.getServerName();
            String serverPort = (req.getServerPort() == 80) ? "" : ":" + req.getServerPort();
            String contextPath = req.getContextPath();
            String fin = scheme + serverName + serverPort + contextPath;
            String redirect = fin + "/home.jsp";
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("redirect", redirect);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");

        try {
            String reqUrl = API_PATH;

            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + Utils.getToken());
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int code = conn.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                List<IpBlocking> variables = getIPs(conn);

                for (IpBlocking var : variables) {
                    out.println("<div id=\"" + "ipblock_" + var.ip + "\" class=\"blocked-ip\">");
                    out.println("<label class=\"blocked-ip-label\">" + var.ip + "</label>");
                    out.println("<div class=\"blocked-ip-label-div\">");
                    out.println("<label class=\"blocked-date-label\">" + var.getDateStr(props.getProperty("ipBlockDaysFormat")) + "</label>");
                    out.println("<label class=\"blocked-date-label\">" + var.getDateStr(props.getProperty("ipBlockHoursFormatShort")) + "</label>");
                    out.println("</div>");
                    out.println("<button class=\"close-button\" aria-label=\"Close alert\" type=\"button\" onclick=\"unblockIP('" + var.ip + "')\">");
                    out.println("<span aria-hidden=\"true\">&times;</span>");
                    out.println("</button>");
                    out.println("</div>");
                }

            } else {
                resp.sendError(code);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private static List<IpBlocking> getIPs(HttpURLConnection conn) throws IOException {
        List<IpBlocking> variables = new ArrayList<>();

        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        StringBuilder res = new StringBuilder();
        String aux;
        while ((aux = br.readLine()) != null)
            res.append(aux);

        JSONArray data = new JSONArray(res.toString());
        for (int i = 0; i < data.length(); i++) {
            JSONObject jo = data.getJSONObject(i);
            IpBlocking var = new IpBlocking(
                    jo.getString("_id"),
                    jo.getString("ip"),
                    jo.getBoolean("blocked"),
                    jo.getLong("blockingDate"),
                    jo.getDouble("expirationTime")
            );
            variables.add(var);
        }
        return variables;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

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
