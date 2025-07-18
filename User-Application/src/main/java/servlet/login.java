/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Utils.Utils;
import Utils.antiCSRF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author alumne
 */
@WebServlet(name = "login", urlPatterns = {"/sso/login"})
public class login extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));

        String code = getCode(request.getQueryString());
        if (code == null || code.isEmpty()) {
            String redirAuthUrl = props.getProperty("redirAuthUrl");
            String realm = props.getProperty("realm");
            String clientId = props.getProperty("clientId");
            String redirectUri = props.getProperty("redirectUri");
               
            //localhost:9090/realms/MPEG-G/account
            String authUrl = String.format("%s/realms/%s/protocol/openid-connect/auth?response_type=code&client_id=%s&scope=openid&redirect_uri=%s", 
                                           redirAuthUrl, realm, clientId, redirectUri);
            
            System.out.println(authUrl);
            response.sendRedirect(authUrl);
        } else {
            URL url = new URL(props.getProperty("authUrl") + "/realms/" + props.getProperty("realm") + "/protocol/openid-connect/token");
            System.out.println(url);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(("user-application:" + props.getProperty("clientSecret")).getBytes()));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String body = "grant_type=authorization_code&" +
                    "code=" + code +
                    "&redirect_uri=" + props.getProperty("redirectUri");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(login.class.getName()).log(Level.SEVERE, "CheckAuth request token error", ex);
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "", aux;
            while ((aux = br.readLine()) != null)
                res += aux;
            Utils.setToken(res, null);
            Cookie c = new Cookie("GIPAMS-auth", Utils.getToken());
            c.setHttpOnly(true);
            c.setPath("/UA");
            response.addCookie(c);

            try {
                Cookie antiCSRFcookie = antiCSRF.getCookie(request.getSession().getId());
                response.addCookie(antiCSRFcookie);
            } catch (UnrecoverableEntryException | CertificateException | KeyStoreException |
                     NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            String scheme = "http" + "://";
            String serverName = request.getServerName();
            String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
            String contextPath = request.getContextPath();
            String fin = scheme + serverName + serverPort + contextPath;
            response.sendRedirect(fin + "/home.jsp");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    private String getCode(String queryString) {
        if (queryString == null || queryString.isEmpty()) return null;
        String[] fields = queryString.split("&");
        String[] kv;
        for (int i = 0; i < fields.length; ++i) {
            kv = fields[i].split("=");
            if (kv[0].equals("code"))
                return kv[1];
        }
        return null;
    }

}
