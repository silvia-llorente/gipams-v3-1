package rmm.Utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {


    public static Date GetDateFromParameter(String log, long alternative) throws NumberFormatException {
        Date logDate;
        if (!log.equals("")) {
            long longDate = Long.parseLong(log);
            logDate = new Date(longDate);
        } else {
            logDate = new Date(alternative);
        }

        return logDate;
    }

    public static Date GetNowTime(){
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }
}
