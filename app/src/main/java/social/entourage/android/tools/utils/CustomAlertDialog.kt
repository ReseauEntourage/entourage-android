package social.entourage.android.tools.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import social.entourage.android.R

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
        val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<TextView>(R.id.yes).text = action
        customDialog.findViewById<Button>(R.id.yes).setOnClickListener {
            onYes()
            alertDialog.dismiss()
        }
        customDialog.findViewById<Button>(R.id.no).setOnClickListener {
            onNo()
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
        customDialog.findViewById<Button>(R.id.yes).visibility = View.GONE
        with(customDialog.findViewById<TextView>(R.id.no)) {
            text = action
            setOnClickListener {
                onAction()
                alertDialog.dismiss()
            }
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
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
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(customDialog)
        val alertDialog = builder.create()
        customDialog.findViewById<TextView>(R.id.title).text = title
        customDialog.findViewById<TextView>(R.id.content).text = content
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


        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

    fun showWithoutActions(
        context: Context,
        title: String,
        subtitle: String,
        illustration: Int
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customDialog: View =
            layoutInflater.inflate(R.layout.new_custom_dialog_no_actions, null)

        with(customDialog) {
            val builder = AlertDialog.Builder(context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            findViewById<TextView>(R.id.title).text = title
            findViewById<TextView>(R.id.subtitle).text = subtitle
            findViewById<ImageView>(R.id.illustration).setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    illustration,
                    null
                )
            )
            findViewById<ImageView>(R.id.close).setOnClickListener { alertDialog.dismiss() }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }
    }
}