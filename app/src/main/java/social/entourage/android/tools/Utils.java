package social.entourage.android.tools;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Patterns;

import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

import social.entourage.android.R;

/**
 * Created by mihaiionescu on 27/07/16.
 */
public class Utils {

    // This class should not be instantiated
    private Utils() {

    }

    public static String checkPhoneNumberFormat(String phoneNumber) {

        if (phoneNumber.startsWith("0")) {
            phoneNumber = "+33" + phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        if(Patterns.PHONE.matcher(phoneNumber).matches())
            return phoneNumber;

        return null;
    }

    public static String checkEmailFormat(String email) {
        if (email != null && !email.equals("")) {
            return email;
        }
        return null;
    }

    public static String dateAsStringFromNow(Date date, Context context) {
        if (date == null) return "";
        Date now = new Date();
        Calendar.getInstance().setTime(date);
        // check for today
        if (now.getYear() == date.getYear() && now.getMonth() == date.getMonth() && now.getDate() == date.getDate()) {
            return context.getString(R.string.date_today);
        }
        // check for yesterday
        long sinceMidnight = now.getSeconds() * 1000 + now.getMinutes() * 60 * 1000 + now.getHours() * 60 * 60 * 1000;
        long oneDay = 24 * 60 * 60 * 1000; // 24 hours in millis
        if ( (now.getTime() - date.getTime()) < (oneDay + sinceMidnight) ) {
            return context.getString(R.string.date_yesterday);
        }
        // regular date
        String month = "";
        switch (date.getMonth()) {
            case 1:
                month = context.getString(R.string.date_month_1);
                break;
            case 2:
                month = context.getString(R.string.date_month_2);
                break;
            case 3:
                month = context.getString(R.string.date_month_3);
                break;
            case 4:
                month = context.getString(R.string.date_month_4);
                break;
            case 5:
                month = context.getString(R.string.date_month_5);
                break;
            case 6:
                month = context.getString(R.string.date_month_6);
                break;
            case 7:
                month = context.getString(R.string.date_month_7);
                break;
            case 8:
                month = context.getString(R.string.date_month_8);
                break;
            case 9:
                month = context.getString(R.string.date_month_9);
                break;
            case 10:
                month = context.getString(R.string.date_month_10);
                break;
            case 11:
                month = context.getString(R.string.date_month_11);
                break;
            case 12:
                month = context.getString(R.string.date_month_12);
                break;
        }
        return context.getString(R.string.date_format, date.getDate(), month, date.getYear()+1900);
    }
}
