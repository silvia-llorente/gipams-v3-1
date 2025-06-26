<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();
String mpegfile = "", dg_id = "", dt_id = "";
String referer =  new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString();
if(request.getAttribute("mpegfile") != null) mpegfile = request.getAttribute("mpegfile").toString();
else mpegfile = request.getParameter("mpegfile");
if(request.getAttribute("dg_id") != null) dg_id = request.getAttribute("dg_id").toString();
else dg_id = request.getParameter("dg_id");
if(request.getAttribute("dt_id") != null) dt_id = request.getAttribute("dt_id").toString();
else dt_id = request.getParameter("dt_id");
if(mpegfile == null || dg_id == null) response.sendRedirect(referer);
if(dt_id != null && ! dt_id.isEmpty())print.println(Prints.printHead("Add Dataset protection")+
"<div class=\"container\">\n" +
"    <h1>Add Dataset Protection</h1>\n"); 
else print.println(Prints.printHead("Add DatasetGroup protection")+
"<div class=\"container\">\n" +
"    <h1>Add DatasetGroup Protection</h1>\n");
if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")) print.println("    <div class=\"card-panel green white-text\" >Dataset protection added successfully</div>\n"); 
else if(request.getAttribute("success") != null && request.getAttribute("success").equals("false")) print.println("    <div class=\"card-panel red white-text\" >Error adding dataset protection</div>\n");
print.println("    <div class=\"row\">\n" +
"        <form  method=\"POST\" action=\"addProtection\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n");
if(dt_id != null && ! dt_id.isEmpty())print.println("                    <span>Select Dataset Protection</span>\n"+
        "                    <input type=\"file\" name=\"dt_pr\" accept=\"text/xml\">\n");
else print.println("                    <span>Select DatasetGroup Protection</span>\n"+
        "                    <input type=\"file\" name=\"dg_pr\" accept=\"text/xml\">\n");
print.println(
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div><br>\n" +
"            <input type=\"hidden\" name=\"mpegfile\" value=\""+mpegfile+"\" />\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\" />\n" +
"            <label for=\"keyTypes\"><h5>Protection key type</h5></label>\n" +
"            <div class=\"input-field\" id=\"keyTypes\">\n" +
"               <div>\n" +
"                   <label for=\"SymOpt\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"keyType\" value=\"sym\" id=\"SymOpt\" checked/>\n" +
"                       <span>Symmetric Key</span>\n" +
"                   </label>\n" +
"                   <label for=\"DerOpt\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"keyType\" value=\"der\" id=\"DerOpt\"/>\n" +
"                       <span>Key Derivation</span>\n" +
"                   </label>\n" +
"                   <label for=\"AsymOpt\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"keyType\" value=\"asym\" id=\"AsymOpt\"/>\n" +
"                       <span>Asymmetric Key</span>\n" +
"                   </label>\n" +
"               </div>\n" +
"            </div>\n" +
"            <label for=\"algTypes\"><h5>Cipher algorithm</h5></label>\n" +
"            <div class=\"input-field\" id=\"algTypes\">\n" +
"               <div>\n" +
"                   <label for=\"128\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"algType\" value=\"urn:mpeg:mpeg-g:protection:aes128-ctr\" id=\"128\" checked/>\n" +
"                       <span>AES-128 bit encryption</span>\n" +
"                   </label>\n" +
"                   <label for=\"192\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"algType\" value=\"urn:mpeg:mpeg-g:protection:aes192-ctr\" id=\"192\" />\n" +
"                       <span>AES-192 bit encryption</span>\n" +
"                   </label>\n" +
"                   <label for=\"256\">\n" +
"                       <input type=\"radio\" class=\"with-gap\" name=\"algType\" value=\"urn:mpeg:mpeg-g:protection:aes256-ctr\" id=\"256\"/>\n" +
"                       <span>AES-256 bit encryption</span>\n" +
"                   </label>\n" +
"               </div>\n" +
"            </div>\n" +
"            <div class=\"input-field\">\n" +
"               <input placeholder=\"Key name\" name=\"keyName\" type=\"text\" />\n" +
"               <label>Key name</label>\n" +
"            </div>\n" +
"           <label for=\"policy\"><h5>Regulació d'accés</h5></label>\n" +
"           <div class=\"input-field\" id=\"policy\">\n" +
"               <input placeholder=\"Rol\" name=\"role\" type=\"text\">\n" +
"               <select name=\"action\" >\n" +
"                   <option selected value=\"GetHierarchy\">GetHierarchy</option>\n");
if(dt_id == null) print.println("                   <option value=\"GetMetadataContentDG\">GetMetadataContentDG</option>\n");
else print.println("                   <option value=\"GetDataBySimpleFilter\">GetDataBySimpleFilter</option>\n" +
"                   <option value=\"GetMetadataContent\">GetMetadataContent</option>\n");
print.println("               </select>\n" +
"               <input placeholder=\"Data\" name=\"date\" type=\"date\">\n" +
"           </div>\n" +
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i>Add protection\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\""+referer+"\" method=\"POST\" class=\"col s12\">\n" +
"            <input type=\"hidden\" name=\"file_id\" value=\""+mpegfile+"\"/>\n" +
"            <input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\" />\n" +
"            <input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\" />\n" +
"            <button class=\"btn waves-effect waves-light\" >Go Back</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>