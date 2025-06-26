/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Utils.GoogleUserCredentials;
import Utils.GoogleUtils;
import Utils.LoginApplication;
import Utils.Utils;
import Utils.antiCSRF;
import org.json.JSONException;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author alumne
 */
@WebServlet(name = "loginGoogle", urlPatterns = {"/sso/loginGoogle"})
public class loginGoogle extends HttpServlet {

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

        String scheme = "https" + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        String fin = scheme + serverName + serverPort + contextPath;

        GoogleUserCredentials cred = GoogleUtils.ReadUserCredentials(request);

        if (cred != null) {
            int code = -1;
            try {
                URL url = new URL(props.getProperty("accountManagerUrl") + "/api/v1/insert_google_user");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + cred.token);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                try {
                    body.put("id", cred.id);
                } catch (JSONException jsonEx) {
                    System.out.println(jsonEx.getMessage());
                }

                OutputStream os = conn.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                System.out.println(body.toString());
                Logger.getLogger(loginGoogle.class.getName()).log(Level.INFO, "BODY to AM: " + body.toString());
                osw.write(body.toString());
                osw.flush();
                osw.close();

                code = conn.getResponseCode();

                System.out.println(code);
                Logger.getLogger(loginGoogle.class.getName()).log(Level.INFO, "RESPONSE CODE FROM AM: " + code);

            } catch (IOException ex) {
                Logger.getLogger(loginGoogle.class.getName()).log(Level.SEVERE, "Account manager request error", ex);
            }

            if (code == HttpURLConnection.HTTP_OK) {
                request.setAttribute("success", "true");
                System.out.println("Ok response");
                Logger.getLogger(loginGoogle.class.getName()).log(Level.INFO, "Ok response");

                Utils.setToken(cred.token, LoginApplication.GOOGLE);
                Cookie c = new Cookie("GIPAMS-auth", Utils.getToken());
                Logger.getLogger(loginGoogle.class.getName()).log(Level.INFO, "CREATED COOKIE GIPAMS-auth: " + Utils.getToken());
                c.setPath("/UA");
                response.addCookie(c);

                try {
                    Cookie antiCSRFcookie = antiCSRF.getCookie(request.getSession().getId());
                    response.addCookie(antiCSRFcookie);
                } catch (UnrecoverableEntryException | CertificateException | KeyStoreException |
                         NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

                response.sendRedirect(fin + "/home.jsp");
                Logger.getLogger(loginGoogle.class.getName()).log(Level.INFO, "REDIRECT SENT TO: " + fin + "/home.jsp");
                return;

            } else {
                request.setAttribute("success", "false");
                System.out.println("ERR response");
                Logger.getLogger(loginGoogle.class.getName()).log(Level.WARNING, "ERR response");
            }
        }
        response.sendRedirect(fin + "/login.jsp");
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

}
