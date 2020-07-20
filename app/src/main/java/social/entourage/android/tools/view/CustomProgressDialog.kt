package social.entourage.android.view

import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import social.entourage.android.R


/**
 * Created by Jr (MJ-DEVS) on 15/05/2020.
 */
class CustomProgressDialog(val context:Context) {

    private var alertDialog: AlertDialog? = null

    fun show(resId: Int?) {
        if (alertDialog == null) {
            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)

            builder.setView(R.layout.layout_custom_progress_dialog)
            alertDialog = builder.create()
        }

        if (alertDialog!!.isShowing) alertDialog?.dismiss()

        val text = alertDialog?.findViewById<TextView>(R.id.ui_progressdialog_tv_title)
        resId?.let {text?.text = context.getText(resId)  }

        alertDialog?.show()
        val window: Window? = alertDialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(alertDialog?.window!!.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            alertDialog?.window!!.attributes = layoutParams
        }
    }

    fun dismiss() {
        alertDialog?.dismiss()
        alertDialog = null
    }
}