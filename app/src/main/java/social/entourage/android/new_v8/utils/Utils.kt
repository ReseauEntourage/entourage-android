package social.entourage.android.new_v8.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.CalendarContract
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
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
        fun showAlertDialogButtonClicked(
            context: Context,
            title: String,
            content: String,
            action: String,
            onNo: () -> (Unit) = {},
            onYes: (() -> Unit)?,
        ) {
            val layoutInflater = LayoutInflater.from(context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            customDialog.findViewById<TextView>(R.id.title).text = title
            customDialog.findViewById<TextView>(R.id.content).text = content
            if (onYes != null) {
                customDialog.findViewById<TextView>(R.id.yes).text = action
                customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
                    onYes()
                    alertDialog.dismiss()
                }
            } else {
                customDialog.findViewById<Button>(R.id.yes).visibility = View.GONE
                customDialog.findViewById<TextView>(R.id.no).text =
                    context.getString(R.string.button_OK)
            }
            customDialog.findViewById<Button>(R.id.no).setOnClickListener {
                onNo()
                alertDialog.dismiss()
            }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }

        fun showAlertDialogButtonClickedWithCrossClose(
            context: Context,
            title: String,
            content: String,
            buttonNOK: String,
            buttonOk:String,
            showCross:Boolean = true,
            onNo: () -> (Unit) = {},
            onYes: (() -> Unit),
        ) {
            val layoutInflater = LayoutInflater.from(context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            customDialog.findViewById<ImageView>(R.id.ui_pop_close).isVisible = showCross
            customDialog.findViewById<TextView>(R.id.title).text = title
            customDialog.findViewById<TextView>(R.id.content).text = content

            customDialog.findViewById<ImageView>(R.id.ui_pop_close).setOnClickListener {
                alertDialog.dismiss()
            }
            //Action button Left
            customDialog.findViewById<TextView>(R.id.yes).text = buttonNOK
            customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
                onNo()
                alertDialog.dismiss()
            }
            //Action button right
            customDialog.findViewById<TextView>(R.id.no).text = buttonOk
            customDialog.findViewById<Button>(R.id.no).setOnClickListener {
                onYes()
                alertDialog.dismiss()
            }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }

        fun showAlertDialogButtonEditText(
            context: Context,
            title: String,
            content: String,
            subcontent:String,
            placeholder: String,
            buttonOk:String,
            onValidate: ((String) -> Unit),
        ) {
            val layoutInflater = LayoutInflater.from(context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog_input_txt, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            customDialog.findViewById<ImageView>(R.id.ui_pop_close).isVisible = true
            customDialog.findViewById<TextView>(R.id.title).text = title
            customDialog.findViewById<TextView>(R.id.content).text = content
            customDialog.findViewById<TextView>(R.id.subcontent).text = subcontent
            customDialog.findViewById<EditText>(R.id.ui_message).hint = placeholder

            customDialog.findViewById<ImageView>(R.id.ui_pop_close).setOnClickListener {
                alertDialog.dismiss()
            }

            customDialog.findViewById<TextView>(R.id.ui_bt_send).text = buttonOk
            customDialog.findViewById<Button>(R.id.ui_bt_send).setOnClickListener {
                onValidate(customDialog.findViewById<EditText>(R.id.ui_message).text.toString())
                alertDialog.dismiss()
            }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }

        fun showAlertDialogWithoutActions(
            context: Context,
            title: String,
            subtitle: String,
            illustration: Int
        ) {
            val layoutInflater = LayoutInflater.from(context)
            val customDialog: View =
                layoutInflater.inflate(R.layout.new_custom_dialog_no_actions, null)

            with(customDialog) {
                val tvTitle = findViewById<TextView>(R.id.title)
                val tvSubtitle = findViewById<TextView>(R.id.subtitle)
                val ivClose = findViewById<ImageView>(R.id.close)
                val ivIllustration = findViewById<ImageView>(R.id.illustration)
                val builder = AlertDialog.Builder(context)
                builder.setView(customDialog)
                val alertDialog = builder.create()
                tvTitle.text = title
                tvSubtitle.text = subtitle
                ivIllustration.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        illustration,
                        null
                    )
                )
                ivClose.setOnClickListener { alertDialog.dismiss() }
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                alertDialog.show()
            }
        }

        fun showAlertDialogButtonClickedInverse(
            context: Context,
            title: String,
            content: String,
            action: String,
            onYes: () -> (Unit) = {},
        ) {
            val layoutInflater = LayoutInflater.from(context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            customDialog.findViewById<TextView>(R.id.title).text = title
            customDialog.findViewById<TextView>(R.id.content).text = content
            with(customDialog.findViewById<TextView>(R.id.yes)) {
                text = action
                setOnClickListener {
                    alertDialog.dismiss()
                }
            }
            customDialog.findViewById<Button>(R.id.no).setOnClickListener {
                onYes()
                alertDialog.dismiss()
            }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }

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
                val i = ZonedDateTime.ofInstant(
                    it.metadata?.startsAt?.toInstant(),
                    ZoneId.systemDefault()
                )
                YearMonth.from(i)
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
            showAlertDialogButtonClicked(
                context,
                context.getString(R.string.event_add_to_calendar_title),
                context.getString(R.string.event_add_to_calendar_subtitle),
                context.getString(R.string.add),
            ) {
                val startMillis: Long = Calendar.getInstance().run {
                    time = event.metadata?.startsAt
                    timeInMillis
                }
                val endMillis: Long = Calendar.getInstance().run {
                    time = event.metadata?.endsAt
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