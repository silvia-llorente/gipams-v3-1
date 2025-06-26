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
@WebServlet(name = "addAccessUnit", urlPatterns = {"/addAccessUnit"})
@MultipartConfig
public class addAccessUnit extends HttpServlet {

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
        String dt_id = request.getParameter("dt_id");
        Part au_dataP = request.getPart("au_data");
        Part au_prP = request.getPart("au_pr");
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        StreamDataBodyPart au_data = null, au_pr = null;
        if(au_dataP == null || au_dataP.getSize() == 0) {
            request.setAttribute("success","false");
            RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/addAccessUnit.jsp");
            dispatcher.forward(request, response);
        }
        else {
            au_data = new StreamDataBodyPart("au_data",au_dataP.getInputStream());
            if(au_prP != null) au_pr = new StreamDataBodyPart("au_pr",au_prP.getInputStream());
            final FormDataMultiPart multipart;
            String res;
            try (FormDataMultiPart formDataMultiPart = new FormDataMultiPart()) {
                multipart = (FormDataMultiPart) formDataMultiPart
                        .field("mpegfile", mpegfile)
                        .field("dg_id", dg_id)
                        .field("dt_id", dt_id)
                        .bodyPart(au_data)
                        .bodyPart(au_pr);
                final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
                final WebTarget target = client.target(props.getProperty("workflowUrl") + "/api/v1/addAccessUnit");
                final Response resp = target.request().header("Authorization", "Bearer "+Utils.getToken()).post(Entity.entity(multipart, multipart.getMediaType().toString()));
                res = resp.readEntity(String.class);
            }
            multipart.close();

            System.out.println(res);

            request.setAttribute("mpegfile", mpegfile);
            request.setAttribute("dg_id", dg_id);
            request.setAttribute("dt_id", dt_id);
            if(!res.isEmpty())request.setAttribute("success","true");
            else request.setAttribute("success","false");
            RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/addAccessUnit.jsp");
            dispatcher.forward(request, response);
        }
        
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
