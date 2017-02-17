package data;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by christof on 06.12.16.
 */
public class Date implements Serializable {
    private final String year;
    private final String month;
    private final String day;
    private final String hour;
    private final String minutes;
    private final String seconds;


    public Date(String date) {
        String[] temp1 = date.split(" ");
        String[] temp2 = temp1[0].split("-");
        String[] temp3 = temp1[1].split(":");
        this.year = temp2[0];
        this.month = temp2[1];
        this.day = temp2[2];
        this.hour = temp3[0];
        this.minutes = temp3[1];
        this.seconds = temp3[2];
    }

    public static Date getNextHour(Date date, int amount) {
        Calendar calendar = new GregorianCalendar(Integer.valueOf(date.getYear()), Integer.valueOf(date.getMonth()), Integer.valueOf(date.getDay()), Integer.valueOf(date.getHour()),
                Integer.valueOf(date.getMinutes()), Integer.valueOf(date.getSeconds()));
        calendar.add(Calendar.HOUR_OF_DAY, amount);

        return new Date(getStringValue(calendar.get(calendar.YEAR)) + "-" + getStringValue(calendar.get(calendar.MONTH)) + "-" + getStringValue(calendar.get(calendar.DAY_OF_MONTH)) + " " +
                getStringValue(calendar.get(calendar.HOUR_OF_DAY)) + ":" + getStringValue(calendar.get(calendar.MINUTE)) + ":" + getStringValue(calendar.get(calendar.SECOND)));
    }

    private static String getStringValue(int value) {
        String result = String.valueOf(value);
        if (result.length() < 2) {
            return "0" + result;
        } else {
            return result;
        }
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getHour() {
        return hour;
    }

    public String getMinutes() {
        return minutes;
    }

    public String getSeconds() {
        return seconds;
    }

    public String getShortStringDateForPath() {
        return year + "-" + month + "-" + day + "-" + hour + "h";
    }

    public String getStringDate() {
        return year + "-" + month + "-" + day + " " + hour + ":" + minutes + ":" + seconds;
    }

    public String getStringDateForPath() {
        return year + "-" + month + "-" + day + "-" + hour + "h" + minutes + "m" + seconds + "s";
    }

    public boolean lessThan(Date other) {
        return Long.valueOf(year + month + day + hour + minutes + seconds) < Long.valueOf(other.getYear()
                + other.getMonth() + other.getDay() + other.getHour() + other.getMinutes() + other.getSeconds());
    }

    public long diff(Date other) {
        return (((((((Long.valueOf(day) - Long.valueOf(other.getDay())) * 24) + Long.valueOf(hour) - Long.valueOf(other.getHour())) * 60) + Long.valueOf(minutes) - Long.valueOf(other.getMinutes())) * 60)
                + Long.valueOf(seconds) - Long.valueOf(other.getSeconds())) * 1000L;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Date.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Date other = (Date) obj;
        return this.getStringDate().equals(other.getStringDate());
    }

    public int hashCode() {
        return (getStringDate().hashCode());
    }

}
