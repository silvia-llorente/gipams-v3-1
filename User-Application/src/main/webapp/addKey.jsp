<%@page import="Utils.Prints"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="Utils.Utils"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<% if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;} %>
<% PrintWriter print = response.getWriter();
print.println(Prints.printHead("Add key"));
String mpegfile = request.getParameter("mpegfile"), dg_id = request.getParameter("dg_id"), dt_id = request.getParameter("dt_id");
%>
<div class="container">
    <%
    if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")) print.println("    <div class=\"card-panel green white-text\" >Key added successfully</div>\n"); 
else if(request.getAttribute("success") != null && request.getAttribute("success").equals("false")) print.println("    <div class=\"card-panel red white-text\" >Error adding key</div>\n");
    %>
    <h1>Add new key</h1>
    <div class="row">
        <form  method="POST" action="addKey" class="col s12">  
            <label for="keyTypes">Key type</label>
            <div class="input-field" id="keyTypes">
                <div>
                    <label for="SymOpt">
                        <input type="radio" class="with-gap" name="keyType" value="sym" id="SymOpt" checked/>
                        <span>Symmetric Key</span>
                    </label>
                    <label for="AsymOpt">
                        <input type="radio" class="with-gap" name="keyType" value="asym" id="AsymOpt"/>
                        <span>Asymmetric Key</span>
                    </label>
                </div>
            </div><br>
            <div class="input-field">
                <input placeholder="Symmetric key (base64)" name="key" type="text" required>
                <label>Symmetric key</label>
            </div><br>
            <div class="input-field">                
                <label>Asymmetric key</label>
                <input placeholder="Private key (base64)" type="text" name="privKey"/>
                <input placeholder="Public key (base64)" type="text" name="pubKey"/>
            </div><br>
            <%
            print.println("<input type=\"hidden\" name=\"mpegfile\" value=\""+mpegfile+"\"/>");
            print.println("<input type=\"hidden\" name=\"dg_id\" value=\""+dg_id+"\"/>");
            print.println("<input type=\"hidden\" name=\"dt_id\" value=\""+dt_id+"\"/>");
            %>
            <div class="input-field">
                <button class="btn waves-effect waves-light" type="submit">
                    <i class="material-icons dp48">file_upload</i> Upload key
                </button>
            </div>
        </form>
    </div>
</div>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
</body>
</html>