package social.entourage.android.tools.utils

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents

object CustomAlertDialog {
    fun showWithCancelFirst(
        context: Context,
        title: String,
        content: String,
        action: String,
        onNo: () -> (Unit) = {},
        onYes: (() -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<TextView>(R.id.yes).text = action
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.no).setOnClickListener {
            onNo()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showAmbassadorWithTwoButton(
        context: Context,
        onNo: () -> (Unit) = {},
        onYes: (() -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_organise_as_ambassador, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.button_start).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.button_configure_later).setOnClickListener {
            onNo()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }


    fun showForLastActionOneDemand(
        context: Context,
        title: String,
        titleAction:String,
        content: String,
        action: String,
        onNo: () -> (Unit) = {},
        onYes: (() -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_with_subtitle, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.subtitle).text = titleAction
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<TextView>(R.id.yes).text = action
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.no).setOnClickListener {
            onNo()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }
    fun showForLastActionOneContrib(
        context: Context,
        title: String,
        titleAction:String,
        content: String,
        action: String,
        onNo: () -> (Unit) = {},
        onYes: (() -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_with_subtitle, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.subtitle).visibility = View.GONE
        customDialog.findViewById<TextView>(R.id.yes).text = action
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.no).setOnClickListener {
            onNo()
            alertDialog.dismiss()
        }
        val formattedString = context.getString(R.string.custom_dialog_action_content_one_contrib, titleAction)

        val styledText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(formattedString, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(formattedString)
        }
        customDialog.findViewById<TextView>(R.id.content).text = styledText
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }
    fun showForLastActionTwo(
        context: Context,
        title: String,
        content: String,
        action: String,
        onYes: (() -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_one_button, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<TextView>(R.id.yes).text = action
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }
    fun showForLastActionThree(
        context: Context,
        title: String,
        content: String,

    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_no_button, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<ImageButton>(R.id.btn_cross).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showOnlyOneButton(
        context: Context,
        title: String,
        content: String,
        action: String,
        onAction: () -> (Unit) = {}
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()

        with(customDialog.findViewById<TextView>(R.id.title)){
            text = title
            setTextColor(context.getColor(R.color.light_orange))
        }
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<Button>(R.id.yes).visibility = View.GONE
        with(customDialog.findViewById<TextView>(R.id.no)) {
            text = action
            setOnClickListener {
                onAction()
                alertDialog.dismiss()
            }
        }
        with(customDialog.findViewById<ImageButton>(R.id.btn_cross)) {
            visibility = View.VISIBLE
            setOnClickListener {
                alertDialog.dismiss()
            }
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }
    fun showOnlyOneButtonNoClose(
        context: Context,
        title: String,
        content: String,
        action: String,
        onAction: () -> (Unit) = {}
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()

        with(customDialog.findViewById<TextView>(R.id.title)){
            text = title
            setTextColor(context.getColor(R.color.light_orange))
        }
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<Button>(R.id.yes).visibility = View.GONE
        with(customDialog.findViewById<TextView>(R.id.no)) {
            text = action
            setOnClickListener {
                onAction()
                alertDialog.dismiss()
            }
        }
        with(customDialog.findViewById<ImageButton>(R.id.btn_cross)) {
            visibility = View.GONE
            setOnClickListener {
                //alertDialog.dismiss()
            }
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showWelcomeAlert(
        context: Context,
        title: String,
        content: String,
        action: String,
        onAction: () -> (Unit) = {}
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()

        with(customDialog.findViewById<TextView>(R.id.title)){
            text = title
            setTextColor(context.getColor(R.color.light_orange))
        }
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<Button>(R.id.no).visibility = View.GONE
        with(customDialog.findViewById<TextView>(R.id.yes)) {
            text = action
            setTextColor(context.getColor(R.color.white))

            setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.I_present_click_i_post)
                onAction()
                alertDialog.dismiss()
            }
        }
        with(customDialog.findViewById<ImageButton>(R.id.btn_cross)) {
            visibility = View.VISIBLE
            setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.i_present_close_pop)
                alertDialog.dismiss()
            }
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        AnalyticsEvents.logEvent(AnalyticsEvents.I_present_view_pop)
        alertDialog.show()
    }

    fun show(
        context: Context,
        title: String,
        content: String,
        action: String,
        onYes: () -> (Unit) = {},
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        with(customDialog.findViewById<TextView>(R.id.yes)) {
            text = action
            setOnClickListener {
                onYes()
                alertDialog.dismiss()
            }
        }
        customDialog.findViewById<Button>(R.id.no).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showWithNoDefined(
        context: Context,
        title: String,
        content: String,
        action: String,
        noAction:String,
        onYes: () -> (Unit) = {},
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<ImageView>(R.id.btn_cross).setOnClickListener { alertDialog.dismiss() }
        with(customDialog.findViewById<TextView>(R.id.no)) {
            text = noAction
            setOnClickListener {
                alertDialog.dismiss()
            }
        }
        with(customDialog.findViewById<TextView>(R.id.yes)) {
            text = action
            setOnClickListener {
                onYes()
                alertDialog.dismiss()
            }
        }


        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showButtonClickedWithCrossClose(
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
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog, null)
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
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showButtonEditText(
        context: Context,
        title: String,
        content: String,
        subcontent:String,
        placeholder: String,
        buttonOk:String,
        onValidate: ((String) -> Unit),
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View = layoutInflater.inflate(R.layout.layout_custom_alert_dialog_input_txt, null)
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
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        alertDialog.show()
    }

    fun showWithoutActions(
        context: Context,
        title: String,
        content: String,
        illustration: Int? = null
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View =
            layoutInflater.inflate(R.layout.layout_custom_alert_dialog_no_button, null)

        with(customDialog) {
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            findViewById<TextView>(R.id.title).text = title
            findViewById<TextView>(R.id.content).text = content
            illustration?.let { illustration ->
                val view = findViewById<ImageView>(R.id.iv_bottom)
                view.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        illustration,
                        null
                    )
                )
                view.isVisible = true
            }
            findViewById<ImageView>(R.id.btn_cross).setOnClickListener { alertDialog.dismiss() }
            alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            alertDialog.show()
        }
    }
}