<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="rmc.Utils.Utils" %>
<%@ page import="rmc.Utils.Prints" %>
<%@ page import="java.io.PrintWriter" %>

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
    <title>REPORTS CONSOLE</title>
</head>
<body>

<% PrintWriter print = response.getWriter();
    print.println(Prints.printNav("REPORTS CONSOLE"));%>

<div class="dashboard">
    <div class="dashboard-button" style="background-color: bisque; color: black;"
         onclick="navigate('keycloakThreats.jsp')">
        <div class="button-title">KEYCLOAK LOGIN THREATS</div>
    </div>

    <div class="dashboard-button" style="background-color: palegreen; color: black;"
         onclick="navigate('websiteIssues.jsp')">
        <div class="button-title">WEBSITE ISSUES</div>
    </div>

</div>

</body>
</html>

<script>
    function navigate(page) {
        window.location.href = page;
    }
</script>

<style>
    .dashboard {
        display: flex;
        gap: 20px;
    }

    .dashboard-button {
        margin: 30px;
        width: 200px;
        height: 200px;
        display: flex;
        flex-direction: column;
        border: 1px solid #ccc;
        border-radius: 5px;
        overflow: hidden;
        cursor: pointer;
        transition: transform 0.2s;
    }

    .dashboard-button:hover {
        transform: scale(1.05);
    }

    .button-title {
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 32px;
        font-weight: bold;
        text-align: center;
    }
</style>
