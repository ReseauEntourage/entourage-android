package social.entourage.android.tools;

import android.content.Context;
import android.util.Patterns;

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
        return checkPhoneNumberFormat(null, phoneNumber);
    }

    public static String checkPhoneNumberFormat(String countryCode, String phoneNumber) {

        if (phoneNumber.startsWith("0")) {
            phoneNumber = phoneNumber.substring(1);
            if (countryCode != null) {
                phoneNumber = countryCode + phoneNumber;
            } else {
                phoneNumber = "+33" + phoneNumber;
            }
        } else {
            if (countryCode != null) {
                phoneNumber = countryCode + phoneNumber;
            }
        }

        if (!phoneNumber.startsWith("+")) {
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
        // check for today
        if (now.getYear() == date.getYear() && now.getMonth() == date.getMonth() && now.getDate() == date.getDate()) {
            return context.getString(R.string.date_today);
        }
        // check for yesterday
        long sinceMidnight = now.getSeconds() * 1000 + now.getMinutes() * 60 * 1000 + now.getHours() * 60 * 60 * 1000;
        long oneDay = 86400000L; // 24 hours in millis
        if ( (now.getTime() - date.getTime()) < (oneDay + sinceMidnight) ) {
            return context.getString(R.string.date_yesterday);
        }
        // regular date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String month = "";
        switch (calendar.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                month = context.getString(R.string.date_month_1);
                break;
            case Calendar.FEBRUARY:
                month = context.getString(R.string.date_month_2);
                break;
            case Calendar.MARCH:
                month = context.getString(R.string.date_month_3);
                break;
            case Calendar.APRIL:
                month = context.getString(R.string.date_month_4);
                break;
            case Calendar.MAY:
                month = context.getString(R.string.date_month_5);
                break;
            case Calendar.JUNE:
                month = context.getString(R.string.date_month_6);
                break;
            case Calendar.JULY:
                month = context.getString(R.string.date_month_7);
                break;
            case Calendar.AUGUST:
                month = context.getString(R.string.date_month_8);
                break;
            case Calendar.SEPTEMBER:
                month = context.getString(R.string.date_month_9);
                break;
            case Calendar.OCTOBER:
                month = context.getString(R.string.date_month_10);
                break;
            case Calendar.NOVEMBER:
                month = context.getString(R.string.date_month_11);
                break;
            case Calendar.DECEMBER:
                month = context.getString(R.string.date_month_12);
                break;
        }
        return context.getString(R.string.date_format, calendar.get(Calendar.DAY_OF_MONTH), month, calendar.get(Calendar.YEAR));
    }
}
