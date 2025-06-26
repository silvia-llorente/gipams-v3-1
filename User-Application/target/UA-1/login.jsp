<%@page import="Utils.Prints" %>
<%@page import="java.io.PrintWriter" %>
<%@page import="java.util.*" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="Utils.Utils" %>
<%@page import="java.util.Properties" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.File" %>

<% Properties props = new Properties();
    props.load(new FileInputStream(new File(request.getServletContext().getRealPath("/WEB-INF/classes/app.properties"))));
    String clientIDGoogle = props.getProperty("clientIdGoogle");
    String redirectUriGoogle = props.getProperty("redirectUriGoogle");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Login</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
</head>
<body>
<script src="https://accounts.google.com/gsi/client?hl=en" async defer></script>
<nav>
    <div class="nav-wrapper blue darken-1"></div>
</nav>
<div style="display: flex; flex-direction: row; height: 50%;">
    <div style="display: flex; flex-direction: column; align-self: center; width: 100%;">
        <h1 class="header" style="align-self: center;">Login</h1>
        <div style="display: flex; flex-direction: column; align-self: center;">
            <button onclick="loginKeycloak();" style="margin: 5px;">I have an account</button>

            <div style="margin: 5px;">
                <div id="g_id_onload"
                     data-client_id="<%=clientIDGoogle%>"
                     data-login_uri="<%=redirectUriGoogle%>"
                     data-auto_prompt="false">
                </div>
                <div class="g_id_signin"
                     data-type="standard"
                     data-size="large"
                     data-width="200"
                     data-theme="outline"
                     data-text="continue_with"
                     data-shape="square"
                     data-locale="en"
                     data-logo_alignment="left">
                </div>
            </div>

<%--            <button onclick="loginFacebook()" style="margin: 5px;">Facebook</button>--%>
<%--            <button onclick="loginLinkedin()" style="margin: 5px;">Linkedin</button>--%>
        </div>
    </div>
</div>
        
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
<script src="https://code.jquery.com/jquery-3.7.0.min.js" type="text/javascript"></script>
<script src="https://code.jquery.com/ui/1.13.2/jquery-ui.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
<script type="text/javascript">
    function loginKeycloak() {
        window.location.assign("sso/login");
    }

    // function loginFacebook() {
    //     window.location.assign("sso/loginFacebook")
    // }
    //
    // function loginLinkedin() {
    //     window.location.assign("sso/loginLinkedin")
    // }
</script>
</body>
</html>

