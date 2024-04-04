package social.entourage.android.homev2

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import social.entourage.android.R
import social.entourage.android.databinding.DialogEventConfirmationBinding

class EventConfirmationDialogFragment : DialogFragment() {

    interface EventConfirmationListener {
        fun onConfirmParticipation()
        fun onDeclineParticipation()
    }

    var listener: EventConfirmationListener? = null
    private var _binding: DialogEventConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEventConfirmationBinding.inflate(layoutInflater)

        binding.validateBtn.setOnClickListener {
            listener?.onConfirmParticipation()
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            listener?.onDeclineParticipation()
            dismiss()
        }

        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
