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
    <title>KEYCLOAK LOGIN THREATS</title>
</head>
<body>

<% PrintWriter print = response.getWriter();
    print.println(Prints.printNav("KEYCLOAK LOGIN THREATS"));%>

<div id="container">

    <div id="content-box">
        <table id="log_table">
            <thead>
            <tr>
                <th style="display:none;"></th>
                <th></th>
                <th>TYPE</th>
                <th>UserName</th>
                <th>IP</th>
                <th>ERROR</th>
            </tr>
            </thead>
            <tbody id="log_table_body"></tbody>
        </table>
    </div>

    <div id="blockedIPs-box">
        <form id="IpBlockingExpirationTimeForm" class="col s12">
            <h5 style="align-self: center;">Blocked IPs</h5>
            <div id="resultMessageBoxExpirationTime" style="display: none" class="callout resultMessage" data-closable>
                <div id="updateMessageBoxExpirationTime">
                </div>
                <%--close cross--%>
                <button class="close-button" aria-label="Close alert" type="button" data-close>
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="form-group" style="margin-top: 6px; margin-bottom: 2px">
                <label class="form-label" for="IpBlockingExpirationTime">Block expiration time (sec.)</label>
                <input class="form-input" name="IpBlockingExpirationTime" id="IpBlockingExpirationTime" type="text"
                       onkeypress="return isNumberDecimal(event)" value=""/>
                <button class="btn waves-effect waves-light"
                        style="position: relative; margin-left: 5px; text-align: center; line-height: 26px;"
                        type="submit">Save
                </button>
            </div>
        </form>
        <div id="blockedIPs-box-cards">
        </div>
    </div>

    <form id="detection-config-params" class="col s12">
        <h5 style="align-self: center;">Detection Parameters</h5>
        <div id="resultMessageBoxConfigParams" style="display: none" class="callout resultMessage" data-closable>
            <div id="updateMessageBoxConfigParams">
            </div>
            <%--close cross--%>
            <button class="close-button" aria-label="Close alert" type="button" data-close>
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
    </form>

</div>

</body>
</html>

<script src="https://code.jquery.com/jquery-latest.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.5.3/js/foundation.min.js"></script>

