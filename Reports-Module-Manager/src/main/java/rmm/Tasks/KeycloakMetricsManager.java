package rmm.Tasks;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import rmm.Database.IpBlockingMongoDB;
import rmm.Database.MetricsMongoDB;
import rmm.Models.CustomLog;
import rmm.Models.IpBlocking;
import rmm.Models.LogMeasurement;
import rmm.Utils.DateUtils;
import rmm.Utils.LogsUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class KeycloakMetricsManager implements Job {
    private static Scheduler scheduler;

    public static void init() {
        main(new String[0]);
    }

    public static void finish() {
        try {
            scheduler.shutdown();
            System.out.println("TASK SCHEDULER STOPPED F0R " + KeycloakMetricsManager.class.toString());
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println("-----------START EXECUTION--------------");
        try {
            long minLogMs = DateUtils.GetNowTime().getTime() - 60000;
            Date minLogDate = DateUtils.GetDateFromParameter(Long.toString(minLogMs), 0L);
            Date maxLogDate = DateUtils.GetDateFromParameter("", Long.MAX_VALUE);
            List<CustomLog> logs = LogsUtils.ReadLogs(maxLogDate, minLogDate, -1);

            UpdateLastMinuteLogs(logs);

        } catch (Exception e) {
            System.out.println("------------EXECUTION ERROR---------------");
            System.out.println(e.getMessage());
            System.out.println("------------END EXECUTION---------------");
            throw new RuntimeException(e);
        }

        System.out.println("------------END EXECUTION---------------");
    }

    private void UpdateLastMinuteLogs(List<CustomLog> logs) throws IOException {
        ArrayList<String> IPs = MetricsMongoDB.GetInstance().GetAllIPs();
        Map<String, List<CustomLog>> groupedLogs = logs.stream()
                .collect(Collectors.groupingBy(CustomLog::getIp));
        ArrayList<String> newIPs = groupedLogs.keySet().stream()
                .filter(k -> !IPs.contains(k))
                .collect(Collectors.toCollection(ArrayList::new));
        IPs.addAll(newIPs);

        for (String ip : IPs) {
            ArrayList<CustomLog> ipLogs = new ArrayList<>();
            try {
                ipLogs = groupedLogs.get(ip)
                        .stream()
                        .sorted(Comparator.comparing(log -> log.date))
                        .collect(Collectors.toCollection(ArrayList::new));
            } catch (NullPointerException ignored) {
            }

            LogMeasurement existingIpMeasurement = MetricsMongoDB.GetInstance().GetMeasurementByIP(ip);
            boolean isNew = existingIpMeasurement == null;

            LogMeasurement measurement = new LogMeasurement(ip);
            measurement.UpdateErrorsMeasurements(ipLogs);
            measurement.UpdateAttemptsMeasurement(ipLogs);
            measurement.UpdateAttemptsIntervals(ipLogs);
            measurement.UpdateAccountUsage(ipLogs);

            if (isNew) {
                if (MetricsMongoDB.GetInstance().InsertMeasurement(measurement))
                    System.out.println("LOG INSERTED");
                else
                    System.out.println("ERROR INSERTING LOG");

                IpBlocking ipBlocking = new IpBlocking(ip);
                if (IpBlockingMongoDB.GetInstance().InsertBlocking(ipBlocking))
                    System.out.println("IP BLOCKING INSERTED");
                else
                    System.out.println("ERROR INSERTING IP BLOCKING");

            } else {
                measurement._id = existingIpMeasurement._id;
                if (MetricsMongoDB.GetInstance().UpdateMeasurement(measurement))
                    System.out.println("LOG UPDATED");
                else
                    System.out.println("ERROR UPDATING LOG");
            }

        }
    }

    public static void main(String[] args) {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();

            scheduler.start();
            System.out.println("TASK SCHEDULER STARTED F0R " + KeycloakMetricsManager.class.toString());

            JobDetail job = JobBuilder.newJob(KeycloakMetricsManager.class)
                    .withIdentity("job1", "group1")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger1", "group1")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }
}
