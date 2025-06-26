<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Utils.Utils"%>
        
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<%
    PrintWriter print = response.getWriter();
    print.println(Prints.printHead("Menu"));
    print.println("<body>\n" +
        "<div id=\"links\" class=\"section scrollspy\">\n" +
            "<div class=\"row\">\n" +
                "<div class=\"col s12\">\n");
    print.println("<h2> Hello, "+ Utils.getUsername(request) + "!</h2>");
    print.println("<h3 class=\"header\">Menu</h3>\n" +
                    "<div class=\"collection\">\n" +
                        "<a href=\"addFile.jsp\" class=\"collection-item\">Add file</a>\n" +
                        "<a href=\"ownFiles.jsp\" class=\"collection-item\">Own files</a>\n" +
                        "<a href=\"searchDatasetGroup.jsp\" class=\"collection-item\">Search Dataset Group</a>\n" +
                        "<a href=\"searchDataset.jsp\" class=\"collection-item\">Search Dataset</a>\n" +
                    "</div>\n" +
                "</div>\n" +
            "</div>\n" +
        "</div>\n" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
    "</body>\n" +
"</html>\n" );
%>