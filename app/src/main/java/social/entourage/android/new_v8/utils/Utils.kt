package social.entourage.android.new_v8.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import social.entourage.android.R


class Utils {
    companion object {
        fun showAlertDialogButtonClicked(
            view: View?,
            title: String,
            content: String,
            action: String,
            onNo: () -> (Unit) = {},
            onYes: (() -> Unit)?,
        ) {
            val layoutInflater = LayoutInflater.from(view?.context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
            val builder = AlertDialog.Builder(view?.context)
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
                    view?.context?.getString(R.string.button_OK)
            }
            customDialog.findViewById<Button>(R.id.no).setOnClickListener {
                onNo()
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
    }
}