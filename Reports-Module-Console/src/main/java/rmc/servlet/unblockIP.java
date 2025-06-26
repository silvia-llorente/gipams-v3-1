package rmc.servlet;

import rmm.Models.IpBlocking;
import rmc.Utils.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/reports/unblockIp")
public class unblockIP extends HttpServlet {

    Gson _gson = null;
    private Properties props = null;
    private String API_PATH = "";

    public unblockIP() {
        _gson = new Gson();

        props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            API_PATH = props.getProperty("reports.api.baseurl") + props.getProperty("reports.api.keycloakPath") + props.getProperty("reports.api.keycloakPath.unblockIp");
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

        Gson gson = new Gson();
        boolean operationResult = false;

        try {
            StringBuilder requestBody = new StringBuilder();
            String line;
            BufferedReader reader = req.getReader();
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            reader.close();
            JsonObject jsonObject = gson.fromJson(requestBody.toString(), JsonObject.class);
            String ip = jsonObject.get("ip").getAsString();

            String reqUrl = API_PATH;
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + Utils.getToken());

            String body = "ip=" + ip;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("utf-8"));
            }

            if (conn.getResponseCode() == HttpServletResponse.SC_OK) {
                resp.setStatus(HttpServletResponse.SC_OK);
                operationResult = true;
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (IOException ex) {
            Logger.getLogger(updateDetectionParameters.class.getName()).log(Level.SEVERE, null, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("result", operationResult);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
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
