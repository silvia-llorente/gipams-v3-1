package servlet;

import Utils.Utils;
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
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "addKey", urlPatterns = {"/addKey"})
public class addKey extends HttpServlet {

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
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        String type = request.getParameter("keyType");
        String mpegfile = request.getParameter("mpegfile");
        String dg_id = request.getParameter("dg_id");
        String dt_id = request.getParameter("dt_id");
        String name = request.getParameter("name");
        String key = request.getParameter("key");
        String privKey = request.getParameter("privKey");
        String pubKey = request.getParameter("pubKey");
        
        URL url = new URL(props.getProperty("ps.url") + "/addSymKey");
        if(type.equals("asym")) url = new URL(props.getProperty("ps.url") + "/addRSAKey");
        else {
            request.setAttribute("success","false");
            RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/addKey.jsp");
            dispatcher.forward(request, response);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic "+Base64.getEncoder().encodeToString(("user-application:"+props.getProperty("clientSecret")).getBytes()));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String body = "mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id"+dt_id+"&name="+name+"&key="+key;
        if(type.equals("asym")) body = "mpegfile="+mpegfile+"&dg_id="+dg_id+"&dt_id"+dt_id+"&name="+name+"&privKey="+privKey+"&pubKey="+pubKey;
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try(OutputStream os = conn.getOutputStream()) {			
            os.write(body.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(login.class.getName()).log(Level.SEVERE, "AddKey request token error", ex);
        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String res = "",aux;
        while((aux = br.readLine()) != null)
            res += aux;

        if(Integer.parseInt(res) == 0)request.setAttribute("success","true");
        else request.setAttribute("success","false");
        RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/addKey.jsp");
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
