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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author alumne
 */
@WebServlet(name = "addFile", urlPatterns = {"/addFile"})
@MultipartConfig
public class addFile extends HttpServlet {

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
        if (!Utils.checkAuth(request)) {
            System.out.println("Permission denied!");
            redirect(request, response, "login.jsp");
            return;
        }

        if (!antiCSRF.validateToken(request)) {
            Logger.getLogger(antiCSRF.class.getName()).log(Level.SEVERE, "AntiCSRF token error");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String filename = request.getParameter("file_name");

        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));

        URL url = new URL(props.getProperty("workflowUrl") + "/api/v1/addFile");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // Depuración del token que llega
        System.out.println("Token utilizado: " + Utils.getToken());


        conn.setRequestProperty("Authorization", "Bearer " + Utils.getToken());
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String body = "file_name=" + filename;
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(login.class.getName()).log(Level.SEVERE, "AddFile request token error", ex);
        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String res = "", aux;
        while ((aux = br.readLine()) != null)
            res += aux;
        System.out.println(res);
        System.out.println("File added, redirecting...");
        redirect(request, response, "addDatasetGroup.jsp?mpegfile=" + res);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        //String scheme = "https" + "://";
        String scheme = "http" + "://";
        String serverName = request.getServerName();
        String serverPort = (request.getServerPort() == 80) ? "" : ":" + request.getServerPort();
        String contextPath = request.getContextPath();
        String fin = scheme + serverName + serverPort + contextPath;
        String redirect = fin + "/" + path;
        response.sendRedirect(redirect);
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
