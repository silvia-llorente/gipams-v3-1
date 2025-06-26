<%@page import="Utils.Utils"%>
<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();
print.println(Prints.printHead("Crypt4gh utility")+
"<div class=\"container\">\n" +
"    <h1>Encrypt or decrypt files using the Crypt4GH standard</h1>\n"+
"    <div class=\"row\">\n" +
"        <form  method=\"POST\" action=\"crypt4ghEnc\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select plain text file to encrypt</span>\n" +
"                    <input type=\"file\" name=\"plain\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Encrypt file!\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form  method=\"POST\" action=\"crypt4ghDec\" enctype=\"multipart/form-data\" class=\"col s12\">\n" +
"            <div class=\"file-field input-field\">\n" +
"                <div class=\"btn\">\n" +
"                    <span>Select encrypted file to decrypt</span>\n" +
"                    <input type=\"file\" name=\"cipher\" accept=\"text\">\n" +
"                </div>\n" +
"                <div class=\"file-path-wrapper\">\n" +
"                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">\n" +
"                </div>\n" +
"            </div>\n" +      
"            <div class=\"input-field\">\n" +
"                <button class=\"btn waves-effect waves-light\" type=\"submit\">\n" +
"                    <i class=\"material-icons dp48\">file_upload</i> Decrypt file!\n" +
"                </button>\n" +
"            </div>\n" +
"        </form>\n" +
"        <form action=\"home.jsp\" method=\"POST\" class=\"col s12\">\n" +
"            <button class=\"btn waves-effect waves-light\" >Home</button>\n" +
"        </form>\n" +
"    </div>\n" +
"</div>\n" +
"<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>\n" +
"</body>\n" +
"</html>");
%>