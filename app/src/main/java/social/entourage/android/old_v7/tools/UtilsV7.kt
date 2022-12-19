package social.entourage.android.old_v7.tools

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.WindowManager
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import social.entourage.android.R
import social.entourage.android.new_v8.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Created by mihaiionescu on 27/07/16.
 */
object UtilsV7 {
    fun dateAsStringFromNow(date: Date, context: Context): String {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.time = date
        val now = Calendar.getInstance()
        // check for today
        if (now[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && now[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && now[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return context.getString(R.string.date_today).uppercase(Locale.getDefault())
        }

        // check for yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        if (yesterday[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && yesterday[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && yesterday[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return context.getString(R.string.date_yesterday).uppercase(Locale.getDefault())
        }
        // regular date
        val month = getMonthAsString(lastUpdate[Calendar.MONTH], context)
        return context.getString(R.string.date_format, lastUpdate[Calendar.DAY_OF_MONTH], month, lastUpdate[Calendar.YEAR])
            .uppercase(Locale.getDefault())
    }

    fun getMonthAsString(month: Int, context: Context): String {
        return when (month) {
            Calendar.JANUARY -> context.getString(R.string.date_month_1)
            Calendar.FEBRUARY -> context.getString(R.string.date_month_2)
            Calendar.MARCH -> context.getString(R.string.date_month_3)
            Calendar.APRIL -> context.getString(R.string.date_month_4)
            Calendar.MAY -> context.getString(R.string.date_month_5)
            Calendar.JUNE -> context.getString(R.string.date_month_6)
            Calendar.JULY -> context.getString(R.string.date_month_7)
            Calendar.AUGUST -> context.getString(R.string.date_month_8)
            Calendar.SEPTEMBER -> context.getString(R.string.date_month_9)
            Calendar.OCTOBER -> context.getString(R.string.date_month_10)
            Calendar.NOVEMBER -> context.getString(R.string.date_month_11)
            Calendar.DECEMBER -> context.getString(R.string.date_month_12)
            else -> ""
        }
    }

    fun getDateStringFromSeconds(milliseconds: Long): String {
        return DateUtils.formatElapsedTime(milliseconds / 1000)
    }

    fun getScreenWidth(context: Context): Int {
        val size = Point()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(size)
        return size.x
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun formatTextWithBoldSpanAndColor(color:Int, isBold:Boolean, text: String, vararg textToBold: String): SpannableStringBuilder {

        val builder = SpannableStringBuilder(text)

        for (textItem in textToBold) {
            if (textItem.isNotEmpty() && textItem.trim { it <= ' ' } != "") {
                //for counting start/end indexes
                val _text = text.lowercase(Locale.getDefault())
                val _textToBold = textItem.lowercase(Locale.getDefault())
                val startingIndex = _text.indexOf(_textToBold)
                val endingIndex = startingIndex + _textToBold.length

                if (startingIndex >= 0 && endingIndex >= 0) {
                    builder.setSpan(ForegroundColorSpan(color),startingIndex,endingIndex,0)
                    if (isBold) builder.setSpan(StyleSpan(Typeface.BOLD), startingIndex, endingIndex, 0)
                }
            }
        }
        return builder
    }
}