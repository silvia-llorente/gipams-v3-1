<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="Utils.Utils"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.HttpURLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.util.Properties"%>

    <%
        if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;}
        final PrintWriter writer = response.getWriter();
        
        writer.println(Prints.printHead("Own files"));
        
        Properties props = new Properties();
        props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
        
        URL url = new URL(props.getProperty("workflowUrl") + "/api/v1/ownFiles");//your url i.e fetch data from .
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer "+Utils.getToken());
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP Error code : " + conn.getResponseCode());
        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String data = br.readLine();
        conn.disconnect();
        List< Map<String, String> > t = Utils.JSON_to_ListMap(data);
        writer.println("<div class=\"container\">\n<h1>Files</h1>\n");
        for (int w = 0; w < t.size(); w++) {
            Map<String, String> aux = t.get(w);
            writer.println(Prints.printFile(aux.get("name"),aux.get("id")));
        }
        writer.println("</div>"
                + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>"
                + "</body>"
                + "</html>");
        %>