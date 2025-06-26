package rmm.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import kelp.Utils.KeycloakEventTypes;
import org.jongo.marshall.jackson.oid.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

public class LogMeasurement {

    @JsonProperty("_id")
    @ObjectId
    public String _id;

    @JsonProperty("ip")
    public String ip;

    // region Errors detection
    @JsonProperty("errors")
    public int errors;
    @JsonProperty("consecutiveErrors")
    public LinkedHashMap<Long, Integer> consecutiveErrors;    // store start timestamp of the consecutive count.
    // endregion

    // region Login Rate
    @JsonProperty("totalAttempts")
    public int totalAttempts;                  // OKs & ERRs -> overall time -> reset when monitor reset
    // endregion

    // region Attempts
    @JsonProperty("attemptsTimeIntervals")
    public LinkedHashMap<Long, Double> attemptsTimeIntervals;    // interval = t1-t2; Store timestamps (t1) related to interval for clean-up
    // endregion

    // region Account targeting
    @JsonProperty("targetAccounts")
    public ArrayList<String> targetAccounts;
    @JsonProperty("targetAccountsCount")
    public int targetAccountsCount;
    // endregion

    public LogMeasurement() {
    }

    public LogMeasurement(String ip) {
        this.ip = ip;
        this.errors = 0;
        this.consecutiveErrors = new LinkedHashMap<>();
        this.totalAttempts = 0;
        this.attemptsTimeIntervals = new LinkedHashMap<>();
        this.targetAccounts = new ArrayList<>();
        this.targetAccountsCount = 0;
    }

    public void clear() {
        this.errors = 0;
        this.consecutiveErrors = new LinkedHashMap<>();
        this.totalAttempts = 0;
        this.attemptsTimeIntervals = new LinkedHashMap<>();
        this.targetAccounts = new ArrayList<>();
        this.targetAccountsCount = 0;
    }

    public LinkedHashMap<Long, Double> GetAttemptsTimeIntervals() {
        long lastTimestamp = this.attemptsTimeIntervals.keySet().stream().reduce((first, second) -> second).orElse(0L);
        LinkedHashMap<Long, Double> copy = (LinkedHashMap<Long, Double>) this.attemptsTimeIntervals.clone();
        copy.remove(lastTimestamp);
        return copy;
    }

    public void UpdateErrorsMeasurements(ArrayList<CustomLog> ipLogs) {
        ArrayList<CustomLog> errorsList = ipLogs.stream()
                .filter(log -> log.getType().equals(KeycloakEventTypes.LOGIN_ERR))
                .collect(Collectors.toCollection(ArrayList::new));
        this.errors = errorsList.size();

        this.consecutiveErrors = GetConsecutiveErrorCounts(ipLogs);
    }

    private LinkedHashMap<Long, Integer> GetConsecutiveErrorCounts(ArrayList<CustomLog> ipLogs) {
        LinkedHashMap<Long, Integer> counts = new LinkedHashMap<>();

        if (!ipLogs.isEmpty()) {
            boolean newSeq = true;
            long currentSeqTime = -1;
            int currentCount = 0;
            for (CustomLog log : ipLogs) {
                if (log.getType().equals(KeycloakEventTypes.LOGIN_OK)) {
                    if (currentCount > 0 && currentSeqTime > 0)
                        counts.put(currentSeqTime, currentCount);
                    newSeq = true;
                    currentSeqTime = -1;
                    currentCount = 0;
                    continue;
                }

                if (newSeq) {
                    newSeq = false;
                    currentSeqTime = log.date.getTime();
                }

                currentCount++;
            }

            if (!newSeq) {
                if (currentCount > 0 && currentSeqTime > 0)
                    counts.put(currentSeqTime, currentCount);
            }
        }
        return counts;
    }

    public void UpdateAttemptsMeasurement(ArrayList<CustomLog> ipLogs) {
        this.totalAttempts = ipLogs.size();
    }

    public void UpdateAttemptsIntervals(ArrayList<CustomLog> ipLogs) {
        this.attemptsTimeIntervals = new LinkedHashMap<>();

        for (CustomLog log : ipLogs) {
            long currentTimestamp = Long.parseLong(log.getDateStrLong());
            if (!this.attemptsTimeIntervals.isEmpty()) {
                long lastTimestamp = this.attemptsTimeIntervals.keySet().stream().reduce((first, second) -> second).orElse(0L);
                double diff = (double) (currentTimestamp - lastTimestamp);
                this.attemptsTimeIntervals.replace(lastTimestamp, diff);
            }
            this.attemptsTimeIntervals.put(currentTimestamp, 0.0);
        }
    }

    public void UpdateAccountUsage(ArrayList<CustomLog> ipLogs) {
        this.targetAccounts = ipLogs.stream()
                .map(CustomLog::getUsername)
                .distinct()
                .filter(account -> !this.targetAccounts.contains(account))
                .collect(Collectors.toCollection(ArrayList::new));
        this.targetAccountsCount = this.targetAccounts.size();
    }
}
