package uk.co.createanet.footballformapp.lib;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by matt on 04/08/2014.
 */
public class DateFormatting {

    public static final String FORMAT_LONG = "EE d MMM";
    public static final String FORMAT_LONG_TIME = "d MMM yyyy HH:mm";

    public static String dateToString(Date dateIn, String formatOut) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatOut, Locale.UK);
        return sdf.format(dateIn);
    }

    public static String dateToDisplayString(Date dateIn){
        return dateToString(dateIn, FORMAT_LONG);
    }

    public static String dateToDisplayStringTime(Date dateIn){
        return dateToString(dateIn, FORMAT_LONG_TIME);
    }

    public static Date stringToDate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            Log.d("Constants", "Unable to parse date " + dateString);
        }

        return date;
    }

    public String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

}
