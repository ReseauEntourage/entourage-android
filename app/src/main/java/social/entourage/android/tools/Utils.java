package social.entourage.android.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Calendar;
import java.util.Date;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;

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
        } else if (!phoneNumber.startsWith("+")) {
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
            return context.getString(R.string.date_today).toUpperCase();
        }
        // check for yesterday
        long sinceMidnight = now.getSeconds() * 1000 + now.getMinutes() * 60 * 1000 + now.getHours() * 60 * 60 * 1000;
        long oneDay = 86400000L; // 24 hours in millis
        if ( (now.getTime() - date.getTime()) < (oneDay + sinceMidnight) ) {
            return context.getString(R.string.date_yesterday).toUpperCase();
        }
        // regular date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String month = getMonthAsString(calendar.get(Calendar.MONTH), context);

        return context.getString(R.string.date_format, calendar.get(Calendar.DAY_OF_MONTH), month, calendar.get(Calendar.YEAR)).toUpperCase();
    }

    public static String getMonthAsString(int month, Context context) {
        switch (month) {
            case Calendar.JANUARY:
                return context.getString(R.string.date_month_1);
            case Calendar.FEBRUARY:
                return context.getString(R.string.date_month_2);
            case Calendar.MARCH:
                return context.getString(R.string.date_month_3);
            case Calendar.APRIL:
                return context.getString(R.string.date_month_4);
            case Calendar.MAY:
                return context.getString(R.string.date_month_5);
            case Calendar.JUNE:
                return context.getString(R.string.date_month_6);
            case Calendar.JULY:
                return context.getString(R.string.date_month_7);
            case Calendar.AUGUST:
                return context.getString(R.string.date_month_8);
            case Calendar.SEPTEMBER:
                return context.getString(R.string.date_month_9);
            case Calendar.OCTOBER:
                return context.getString(R.string.date_month_10);
            case Calendar.NOVEMBER:
                return context.getString(R.string.date_month_11);
            case Calendar.DECEMBER:
                return context.getString(R.string.date_month_12);
            default:
                return "";
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            result = Html.fromHtml(html);
        }
        return result;
    }

    /**
     * Creates a {@link BitmapDescriptor} from  a drawable, preserving the original ratio.
     *
     * @param drawable The drawable that should be a {@link BitmapDescriptor}.
     * @param dstWidth Destination width
     * @param dstHeight Destination height
     * @return The created {@link BitmapDescriptor}.
     */
    @NonNull
    public static BitmapDescriptor getBitmapDescriptorFromDrawable(@NonNull Drawable drawable, int dstWidth, int dstHeight) {
        BitmapDescriptor bitmapDescriptor;
        // Usually the pin could be loaded via BitmapDescriptorFactory directly.
        // The target map_pin is a VectorDrawable which is currently not supported
        // within BitmapDescriptors.
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        drawable.setBounds(0, 0, width, height);
        Bitmap markerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(markerBitmap);
        drawable.draw(canvas);
        float scale = Math.max(width / (float) dstWidth, height / (float) dstHeight);
        if (scale <= 0) scale = 1;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(markerBitmap, (int) (width / scale), (int) (height / scale), false);
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        return bitmapDescriptor;
    }
}
