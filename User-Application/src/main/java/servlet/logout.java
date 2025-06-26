/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
@WebServlet(name = "logout", urlPatterns = {"/logout"})
public class logout extends HttpServlet {

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

        LoginApplication login = Utils.getApplication();
        if (login == null) {
            URL url = new URL(props.getProperty("authUrl") + "/realms/" + props.getProperty("realm") + "/protocol/openid-connect/logout");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic "+Base64.getEncoder().encodeToString(("user-application:"+props.getProperty("clientSecret")).getBytes()));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String body = "refresh_token=" + Utils.getRefreshToken();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            try(OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            } catch (IOException ex) {
                Logger.getLogger(login.class.getName()).log(Level.SEVERE, "CheckAuth request token error", ex);
            }
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
        } else if (login == LoginApplication.GOOGLE){
            // Just delete GIPAMS access by removing cookie.
            // Google's user only gives us permission to access our application with its account and get public information in a JWT.
            // Google does not store any persistent connection to our application.
        }

        request.getSession().invalidate();
        Cookie c[] = request.getCookies();
        if(c != null){
            for (Cookie c1 : c) {
                if (c1.getName().equals("GIPAMS-auth")) {
                    c1.setValue("");
                    c1.setPath("/UA");
                    c1.setMaxAge(0);
                    response.addCookie(c1);
                } else if (c1.getName().equals(antiCSRF.cookieName)) {
                    c1.setValue("");
                    c1.setPath("/UA");
                    c1.setMaxAge(0);
                    response.addCookie(c1);
                }
            }
        }
        Utils.resetToken(null);
        response.sendRedirect("home.jsp");
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