<script>
    $(document).foundation();

    function getRecentLogs(latest_log = "") {
        $.get('reports/getKeycloakLogs', {
            lastTimeEntry: latest_log,
        }, function (responseText) {
            if (responseText.redirect) {
                window.location.href = responseText.redirect;
            } else {
                let table_body = $('#log_table_body');
                table_body.prepend(responseText);
            }
        });
    }

    function getDetectionParameters() {
        $.get('reports/getDetectionParameters', {},
            function (responseBody) {
                if (responseBody.redirect) {
                    window.location.href = responseBody.redirect;
                } else {
                    let config_panel = $('#detection-config-params');
                    config_panel.append(responseBody);
                }
            });
    }

    function getBlockedIPs() {
        $.get('reports/getBlockedIPs', {},
            function (responseBody) {
                if (responseBody.redirect) {
                    window.location.href = responseBody.redirect;
                } else {
                    let blocking_panel = $('#blockedIPs-box-cards');
                    let child = blocking_panel[0].lastElementChild;
                    while (child) {
                        blocking_panel[0].removeChild(child);
                        child = blocking_panel[0].lastElementChild;
                    }

                    blocking_panel.append(responseBody);
                }
            });
    }

    function getExpirationParam() {
        $.get('reports/getIpBlockingExpirationTime', {},
            function (response) {
                if (response.redirect) {
                    window.location.href = response.redirect;
                } else {
                    let expirationParamInput = $('#IpBlockingExpirationTime');
                    expirationParamInput.val(response);
                }
            });
    }

    function unblockIP(ip) {
        fetch('reports/unblockIp', {
            method: 'POST',
            redirect: 'manual',
            headers: {
                'Content-Type': 'application/json' // Set the Content-Type header
            },
            body: JSON.stringify({ip: ip})
        })
            .then(response => {
                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                } else {
                    return response.json();
                }
            })
            .then(data => {
                if (data.result) {
                    // const id = 'ipblock_' + ip;
                    // const divBlockedIP = document.getElementById(id);
                    // const divBlockedIPs = document.getElementById('blockedIPs-box');
                    // divBlockedIPs.removeChild(divBlockedIP);
                } else {
                    alert("Unblock NOT DONE")
                }
            })
            .catch(error => {
                alert('Update ERROR: ' + error)
                console.error('Error:', error);
            });
    }

    $(document).ready(getRecentLogs());
    $(document).ready(getDetectionParameters());
    $(document).ready(getBlockedIPs());
    $(document).ready(getExpirationParam());
    $(document).ready(function () {
        let contentLoadTriggered = false;
        $("#content-box").scroll(function () {
            let box = $("#content-box");
            const scrollHeight = box[0].scrollHeight;
            const scrollTop = box.scrollTop();
            const scrollAux = Math.round(scrollHeight - scrollTop);
            const outerHeight = box.outerHeight();
            const outerHeightAux = outerHeight + 17;
            if (scrollAux <= outerHeightAux && !contentLoadTriggered) {
                contentLoadTriggered = true;
                let earliest_log = ""
                try {
                    let table_body = $('#log_table_body');
                    let last_log_row = table_body.find('tr').last()[0];
                    earliest_log = last_log_row.cells[0].textContent;
                } catch (e) {
                }

                $.get('reports/getKeycloakLogs', {
                    firstTimeEntry: earliest_log,
                }, function (responseText) {
                    if (responseText.redirect) {
                        window.location.href = responseText.redirect;
                    } else {
                        let table_body = $('#log_table_body');
                        table_body.append(responseText);
                        contentLoadTriggered = false;
                    }
                });
            }
        });
    });
    $(document).ready(function () {
        const elem = document.getElementById('detection-config-params');
        elem.addEventListener('submit', function (event) {
            event.preventDefault(); // Prevent default form submission
            const formData = new FormData(this);
            fetch('reports/updateDetectionParameters', {
                method: 'POST',
                redirect: 'manual',
                body: formData,
            })
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                        return;
                    } else {
                        return response.json();
                    }
                })
                .then(data => {
                    const divResultTxt = document.getElementById('updateMessageBoxConfigParams');
                    const divResult = document.getElementById('resultMessageBoxConfigParams');
                    if (data.result) {
                        divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>OK</strong></p>';
                        divResult.style.background = '#def2e0';
                        divResultTxt.style.color = '#85c886';
                    } else {
                        divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>NOT DONE</strong></p>';
                        divResult.style.background = '#f2dede';
                        divResultTxt.style.color = '#c88685';
                    }
                    divResult.style.visibility = 'visible';
                    divResult.style.display = 'flex';
                })
                .catch(error => {
                    const divResultTxt = document.getElementById('updateMessageBoxConfigParams');
                    const divResult = document.getElementById('resultMessageBoxConfigParams');
                    divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>ERROR:</strong> ' + error + '</p>';
                    console.error('Error:', error);
                    divResult.style.visibility = 'visible';
                    divResult.style.background = '#f2dede';
                    divResultTxt.style.color = '#c88685';
                    divResult.style.display = 'flex';
                });
        });
    });
    $(document).ready(function () {
        document.getElementById('IpBlockingExpirationTimeForm').addEventListener('submit', function (event) {
            event.preventDefault(); // Prevent default form submission
            const formData = new FormData(this);
            fetch('reports/updateIpBlockingExpirationTime', {
                method: 'POST',
                redirect: 'manual',
                body: formData,
            })
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                        return;
                    } else {
                        return response.json();
                    }
                })
                .then(data => {
                    const divResultTxt = document.getElementById('updateMessageBoxExpirationTime');
                    const divResult = document.getElementById('resultMessageBoxExpirationTime');
                    if (data.result) {
                        divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>OK</strong></p>';
                        divResult.style.background = '#def2e0';
                        divResultTxt.style.color = '#85c886';
                    } else {
                        divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>NOT DONE</strong></p>';
                        divResult.style.background = '#f2dede';
                        divResultTxt.style.color = '#c88685';
                    }
                    divResult.style.visibility = 'visible';
                    divResult.style.display = 'flex';
                })
                .catch(error => {
                    const divResultTxt = document.getElementById('updateMessageBoxExpirationTime');
                    const divResult = document.getElementById('resultMessageBoxExpirationTime');
                    divResultTxt.innerHTML = '<p id="updateMessage">Update <strong>ERROR:</strong> ' + error + '</p>';
                    console.error('Error:', error);
                    divResult.style.visibility = 'visible';
                    divResult.style.background = '#f2dede';
                    divResultTxt.style.color = '#c88685';
                    divResult.style.display = 'flex';
                });
        });
    });

    setInterval(function () {
        getBlockedIPs();
    }, 1000);

    setInterval(function () {
        let latest_log = "";
        try {
            let table_body = $('#log_table_body');
            let last_log_row = table_body.find('tr').first()[0];
            latest_log = last_log_row.cells[0].textContent;
        } catch (e) {
        }
        getRecentLogs(latest_log);
    }, 1000);

    function isNumberDecimal(event) {
        const charCode = (event.which) ? event.which : event.keyCode;
        const charOK = !(charCode !== 46 && charCode > 31 && charCode !== 45
            && (charCode < 48 || charCode > 57));
        if (!charOK) return false;

        let inputString = event.target.value;
        inputString = [inputString.slice(0, event.target.selectionStart), event.key, inputString.slice(event.target.selectionStart)].join('');
        return !isNaN(parseFloat(inputString)) && isFinite(inputString);
    }
