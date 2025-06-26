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
@WebServlet(name = "addProtection", urlPatterns = {"/addProtection"})
@MultipartConfig
public class addProtection extends HttpServlet {

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
        //response.setContentType("text/html;charset=UTF-8");
        if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;}
        
        String mpegfile = request.getParameter("mpegfile");
        String dg_id = request.getParameter("dg_id");
        String dt_id = request.getParameter("dt_id");
        String keyType = request.getParameter("keyType");
        String algType = request.getParameter("algType");
        String keyName = request.getParameter("keyName");
        String role = request.getParameter("role");
        String action = request.getParameter("action");
        String date = request.getParameter("date");
        Part dt_prP = request.getPart("dt_pr");
        Part dg_prP = request.getPart("dg_pr");
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        StreamDataBodyPart dt_pr = null, dg_pr = null;
        if(dt_prP != null && dt_prP.getSize() > 0) dt_pr = new StreamDataBodyPart("dt_pr",dt_prP.getInputStream());
        if(dg_prP != null && dg_prP.getSize() > 0) dg_pr = new StreamDataBodyPart("dg_pr",dg_prP.getInputStream());
        // TODO Check string params not null
        final FormDataMultiPart multipart;
        String res;
        try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
            multipart = (FormDataMultiPart) formDataMultiPart
                    .field("mpegfile", mpegfile)
                    .field("dg_id", dg_id)
                    .field("dt_id", dt_id)
                    .field("keyType", keyType)
                    .field("algType", algType)
                    .field("keyName", keyName)
                    .field("role", role)
                    .field("action", action)
                    .field("date", date);
            if(dt_pr != null) multipart.bodyPart(dt_pr);
            if(dg_pr != null) multipart.bodyPart(dg_pr);
            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
            final WebTarget target = client.target(props.getProperty("workflowUrl") + "/api/v1/addProtection");
            final Response resp = target.request().header("Authorization", "Bearer "+Utils.getToken()).post(Entity.entity(multipart, multipart.getMediaType().toString()));
            res = resp.readEntity(String.class);
        }
        multipart.close();
        
        //System.out.println(res);

        request.setAttribute("mpegfile", mpegfile);
        request.setAttribute("dg_id", dg_id);
        request.setAttribute("dt_id", dt_id);
        if(!res.isEmpty())request.setAttribute("success","true");
        else request.setAttribute("success","false");
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/addProtection.jsp");
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
