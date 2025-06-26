package rmm.Utils;

import org.json.JSONObject;
import rmm.Models.CustomFileMap;
import rmm.Models.CustomLog;
import rmm.service.keycloak.KeycloakUserEventLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LogsUtils {

    public static void WriteLogs(String payload) throws IOException {
        Properties props = new Properties();
        props.load(KeycloakUserEventLogger.class.getClassLoader().getResourceAsStream("app.properties"));
        String DIR_PATH = props.getProperty("logsPath");

        String event = KeycloakEventUtils.parseEvent(new JSONObject(payload), props);

        FileUtils.createDirectory(DIR_PATH);
        String fileNameAppend = props.getProperty("logsFilenameKeycloak");
        String ext = props.getProperty("logsExt");
        String fileName = DIR_PATH + "/" + FileUtils.genFileNameWithDate(props, fileNameAppend, ext);
        FileUtils.editFile(fileName, event);
    }

    public static List<CustomLog> ReadLogs(Date max_log_date, Date min_log_date, int size) throws ParseException, IOException {
        Properties props = new Properties();
        props.load(LogsUtils.class.getClassLoader().getResourceAsStream("app.properties"));
        String DIR_PATH = props.getProperty("logsPath");

        String dayPattern = props.getProperty("logsDaysFormat");
        SimpleDateFormat dayFormatter = new SimpleDateFormat(dayPattern);

        String datePattern = props.getProperty("queryDateFormat");
        SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        Date minLogDay = dayFormatter.parse(dayFormatter.format(min_log_date));

        List<CustomFileMap> logFiles = FileUtils.getFilesNames(DIR_PATH).stream().map(f -> {
            try {
                String docDayStr = f.getFileName().toString().split(props.getProperty("logsFilenameKeycloak"))[0];
                Date docDay = dayFormatter.parse(docDayStr);
                if (docDay.compareTo(minLogDay) >= 0 && docDay.compareTo(max_log_date) <= 0)
                    return new CustomFileMap(docDay, f);
            } catch (ParseException ignored) {
            }
            return null;
        }).filter(Objects::nonNull).sorted((fileMap1, fileMap2) -> fileMap2.docDay.compareTo(fileMap1.docDay)).collect(Collectors.toList());

        List<CustomLog> logsList = new ArrayList<>();
        for (CustomFileMap file : logFiles) {
            List<CustomLog> lineLogs = Files.readAllLines(file.filepath, StandardCharsets.UTF_8)
                    .stream()
                    .map(line -> {
                        try {
                            if (!line.isEmpty()) {
                                String[] parsed = line.split("\\]");
                                String time = parsed[0].split("\\[")[1];
                                String log = parsed[1];

                                String lineDateStr = dayFormatter.format(file.docDay) + "T" + time;
                                Date lineDate = dateFormatter.parse(lineDateStr);

                                return new CustomLog(lineDate, log);
                            }
                        } catch (ParseException ignored) {
                        }
                        return null;
                    }).filter(Objects::nonNull)
                    .sorted((cfm1, cfm2) -> cfm2.date.compareTo(cfm1.date))
                    .collect(Collectors.toList());

            for (int i = 0; i < lineLogs.size(); i++) {
                if (logsList.size() == size) break;

                CustomLog log = lineLogs.get(i);

                if (log.date.compareTo(min_log_date) > 0 && log.date.compareTo(max_log_date) < 0) {
                    logsList.add(log);
                }
            }

            if (logsList.size() == size)
                break;
        }
        return logsList;
    }
}