</script>

<style>
    body {
        overflow: hidden;
    }

    #container {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: row;
        overflow: hidden;
        padding-bottom: 64px;
    }

    #content-box {
        overflow-y: auto;
        overflow-x: auto;
        width: 45%;
        height: 100%;
    }

    #log_table thead th {
        position: sticky;
        top: 0;
        background: black;
        color: white;
    }

    #log_table {
        border-collapse: collapse;
        width: 100%;
    }

    #log_table th,
    #log_table td {
        padding: 8px 16px;
        text-align: center;
        text-wrap: nowrap;
        border: 1px solid black;
    }

    #detection-save-btn {
        height: 30px;
        width: 100px;
        align-self: center;
        align-content: center;
        justify-content: center;
    }

    #detection-config-params {
        display: flex;
        flex-direction: column;
        overflow-y: auto;
        overflow-x: auto;
        width: 20%;
        height: 100%;
        margin: 30px;
    }

    .form-group {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 5px; /* Add slight margin for spacing */
    }

    .form-label {
        flex: 2; /* Allocate remaining space proportionally */
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        font-size: 16px;
        color: black;
        text-align: right; /* Align labels to the right */
        margin-right: 5px; /* Add slight margin for spacing */
    }

    .form-input {
        flex: 1; /* Allocate remaining space proportionally */
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif !important;
        font-size: 14px !important;
        padding: 3px !important;
        border: 1px solid blue !important;
        text-align: center; /* Align input in the center */
        max-width: 60px;
    }

    .info-popup {
        position: relative;
        margin-left: 5px;
        text-align: center;
        background-color: #BCDBEA;
        border-radius: 50%;
        width: 24px;
        height: 24px;
        font-size: 14px;
        line-height: 26px;
        cursor: default;
    }

    .info-popup:before {
        content: 'i';
        font-weight: bold;
        color: #fff;
    }

    .info-popup:hover p {
        display: block;
        transform-origin: 100% 0%;

        -webkit-animation: fadeIn 0.3s ease-in-out;
        animation: fadeIn 0.3s ease-in-out;

    }

    .info-popup p { /* The tooltip */
        display: none;
        text-align: left;
        background-color: #1E2021;
        padding: 20px;
        width: 300px;
        position: absolute;
        z-index: 100;
        border-radius: 3px;
        box-shadow: 1px 1px 1px rgba(0, 0, 0, 0.2);
        right: -4px;
        color: #FFF;
        font-size: 13px;
        line-height: 1.4;
    }

    .info-popup p:before { /* The pointer of the tooltip */
        position: absolute;
        content: '';
        width: 0;
        height: 0;
        border: 6px solid transparent;
        border-bottom-color: #1E2021;
        right: 10px;
        top: -12px;
    }

    .info-popup p:after { /* Prevents the tooltip from being hidden */
        width: 100%;
        height: 40px;
        content: '';
        position: absolute;
        top: -40px;
        left: 0;
    }

    @-webkit-keyframes fadeIn {
        0% {
            opacity: 0;
            transform: scale(0.6);
        }

        100% {
            opacity: 100%;
            transform: scale(1);
        }
    }

    @keyframes fadeIn {
        0% {
            opacity: 0;
        }
        100% {
            opacity: 100%;
        }
    }

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

    #blockedIPs-box {
        display: flex;
        flex-direction: column;
        overflow-y: auto;
        overflow-x: auto;
        width: 20%;
        height: 100%;
        margin: 0 30px 30px;
    }

    .blocked-ip {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        border: orange solid 1px;
        border-radius: 30px;
        padding: 10px;
        background: lightgoldenrodyellow;
        margin-top: 4px;
    }

    .blocked-ip-label {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        font-size: 16px;
        color: black;
        text-align: right;
        margin-right: 5px;
    }

    .blocked-date-label {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        font-size: .8rem;
        color: black;
        text-align: right;
        margin-right: 5px;
    }

    .blocked-ip-label-div {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: space-between;
        padding: 0;
    }

</style>
