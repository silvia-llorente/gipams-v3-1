<%@page import="Utils.*"%>
<%@page import="java.io.PrintWriter"%>
<%
    if(!Utils.checkAuth(request)){System.out.println("Permission denied!");response.sendRedirect("login.jsp");return;}
    final PrintWriter writer = response.getWriter();
    writer.println(Prints.printHead("Edit Dataset"));
    writer.println("<div class=\"container\">"
            + "    <h1>Edit Dataset Group</h1>");
    if(request.getAttribute("success") != null && request.getAttribute("success").equals("true")){
        writer.println("<div class=\"card-panel green white-text\" text=\"${success}\">Dataset edited successfully!</div>");
    }
    else if(request.getAttribute("success") != null) {
        writer.println("<div class=\"card-panel red white-text\" >Error editing Dataset.</div>");
    }
    String referer =  new StringBuilder(new StringBuilder().append(request.getHeader("referer")).reverse().toString().split("/")[0]).reverse().toString();
    writer.println("<div class=\"row\">"
            + "        <form  method=\"POST\" action=\"editDataset\" enctype=\"multipart/form-data\" class=\"col s12\">"
            + "            <div class=\"file-field input-field\">"
            + "                <div class=\"btn\">"
            + "                    <span>Select Dataset Metadata</span>"
            + "                    <input type=\"file\" name=\"dt_md\" accept=\"text/xml\">"
            + "                </div>"
            + "                <div class=\"file-path-wrapper\">"
            + "                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">"
            + "                </div>"
            + "            </div>"
            + "            <div class=\"file-field input-field\">"
            + "                <div class=\"btn\">"
            + "                    <span>Select Dataset Protection</span>"
            + "                    <input type=\"file\" name=\"dt_pr\" accept=\"text/xml\">"
            + "                </div>"
            + "                <div class=\"file-path-wrapper\">"
            + "                    <input class=\"file-path validate\" type=\"text\" placeholder=\"Upload one file\">"
            + "                </div>"
            + "            </div>"
            + "            <input type=\"hidden\" name=\"mpegfile\" value=\""+request.getParameter("mpegfile")+"\"/>"
            + "            <input type=\"hidden\" name=\"dg_id\" value=\""+request.getParameter("dg_id")+"\" />"
            + "            <input type=\"hidden\" name=\"dt_id\" value=\""+request.getParameter("dt_id")+"\" />"
            + "            <div class=\"input-field\">"
            + "                <button class=\"btn waves-effect waves-light\" type=\"submit\">"
            + "                    <i class=\"material-icons dp48\">file_upload</i> Edit file"
            + "                </button>"
            + "            </div>"
            + "        </form>"
            + "        <form action=\""+referer+"\" method=\"POST\" class=\"col s12\">"
            + "            <input type=\"hidden\" name=\"file_id\" value=\""+request.getParameter("mpegfile")+"\"/>"
            + "            <input type=\"hidden\" name=\"dg_id\" value=\""+request.getParameter("dg_id")+"\" />"
            + "            <button class=\"btn waves-effect waves-light\" >Go Back</button>"
            + "        </form>"
            + "    </div>"
            + "</div>"
            + "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js\"></script>"
            + "</body>"
            + "</html>");
%>