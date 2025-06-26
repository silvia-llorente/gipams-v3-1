<%@page import="java.io.PrintWriter" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.List" %>
<%@page import="java.io.BufferedReader" %>
<%@page import="java.io.InputStreamReader" %>
<%@page import="java.net.HttpURLConnection" %>
<%@page import="java.net.URL" %>
<%@page import="java.io.FileInputStream" %>
<%@page import="java.io.File" %>
<%@page import="java.util.Properties" %>
<%@ page import="rmc.servlet.getKeycloakLogs" %>
<%@ page import="rmc.Utils.Prints" %>
<%@ page import="rmc.Utils.Utils" %>

<% if (!Utils.checkAuth(request)) {
    System.out.println("Permission denied!");
    response.sendRedirect("sso/login");
    return;
} %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <title>WEBSITE ISSUES</title>
</head>
<body>

<% PrintWriter print = response.getWriter();
    print.println(Prints.printNav("WEBSITE ISSUES"));%>

<div class="cardParent">
    <div id="certificateInfo">
    </div>
    <div id="tomcatInfo">
    </div>
</div>

</body>
</html>

<script src="https://code.jquery.com/jquery-latest.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.5.3/js/foundation.min.js"></script>

<script>
    $(document).foundation();

    function getCertificateStatus() {
        $.get('reports/getCertificateStatus', {},
            function (responseBody) {
                let cert_panel = $('#certificateInfo');
                cert_panel.append(responseBody);
            });
    }

    function getTomcatStatus() {
        $.get('reports/getTomcatStatus', {},
            function (responseBody) {
                let tomcat_panel = $('#tomcatInfo');
                tomcat_panel.append(responseBody);
            });
    }

    $(document).ready(getCertificateStatus());
    $(document).ready(getTomcatStatus());

</script>

<style>
    body {
        overflow: hidden;
    }

    .cardParent {
        font-family: Arial, sans-serif;
        display: flex;
        margin: 0;
    }


    .card {
        margin: 30px;
        background-color: white;
        border-radius: 10px;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        padding: 20px;
        text-align: center;
        width: 300px;
    }

    .icon {
        font-size: 50px;
    }

    .card-column {
        display: flex;
        flex-direction: column;
        width: 100%;
        font-size: 9px;
    }

    .card-row {
        display: flex;
        justify-content: space-between;
    }

    .card-label {
        flex: 1;
        text-align: left;
        padding: 1px;
        margin: 0;
    }

    .card-value {
        flex: 2;
        text-align: right;
        padding: 1px;
        margin: 0;
    }

    .content h2 {
        margin: 10px 0;
        color: #333;
    }

    .content p {
        color: #666;
    }
</style>

