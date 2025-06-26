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
@WebServlet(name = "addDataset", urlPatterns = {"/addDataset"})
@MultipartConfig
public class addDataset extends HttpServlet {

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
        
        String mpegfile = request.getParameter("mpegfile");
        String dg_id = request.getParameter("dg_id");
        Part dt_mdP = request.getPart("dt_md");
        Part dt_prP = request.getPart("dt_pr");
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        StreamDataBodyPart dt_md = new StreamDataBodyPart("dt_md",dt_mdP.getInputStream());
        StreamDataBodyPart dt_pr = new StreamDataBodyPart("dt_pr",dt_prP.getInputStream());
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            multipart = (FormDataMultiPart) formDataMultiPart
                    .field("mpegfile", mpegfile)
                    .field("dg_id", dg_id)
                    .bodyPart(dt_md)
                    .bodyPart(dt_pr);
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(props.getProperty("workflowUrl") + "/api/v1/addDataset");
            final Response resp = target.request().header("Authorization", "Bearer "+Utils.getToken()).post(Entity.entity(multipart, multipart.getMediaType().toString()));
            res = resp.readEntity(String.class);
        }
        multipart.close();
        
        System.out.println(res);

        request.setAttribute("mpegfile", mpegfile);
        request.setAttribute("dg_id", dg_id);
        if(!res.isEmpty())request.setAttribute("success","true");
        else request.setAttribute("success","false");
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/ownFiles.jsp");
        dispatcher.forward(request, response);
        
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
