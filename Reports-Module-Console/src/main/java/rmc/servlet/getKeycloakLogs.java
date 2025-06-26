package rmc.servlet;

import kelp.Utils.KeycloakEventTypes;
import rmc.Utils.*;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import rmm.Models.CustomLog;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet("/reports/getKeycloakLogs")
public class getKeycloakLogs extends HttpServlet {

    Gson _gson = null;
    private Properties props = null;
    private String API_PATH = "";

    public getKeycloakLogs() {
        _gson = new Gson();

        props = new Properties();
        try {
            props.load(getKeycloakLogs.class.getClassLoader().getResourceAsStream("app.properties"));
            API_PATH = props.getProperty("reports.api.baseurl") + props.getProperty("reports.api.keycloakPath") + props.getProperty("reports.api.keycloakPath.userEvents");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();

        if (!Utils.checkAuth(req)) {
            System.out.println("Permission denied!");
            Utils.resetToken(null);
            String scheme = "https" + "://";
            String serverName = req.getServerName();
            String serverPort = (req.getServerPort() == 80) ? "" : ":" + req.getServerPort();
            String contextPath = req.getContextPath();
            String fin = scheme + serverName + serverPort + contextPath;
            String redirect = fin + "/home.jsp";
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("redirect", redirect);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");

        Date minTimeEntryDate = GetDateFromParameter("lastTimeEntry", req, resp);
        Date maxTimeEntryDate = GetDateFromParameter("firstTimeEntry", req, resp);

        String count = props.getProperty("logs_size");

        try {
            String reqUrl = API_PATH;
            String minLogTime = getDateStringForUrl(minTimeEntryDate);
            String maxLogTime = getDateStringForUrl(maxTimeEntryDate);

            reqUrl = reqUrl.concat(String.format("?max_log=%s&min_log=%s&count=%s", maxLogTime, minLogTime, count));

            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + Utils.getToken());
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int code = conn.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                List<CustomLog> logs = new ArrayList<>();

                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String res = "", aux;
                while ((aux = br.readLine()) != null)
                    res += aux;

                JSONArray data = new JSONArray(res);
                for (int i = 0; i < data.length(); ++i) {
                    JSONObject jo = data.getJSONObject(i);
                    long dateLong = jo.getLong("date");
                    Date date = new Date(dateLong);
                    Map<String, Object> log = jo.getJSONObject("log").toMap();
                    logs.add(new CustomLog(date, log));
                }

                for (CustomLog log : logs) {
                    out.println("<tr style=\"background-color:" + getTypeColor(log.getType()) + "\">");
                    out.println("<td style=\"display:none\">" + log.date.getTime() + "</td>");
                    out.println("<td>" + log.getDateStr(props.getProperty("tableDateFormat")) + "</td>");
                    out.println("<td>" + log.getType() + "</td>");
                    out.println("<td>" + log.getUsername() + "</td>");
                    out.println("<td>" + log.getIp() + "</td>");
                    out.println("<td>" + log.getError() + "</td>");
                    out.println("</tr>");
                }
                out.flush();
                out.close();

            } else {
                resp.sendError(code);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static String getDateStringForUrl(Date timeEntryDate) throws UnsupportedEncodingException {
        String logTime = "";
        if (timeEntryDate != null) {
            logTime = URLEncoder.encode(String.valueOf(timeEntryDate.getTime()), StandardCharsets.UTF_8.toString());
        }
        return logTime;
    }

    private Date GetDateFromParameter(String timeParameter, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String timeEntry = req.getParameter(timeParameter);
        Date timeEntryDate = null;
        if (timeEntry != null && !timeEntry.equals("")) {
            try {
                long longDate = Long.parseLong(timeEntry);
                timeEntryDate = new Date(longDate);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }

        return timeEntryDate;
    }

    private String getTypeColor(String type) {
        String color = "";
        if (type.equals(KeycloakEventTypes.LOGIN_OK)) {
            color = "lightgreen";
        } else if (type.equals(KeycloakEventTypes.LOGIN_ERR)) {
            color = "indianred";
        }

        return color;
    }
}
