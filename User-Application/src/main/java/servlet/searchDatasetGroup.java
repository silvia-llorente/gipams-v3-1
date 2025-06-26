/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import Utils.Prints;
import Utils.Utils;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author alumne
 */
@WebServlet(name = "searchDatasetGroup", urlPatterns = {"/searchDatasetGroup"})
public class searchDatasetGroup extends HttpServlet {
    
    private static void printHead(PrintWriter writer){
        writer.println(Prints.printHead("Search Dataset Group Result")+
"<div class=\"container\">\n" +
"    <h1>Matching Dataset Group</h1>\n");
    }
    
    private void printEnd(PrintWriter writer){
        writer.println("</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
    }

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
        
        String center = request.getParameter("center");
        String description = request.getParameter("description");
        String title = request.getParameter("title");
        String type = request.getParameter("type");
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
                
            URL url;
        try {
            url = new URL(props.getProperty("workflowUrl") + "/api/v1/findDatasetGroupMetadata"); //your url i.e fetch data from .
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Authorization","Bearer "+Utils.getToken());
            conn.setDoOutput(true);
            
            String body = "center="+center+"&description="+description+"&title="+title+"&type="+type;
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes("utf-8"));
                        
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String res = "",aux;
            while((aux = br.readLine()) != null)
                res += aux;
            System.out.println(res);
            JSONArray data = new JSONArray(res);
            final PrintWriter writer = response.getWriter();
            printHead(writer);
            if (res.isEmpty() || res.equals("[{}]")) {
                printEnd(writer);
                return;
            }
                        
            
            for(int i = 0; i < data.length(); ++i){
                JSONObject jo = data.getJSONObject(i);
                writer.println(Prints.printDG(String.valueOf(jo.getInt("dg_id")),String.valueOf(jo.getInt("id"))));
            }
            printEnd(writer);
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(searchDatasetGroup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(searchDatasetGroup.class.getName()).log(Level.SEVERE, null, ex);
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
