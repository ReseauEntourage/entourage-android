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

        val title = arguments?.getString(ARG_TITLE) ?: "Titre non fourni"
        val description = arguments?.getString(ARG_DESCRIPTION) ?: "Description non fournie"

        // Ici, tu peux utiliser `title` et `description` pour peupler tes TextViews
        binding.tvTitleEventConfirm.text = title
        binding.tvDescEventConfirm.text = description

        binding.validateBtn.setOnClickListener {
            listener?.onConfirmParticipation()
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            listener?.onDeclineParticipation()
            dismiss()
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireActivity(), R.style.RoundedDialog)
            .setView(binding.root)
            .create()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"

        fun newInstance(title: String, description: String): EventConfirmationDialogFragment {
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
            }
            return EventConfirmationDialogFragment().apply {
                arguments = args
            }
        }
    }
}
