package social.entourage.android.tools.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import social.entourage.android.R
import timber.log.Timber

/**
 * Created by Jr (MJ-DEVS) on 15/05/2020.
 */
class CustomProgressDialog(val context: Context) {

    private var alertDialog: AlertDialog? = null

    private fun Context.findActivity(): Activity? {
        var ctx = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    fun show(resId: Int?) {
        val activity = context.findActivity()
        if (activity?.isFinishing == true || activity?.isDestroyed == true) return

        if (alertDialog == null) {
            alertDialog = AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setView(R.layout.layout_custom_progress_dialog)
                    .create()
        }

        alertDialog?.let { alertDialog ->

            if (alertDialog.isShowing) alertDialog.dismiss()

            resId?.let { resId ->
                alertDialog.findViewById<TextView>(R.id.ui_progressdialog_tv_title)?.text = context.getText(resId)
            }

            alertDialog.show()
            alertDialog.window?.let { window ->
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(window.attributes)
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                window.attributes = layoutParams
            }
        }
    }

    fun dismiss() {
        alertDialog?.let {
            if (it.isShowing) {
                try {
                    it.dismiss()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
        alertDialog = null
    }
}
