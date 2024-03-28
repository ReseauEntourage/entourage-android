package social.entourage.android.homev2

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import social.entourage.android.R

class EventConfirmationDialogFragment : DialogFragment() {

    interface EventConfirmationListener {
        fun onConfirmParticipation()
        fun onDeclineParticipation()
    }

    var listener: EventConfirmationListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.dialog_event_confirmation, null)

/*        view?.findViewById<Button>(R.id.btn_confirm)?.setOnClickListener {
            listener?.onConfirmParticipation()
            dismiss()
        }
        view?.findViewById<Button>(R.id.btn_decline)?.setOnClickListener {
            listener?.onDeclineParticipation()
            dismiss()
        }*/

        builder.setView(view)
        return builder.create()
    }
}
