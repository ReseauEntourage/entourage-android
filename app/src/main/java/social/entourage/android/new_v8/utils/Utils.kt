package social.entourage.android.new_v8.utils

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import social.entourage.android.R


class Utils {
    companion object {
        fun showAlertDialogButtonClicked(
            view: View?,
            title: String,
            content: String,
            action: String,
            onYes: () -> (Unit)
        ) {
            val layoutInflater = LayoutInflater.from(view?.context)
            val customDialog: View = layoutInflater.inflate(R.layout.new_custom_alert_dialog, null)
            val builder = AlertDialog.Builder(view?.context)
            builder.setView(customDialog)
            val alertDialog = builder.create()
            customDialog.findViewById<TextView>(R.id.title).text = title
            customDialog.findViewById<TextView>(R.id.content).text = content
            customDialog.findViewById<TextView>(R.id.yes).text = action
            customDialog.findViewById<Button>(R.id.yes).setOnClickListener { onYes() }
            customDialog.findViewById<Button>(R.id.button)
                .setOnClickListener { alertDialog.dismiss(); }
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.show()
        }
    }
}