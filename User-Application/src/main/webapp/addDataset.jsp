<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();//System.out.println(request.getAttribute("file_id"));
String mpegfile = "", dg_id = "";
String referer =  new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString();
if(request.getAttribute("file_id") != null) mpegfile = request.getAttribute("file_id").toString();
else mpegfile = request.getParameter("file_id");
if(request.getAttribute("dg_id") != null) dg_id = request.getAttribute("dg_id").toString();
else dg_id = request.getParameter("dg_id");
if(mpegfile == null || dg_id == null) response.sendRedirect(referer);
print.println(Prints.printHead("Add Dataset")+
"<div class=\"container\">\n" +
"    <h1>Add Dataset</h1>\n"); 
if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")) print.println("    <div class=\"card-panel green white-text\" >Dataset added successfully</div>\n"); 
else if(request.getAttribute("success") != null && request.getAttribute("success").equals("false")) print.println("    <div class=\"card-panel red white-text\" >Error adding dataset</div>\n");
print.println("    <div class=\"row\">\n" +
"        <form  method=\"post\" action=\"addDataset\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
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
"            <input type=\"hidden\" name=\"mpegfile\" value=\""+mpegfile+"\" />\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Create file\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\""+referer+"\" method=\"POST\" class=\"col s12\">\n" +
"            <input type=\"hidden\" name=\"file_id\" value=\""+mpegfile+"\"/>\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <button class=\"btn waves-effect waves-light\" >Go Back</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>