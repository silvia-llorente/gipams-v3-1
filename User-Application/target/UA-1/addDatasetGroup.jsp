<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();//System.out.println(request.getAttribute("file_id"));
String id = "";
if(request.getAttribute("mpegfile") != null) id = request.getAttribute("mpegfile").toString();
else id = request.getParameter("mpegfile");
if(id == null) response.sendRedirect("home.jsp");
String referer =  new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString();
print.println(Prints.printHead("Add DatasetGroup")+
"<div class=\"container\">\n" +
"    <h1>Add Dataset Group</h1>\n"); 
if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")) print.println("    <div class=\"card-panel green white-text\" >Dataset Group added successfully</div>\n"); 
else if(request.getAttribute("success") != null && request.getAttribute("success").equals("false")) print.println("    <div class=\"card-panel red white-text\" >Error adding DatasetGroup</div>\n");
print.println("    <div class=\"row\">\n" +
"        <form  method=\"post\" action=\"addDatasetGroup\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Dataset Group Metadata</span>\n" +
"                    <input type=\"file\" name=\"dg_md\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Dataset Group Protection</span>\n" +
"                    <input type=\"file\" name=\"dg_pr\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Dataset Metadata</span>\n" +
"                    <input type=\"file\" name=\"dt_md\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Dataset Protection</span>\n" +
"                    <input type=\"file\" name=\"dt_pr\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Access Unit Data</span>\n" +
"                    <input type=\"file\" name=\"au_data\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select Access Unit Protection</span>\n" +
"                    <input type=\"file\" name=\"au_pr\" accept=\"text/xml\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <input type=\"hidden\" name=\"file_id\" value=\""+id+"\" />\n" +
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Create file\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\""+referer+"\" method=\"POST\" class=\"col s12\">\n" +
"            <input type=\"hidden\" name=\"file_id\" value=\""+id+"\"/>\n" +
"            <button class=\"btn waves-effect waves-light\" >Go Back</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>