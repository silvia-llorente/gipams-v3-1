package rmc.servlet;

import rmc.Utils.*;
import rmm.Models.DetectionParam;
import rmm.Models.DetectionParamCollection;

import com.google.gson.Gson;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/reports/updateDetectionParameters")
@MultipartConfig
public class updateDetectionParameters extends HttpServlet {

    Gson _gson = null;
    private String API_PATH = "";

    public updateDetectionParameters() {
        _gson = new Gson();

        Properties props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            API_PATH = props.getProperty("reports.api.baseurl") + props.getProperty("reports.api.keycloakPath") + props.getProperty("reports.api.keycloakPath.threatsManager.updateDetectionConfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
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

        Map<String, String[]> reqMap = req.getParameterMap();

        if (reqMap.isEmpty()) resp.sendError(HttpServletResponse.SC_BAD_REQUEST);

        DetectionParamCollection variables = new DetectionParamCollection();
        for (String key : reqMap.keySet()) {
            DetectionParam var = new DetectionParam();
            var._id = key;
            var.value = String.join("", reqMap.get(key));
            variables.add(var);
        }

        boolean operationResult = false;

        Gson gson = new Gson();
        String json = gson.toJson(variables);
        String body = "request=" + json;
        try {
            URL url = new URL(API_PATH);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + Utils.getToken());

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
            throws IOException, ServletException {
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
            throws IOException, ServletException {
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
