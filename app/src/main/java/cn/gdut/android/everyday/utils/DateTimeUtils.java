package cn.gdut.android.everyday.utils;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by denghewen on 19-07-2016.
 */
public class DateTimeUtils {

    public static String parseDateTime(String dateString, String originalFormat, String outputFromat){

        SimpleDateFormat formatter = new SimpleDateFormat(originalFormat, Locale.CHINA);
        Date date = null;
        try {
            date = formatter.parse(dateString);
            if(isNow(date)){
                return "今天";
            }
            SimpleDateFormat dateFormat=new SimpleDateFormat(outputFromat, new Locale("US"));

            return dateFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getRelativeTimeSpan(String dateString, String originalFormat){

        SimpleDateFormat formatter = new SimpleDateFormat(originalFormat, Locale.CHINA);
        Date date = null;
        try {
            date = formatter.parse(dateString);

            return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static boolean isNow(Date date) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        //获取今天的日期
        String nowDay = sf.format(now);

        //对比的时间
        String day = sf.format(date);

        return day.equals(nowDay);
    }
}
