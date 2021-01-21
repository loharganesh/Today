package app.splitbit.today.Application;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.GJChronology;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeStamp {
    public static long getTimestamp() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        Chronology gregorianJuian = GJChronology.getInstance(zone);
        DateTime dateTime = new DateTime(gregorianJuian);
        long millis = dateTime.getMillis();
        return millis;
    }

    public static String getDate(long milliSeconds) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(milliSeconds);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(d);
    }

}
