package social.entourage.android.tools

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
    fun checkPhoneNumberFormat(phoneNumber: String): String? {
        return checkPhoneNumberFormat(null, phoneNumber)
    }

    fun checkPhoneNumberFormat(countryCode: String?, phoneNumber: String): String? {
        var correctPhoneNumber = phoneNumber
        if (correctPhoneNumber.startsWith("0")) {
            correctPhoneNumber = correctPhoneNumber.substring(1)
            correctPhoneNumber = if (countryCode != null) {
                countryCode + correctPhoneNumber
            } else {
                "+33$correctPhoneNumber"
            }
        } else if (!correctPhoneNumber.startsWith("+")) {
            if (countryCode != null) {
                correctPhoneNumber = countryCode + correctPhoneNumber
            }
        }
        if (!correctPhoneNumber.startsWith("+")) {
            correctPhoneNumber = "+$correctPhoneNumber"
        }
        return if (Patterns.PHONE.matcher(correctPhoneNumber).matches()) correctPhoneNumber else null
    }

    fun formatLastUpdateDate(lastUpdateDate: Date, context: Context): String {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.time = lastUpdateDate
        val now = Calendar.getInstance()
        // for today, return the time part
        if (now[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && now[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && now[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return DateFormat.format(context.getString(R.string.date_format_today_time), lastUpdate.time).toString()
        }
        // check for yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        if (yesterday[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && yesterday[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && yesterday[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return context.getString(R.string.date_yesterday)
        }
        // other date
        val month = getMonthAsString(lastUpdate[Calendar.MONTH], context)
        return context.getString(R.string.date_format_short, lastUpdate[Calendar.DAY_OF_MONTH], month)
    }

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

    fun dateAsStringLitteralFromNow(date: Date, context: Context, format:Int?,caps:Boolean = true): String {
        val lastUpdate = Calendar.getInstance()
        lastUpdate.time = date
        val now = Calendar.getInstance()
        // check for today
        if (now[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && now[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && now[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return if (caps) context.getString(R.string.date_today) else context.getString(R.string.date_today_lower)
        }

        // check for yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        if (yesterday[Calendar.YEAR] == lastUpdate[Calendar.YEAR] && yesterday[Calendar.MONTH] == lastUpdate[Calendar.MONTH] && yesterday[Calendar.DAY_OF_MONTH] == lastUpdate[Calendar.DAY_OF_MONTH]) {
            return if (caps) context.getString(R.string.date_yesterday) else context.getString(R.string.date_yesterday_lower)
        }

        // custom regular date
        val dateStr = SimpleDateFormat(context.getString(R.string.action_date_list_formatter),
            Locale.FRANCE).format(date)
        return if (format != null) context.getString(format,dateStr) else dateStr
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

    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    /**
     * Creates a [BitmapDescriptor] from  a drawable, preserving the original ratio.
     *
     * @param drawable The drawable that should be a [BitmapDescriptor].
     * @param dstWidth Destination width
     * @param dstHeight Destination height
     * @return The created [BitmapDescriptor].
     */
    fun getBitmapDescriptorFromDrawable(drawable: Drawable, dstWidth: Int, dstHeight: Int): BitmapDescriptor {
        val bitmapDescriptor: BitmapDescriptor
        // Usually the pin could be loaded via BitmapDescriptorFactory directly.
        // The target map_pin is a VectorDrawable which is currently not supported
        // within BitmapDescriptors.
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        drawable.setBounds(0, 0, width, height)
        val markerBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(markerBitmap)
        drawable.draw(canvas)
        var scale = max(width / dstWidth.toFloat(), height / dstHeight.toFloat())
        if (scale <= 0) scale = 1f

        //make sure dimensions are > 0 pixel
        val newW = max((width / scale).toInt(), 1)
        val newH = max((height / scale).toInt(), 1)
        val scaledBitmap = Bitmap.createScaledBitmap(markerBitmap, newW, newH, false)
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        return bitmapDescriptor
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

    fun getBitmapFromUri(uri: Uri, contentResolver: ContentResolver): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        else
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }

    fun saveBitmapToFile(bitmap: Bitmap, file: File?): File {
        val photoFile = file ?: createImageFile()

        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return photoFile
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
        val imageFileName = "ENTOURAGE_CROP_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        )
    }
}