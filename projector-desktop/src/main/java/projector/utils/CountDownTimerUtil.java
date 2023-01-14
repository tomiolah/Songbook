package projector.utils;

import java.util.Date;

public class CountDownTimerUtil {

    public static String getTimeTextFromDate(Long milliseconds) {
        if (milliseconds == null) {
            return "";
        }
        if (milliseconds < 0) {
            milliseconds = 0L;
        }
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hour = minutes / 60;
        minutes = minutes % 60;
        String s = "";
        if (hour > 0) {
            s = hour + ":";
        }
        if (!s.isEmpty() && minutes < 10) {
            s += "0";
        }
        s += minutes + ":";
        if (seconds < 10) {
            s += 0;
        }
        s += seconds;
        return s;
    }

    public static Long getRemainedTime(Date finishDate) {
        Date now = new Date();
        if (finishDate == null) {
            return null;
        }
        return finishDate.getTime() - now.getTime();
    }
}
