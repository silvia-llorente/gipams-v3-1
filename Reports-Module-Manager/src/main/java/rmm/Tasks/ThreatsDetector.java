package rmm.Tasks;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import rmm.Database.DetectionMongoDB;
import rmm.Database.IpBlockingMongoDB;
import rmm.Database.MetricsMongoDB;
import rmm.Database.VariablesMongoDB;
import rmm.Management.Nginx;
import rmm.Models.*;
import rmm.Utils.DateUtils;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class ThreatsDetector implements Job {
    private static Scheduler scheduler;

    public static void init() {
        main(new String[0]);
    }

    public static void finish() {
        try {
            scheduler.shutdown();
            System.out.println("TASK SCHEDULER STOPPED F0R " + ThreatsDetector.class.toString());
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("-----------START EXECUTION--------------");
        try {
            double ExpirationTime = VariablesMongoDB.GetInstance().GetVariableByName("IpBlockingExpirationTime", Double.class);

            DetectionParamCollection params = DetectionMongoDB.GetInstance().GetAllParams();
            ArrayList<String> IPs = MetricsMongoDB.GetInstance().GetAllIPs();
            for (String ip : IPs) {
                LogMeasurement measurement = MetricsMongoDB.GetInstance().GetMeasurementByIP(ip);
                IpBlocking blockingInfo = IpBlockingMongoDB.GetInstance().GetBlockingByIP(ip);
                if (!blockingInfo.blocked) {
                    processThreat(measurement, params, blockingInfo, ExpirationTime);
                } else {
                    clearThreatBehaviour(blockingInfo, measurement);
                }
            }

        } catch (Exception e) {
            System.out.println("------------EXECUTION ERROR---------------");
            System.out.println(e.getMessage());
            System.out.println("------------END EXECUTION---------------");
            throw new RuntimeException(e);
        }

        System.out.println("------------END EXECUTION---------------");
    }

    private void processThreat(LogMeasurement measurement, DetectionParamCollection params, IpBlocking blockingInfo, double ExpirationTime) throws IOException {
        DetectionParamCollection failed = threatValidation(measurement, params);
        if (!failed.isEmpty()) {
            System.out.println("ATTACKER DETECTED: " + measurement.ip);
            for (DetectionParam fail : failed) {
                System.out.println("    " + fail.variable);
            }

            blockingInfo.blocked = true;
            blockingInfo.blockingDate = DateUtils.GetNowTime().getTime();
            blockingInfo.expirationTime = ExpirationTime;
            if (IpBlockingMongoDB.GetInstance().UpdateBlocking(blockingInfo)) {
                System.out.println("IP BLOCKED");
                if (Nginx.GetInstance().BlockIpAtProxy(blockingInfo.ip, ExpirationTime))
                    System.out.println("IP BLOCKED AT PROXY");
                else {
                    System.out.println("ERROR BLOCKING IP AT PROXY");
                    blockingInfo.blocked = false;
                    blockingInfo.blockingDate = -1L;
                    IpBlockingMongoDB.GetInstance().UpdateBlocking(blockingInfo);
                }
            } else
                System.out.println("ERROR BLOCKING IP");
        }
    }

    private DetectionParamCollection threatValidation(LogMeasurement measurement, DetectionParamCollection params) {
        DetectionParamCollection failed = new DetectionParamCollection();
        for (DetectionParam param : params) {
            double value = Double.parseDouble(param.value);
            switch (param.variable) {
                case "CommonMaxAttemptsInAMinute":
                    if (measurement.totalAttempts > value)
                        failed.add(param);
                    break;
                case "CommonMaxErrorsInAMinute":
                    if (measurement.errors > value)
                        failed.add(param);
                    break;
                case "CommonMaxTargetedAccounts":
                    if (measurement.targetAccountsCount > value)
                        failed.add(param);
                    break;
                case "CommonMaxErrorsSuccessions":
                    if (measurement.consecutiveErrors.size() > value)
                        failed.add(param);
                    break;
                case "CommonMaxConsecutiveErrors":
                    Optional<Integer> opt = measurement.consecutiveErrors.values().stream().max(Integer::compareTo);
                    if (opt.isPresent()) {
                        int maxConsecutive = opt.get();
                        if (maxConsecutive > value)
                            failed.add(param);
                    }
                    break;
                case "CommonMinAverageAttemptsInterval":
                    LinkedHashMap<Long, Double> intervals = measurement.GetAttemptsTimeIntervals();
                    if (!intervals.isEmpty()) {
                        double average = intervals.values().stream().mapToDouble(d -> d).average().orElse(1000);
                        if (average < value)
                            failed.add(param);
                    }
                    break;
            }
        }

        return failed;
    }

    private void clearThreatBehaviour(IpBlocking blockingInfo, LogMeasurement measurement) throws IOException {
        double elapsedTimeSec = (DateUtils.GetNowTime().getTime() - blockingInfo.blockingDate) * 0.001;
        if (elapsedTimeSec > blockingInfo.expirationTime) {
            blockingInfo.blocked = false;
            blockingInfo.blockingDate = -1L;
            blockingInfo.expirationTime = 0;

            if (!IpBlockingMongoDB.GetInstance().UpdateBlocking(blockingInfo)) {
                System.out.println("ERROR CLEARING LOG BLOCK");
                return;
            }

            measurement.clear();
            if (!MetricsMongoDB.GetInstance().UpdateMeasurement(measurement)) {
                System.out.println("ERROR CLEARING LOG MEASUREMENT");
                return;
            }

            System.out.println("LOG UPDATED");
        }
    }

    public static void main(String[] args) {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();

            scheduler.start();
            System.out.println("TASK SCHEDULER STARTED F0R " + ThreatsDetector.class.toString());

            JobDetail job = JobBuilder.newJob(ThreatsDetector.class)
                    .withIdentity("job2", "group2")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger2", "group2")
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
