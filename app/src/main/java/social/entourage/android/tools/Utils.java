package social.entourage.android.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Patterns;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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

    public static String formatLastUpdateDate(Date lastUpdateDate, Context context) {
        if (lastUpdateDate == null) return "";
        Calendar lastUpdate = Calendar.getInstance();
        lastUpdate.setTime(lastUpdateDate);

        Calendar now = Calendar.getInstance();
        // for today, return the time part
        if (now.get(Calendar.YEAR) ==lastUpdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == lastUpdate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_MONTH) == lastUpdate.get(Calendar.DAY_OF_MONTH)) {
            return DateFormat.format(context.getString(R.string.date_format_today_time), lastUpdate.getTime()).toString();
        }
        // check for yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        if (yesterday.get(Calendar.YEAR) ==lastUpdate.get(Calendar.YEAR)
                && yesterday.get(Calendar.MONTH) == lastUpdate.get(Calendar.MONTH)
                && yesterday.get(Calendar.DAY_OF_MONTH) == lastUpdate.get(Calendar.DAY_OF_MONTH)) {
            return context.getString(R.string.date_yesterday);
        }
        // other date
        String month = getMonthAsString(lastUpdate.get(Calendar.MONTH), context);
        return context.getString(R.string.date_format_short, lastUpdate.get(Calendar.DAY_OF_MONTH), month);
    }

    public static String dateAsStringFromNow(Date date, Context context) {
        if (date == null) return "";
        Calendar lastUpdate = Calendar.getInstance();
        lastUpdate.setTime(date);

        Calendar now = Calendar.getInstance();
        // check for today
        if (now.get(Calendar.YEAR) ==lastUpdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == lastUpdate.get(Calendar.MONTH)
                && now.get(Calendar.DAY_OF_MONTH) == lastUpdate.get(Calendar.DAY_OF_MONTH)) {
            return context.getString(R.string.date_today).toUpperCase();
        }

        // check for yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        if (yesterday.get(Calendar.YEAR) ==lastUpdate.get(Calendar.YEAR)
                && yesterday.get(Calendar.MONTH) == lastUpdate.get(Calendar.MONTH)
                && yesterday.get(Calendar.DAY_OF_MONTH) == lastUpdate.get(Calendar.DAY_OF_MONTH)) {
            return context.getString(R.string.date_yesterday).toUpperCase();
        }
        // regular date
        String month = getMonthAsString(lastUpdate.get(Calendar.MONTH), context);
        return context.getString(R.string.date_format, lastUpdate.get(Calendar.DAY_OF_MONTH), month, lastUpdate.get(Calendar.YEAR)).toUpperCase();
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

    public static Spanned fromHtml(String html){
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
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

        //make sure dimensions are > 0 pixel
        int newW = Math.max((int) (width / scale), 1);
        int newH = Math.max((int) (height/ scale), 1);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(markerBitmap, newW, newH, false);
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        return bitmapDescriptor;
    }
}
