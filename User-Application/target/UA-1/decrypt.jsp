<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();
print.println(Prints.printHead("Decrypt file")+
"<div class=\"container\">\n" +
"    <h1>Decrypt file</h1>\n"+
"    <div class=\"row\">\n" +
"        <form  method=\"POST\" action=\"decrypt\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select ciphertext file</span>\n" +
"                    <input type=\"file\" name=\"cipher\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select protection xml file</span>\n" +
"                    <input type=\"file\" name=\"pr\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select key file</span>\n" +
"                    <input type=\"file\" name=\"key\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +        
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Decrypt ciphertext\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\"home.jsp\" method=\"POST\" class=\"col s12\">\n" +
"            <button class=\"btn waves-effect waves-light\" >Go Back</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>