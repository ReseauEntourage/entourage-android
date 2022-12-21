package social.entourage.android.new_v8.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Html
import android.text.Spanned
import android.text.format.DateFormat
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import social.entourage.android.R
import social.entourage.android.new_v8.events.list.SectionHeader
import social.entourage.android.new_v8.models.EventUiModel
import social.entourage.android.new_v8.models.Events
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max

object Utils {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun hideKeyboard(activity: Activity) {
        val view = activity.findViewById<View>(android.R.id.content)
        if (view != null) {
            val imm: InputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        destinationFilename.deleteOnExit()
        try {
            context.contentResolver.openInputStream(uri)?.use { ins ->
                createFileFromStream(
                    ins,
                    destinationFilename
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return destinationFilename
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    fun getSectionHeaders(
        allEvents: MutableList<Events>?,
        sections: MutableList<SectionHeader>
    ): MutableList<SectionHeader> {
        val map = allEvents?.groupBy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                YearMonth.from(
                    ZonedDateTime.ofInstant(
                        it.metadata?.startsAt?.toInstant(),
                        ZoneId.systemDefault()
                    )
                )
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
        val newSections = map?.map {
            SectionHeader(it.value, it.key.format(DateTimeFormatter.ofPattern("LLLL yyyy")))
        }?.toMutableList()

        return newSections?.let {
            val allSections = sections + newSections
            val sectionsWithoutDuplicates =
                allSections.groupBy { header -> header.sectionText }.map { mapGrouped ->
                    val events = mapGrouped.value.map { it.childList }.flatten()
                    SectionHeader(events, mapGrouped.key)
                }.toMutableList()

            sectionsWithoutDuplicates
        } ?: sections
    }

    fun showAddToCalendarPopUp(context: Context, event: EventUiModel) {
        CustomAlertDialog.show(
            context,
            context.getString(R.string.event_add_to_calendar_title),
            context.getString(R.string.event_add_to_calendar_subtitle),
            context.getString(R.string.add),
        ) {
            val startMillis: Long = Calendar.getInstance().run {
                time = event.metadata?.startsAt ?: time
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                time = event.metadata?.endsAt ?: time
                timeInMillis
            }
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, event.name)
                .putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    event.metadata?.displayAddress
                )
                .putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )
            context.startActivity(intent)
        }
    }

    fun checkUrlWithHttps(url: String): String {
        return if ((url.startsWith(Const.HTTP)).not() && (url.startsWith(Const.HTTPS)).not())
            Const.HTTP + url
        else url
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
        val month = Utils.getMonthAsString(lastUpdate[Calendar.MONTH], context)
        return context.getString(R.string.date_format_short, lastUpdate[Calendar.DAY_OF_MONTH], month)
    }

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
}