/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import Utils.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

/**
 *
 * @author alumne
 */
@WebServlet(name = "crypt4ghDec", urlPatterns = {"/crypt4ghDec"})
@MultipartConfig
public class crypt4ghDec extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;}
        
        Part cipherP = request.getPart("cipher");
        
        if(cipherP == null){
            RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher(
                    new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString());
            dispatcher.forward(request, response);
            return;
        }
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        StreamDataBodyPart cipher = new StreamDataBodyPart("cipher",cipherP.getInputStream());
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            multipart = (FormDataMultiPart) formDataMultiPart
                    .bodyPart(cipher);
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(props.getProperty("workflowUrl") + "/api/v1/crypt4ghDec");
            final Response resp = target.request().header("Authorization", "Bearer "+Utils.getToken()).post(Entity.entity(multipart, multipart.getMediaType().toString()));
            res = resp.readEntity(String.class);
        }
        multipart.close();
        
        String filename = cipherP.getName();
        response.setHeader("Content-disposition", "attachment; filename=plain.txt");
        OutputStream out = response.getOutputStream();
        out.write(res.getBytes());
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
