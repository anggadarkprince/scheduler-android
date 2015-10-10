package com.sketchproject.scheduler.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Angga on 10/8/2015.
 */
public class Parser {
    public static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static String formatDate(String date, String format){
        String formattedDate = date;
        DateFormat simpleFormat =  new SimpleDateFormat(format, Locale.ENGLISH);
        Date dateType;
        try {
            dateType = simpleFormat.parse(formattedDate);
            formattedDate = simpleFormat.format(dateType);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }

    public static String getDateOfMonth(String date){
        DateFormat simpleFormat =  new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        Date dateType;
        try {
            dateType = simpleFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateType);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            if(day < 10){
                return "0"+String.valueOf(day);
            }
            return String.valueOf(day);
        } catch (ParseException e) {
            Log.e("DATE", e.getMessage());
            return null;
        }
    }

    public static int getMonthOfYear(String date){
        DateFormat simpleFormat =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date dateType;
        try {
            dateType = simpleFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateType);
            return calendar.get(Calendar.MONTH);
        } catch (ParseException e) {
            Log.e("DATE", e.getMessage());
            return 0;
        }
    }

    public static String getShortMonth(int month){
        switch (month){
            case 0:
                return "JAN";
            case 1:
                return "FEB";
            case 2:
                return "MAR";
            case 3:
                return "APR";
            case 4:
                return "MAY";
            case 5:
                return "JUN";
            case 6:
                return "JUL";
            case 7:
                return "AUG";
            case 8:
                return "SEP";
            case 9:
                return "OCT";
            case 10:
                return "NOV";
            case 11:
                return "DEC";
            default:
                return "JAN";
        }
    }

    public static String getFullDay(String date){
        DateFormat simpleFormat =  new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        Date dateType;
        try {
            dateType = simpleFormat.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateType);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            switch (day) {
                case 1:
                    return "SUNDAY";
                case 2:
                    return "MONDAY";
                case 3:
                    return "TUESDAY";
                case 4:
                    return "WEDNESDAY";
                case 5:
                    return "THURSDAY";
                case 6:
                    return "FRIDAY";
                case 7:
                    return "SATURDAY";
                default:
                    return "SUNDAY";
            }
        } catch (ParseException e) {
            Log.e("DATE", e.getMessage());
            return null;
        }
    }

}
