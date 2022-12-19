package social.entourage.android.new_v8.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.OpenableColumns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import social.entourage.android.R
import social.entourage.android.new_v8.events.list.SectionHeader
import social.entourage.android.new_v8.models.EventUiModel
import social.entourage.android.new_v8.models.Events
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Utils {
    companion object {
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
    }
}