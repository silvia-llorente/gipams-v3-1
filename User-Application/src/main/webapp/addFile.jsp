<%@page import="Utils.Utils" %>
<%@page import="Utils.antiCSRF" %>
<%@page import="Utils.CSPdelivery" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<% if (!Utils.checkAuth(request)) {
    System.out.println("Permission denied!");
    response.sendRedirect("login.jsp");
    return;
} %>
<%
    String CSPnonce = CSPdelivery.getNonce();
    String headerName = antiCSRF.headerName;
    String cookieName = antiCSRF.cookieName;

    response.addHeader("Content-Security-Policy", CSPdelivery.getHeader(CSPnonce));
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Add File</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

</head>
<body>
<nav>
    <div class="nav-wrapper blue darken-1">
        <ul id="nav-mobile" class="left hide-on-med-and-down">
            <li><a href="home.jsp">Home</a></li>
            <li><a href="addFile.jsp" class="collection-item">Add file</a></li>
            <li><a href="ownFiles.jsp">Own files</a></li>
            <li><a href="searchDatasetGroup.jsp">Search Dataset Group</a></li>
            <li><a href="searchDataset.jsp">Search Dataset</a></li>
        </ul>
        <ul id="nav-mobile2" class="right hide-on-med-and-down">
            <li><a href="logout">Logout</a></li>
        </ul>
    </div>
</nav>

<div class="container">
    <h1>Create new file</h1>
    <div id="resultMessageBoxConfigParams" style="display: none" class="callout resultMessage" data-closable>
        <div id="updateMessageBoxConfigParams">
        </div>
        <%--close cross--%>
        <button class="close-button" aria-label="Close alert" type="button" data-close>
            <span aria-hidden="true">&times;</span>
        </button>
    </div>
    <div class="row">
        <form id="postNewFile" class="col s12">
            <div class="input-field">
                <input placeholder="File name" name="file_name" type="text">
                <label>File name</label>
            </div>
            <br>
            <div class="input-field">
                <button class="btn waves-effect waves-light" type="submit">
                    <i class="material-icons dp48">file_upload</i> Create file
                </button>
            </div>
        </form>
    </div>
</div>
</body>
</html>

<style>
    .resultMessage {
        flex-direction: row;
        justify-content: space-between;
        border-radius: 18px;
        margin: 5px;
        padding: 5px;
    }

    .close-button {
        background: transparent;
        border: 0;
    }
</style>

<script nonce="<%=CSPnonce%>" src="https://code.jquery.com/jquery-latest.js"></script>
<script nonce="<%=CSPnonce%>" src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
<script nonce="<%=CSPnonce%>" src="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.5.3/js/foundation.min.js"></script>
<script nonce="<%=CSPnonce%>">
    function getCsrfToken() {
        let name = '<%=cookieName%>' + "=";
        let decodedCookies = decodeURIComponent(document.cookie);
        let cookies = decodedCookies.split(';');
        for (let i in cookies) {
            let cookie = cookies[i].trim();
            if (cookie.indexOf(name) === 0) {
                return cookie.substring(name.length, cookie.length).split('.')[0];
            }
        }
        return "";
    }

    function showError(error){
        const divResultTxt = document.getElementById('updateMessageBoxConfigParams');
        const divResult = document.getElementById('resultMessageBoxConfigParams');
        divResultTxt.innerHTML = error;
        divResult.style.visibility = 'visible';
        divResult.style.background = '#f2dede';
        divResultTxt.style.color = '#c88685';
        divResult.style.display = 'flex';
    }

    $(document).ready(function () {
        const elem = document.getElementById('postNewFile');
        elem.addEventListener('submit', function (event) {
            event.preventDefault(); // Prevent default form submission
            const formData = new FormData(this);
            fetch('addFile', {
                method: 'POST',
                headers: {'<%=headerName%>': getCsrfToken()},
                body: formData
            })
                .then(response => {
                    if (response.redirected){
                        window.location.href = response.url;
                    }
                    if (!response.ok) {
                        showError('<p id="updateMessage">Add File <strong>ERROR</strong></p>');
                    }
                })
                .catch(error => {
                    showError('<p id="updateMessage">POST <strong>ERROR:</strong> ' + error + '</p>');
                });
        });
    });
</script>