package social.entourage.android.events

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import social.entourage.android.databinding.DialogAcceptPhotoBinding

class AcceptPhotoDialogFragment : DialogFragment() {

    interface Listener {
        fun onAcceptPhotoForUser(userId: Int)
        fun onDeclinePhotoForUser(userId: Int)
    }

    private var _binding: DialogAcceptPhotoBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(userId: Int): AcceptPhotoDialogFragment {
            val f = AcceptPhotoDialogFragment()
            f.arguments = Bundle().apply { putInt(ARG_USER_ID, userId) }
            return f
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
        _binding = DialogAcceptPhotoBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val userId = requireArguments().getInt(ARG_USER_ID)

        binding.title.setText(social.entourage.android.R.string.photo_right_title)
        binding.body.setText(social.entourage.android.R.string.photo_right_body)
        binding.btnDecline.setOnClickListener {
            (parentFragment as? Listener ?: activity as? Listener)?.onDeclinePhotoForUser(userId)
            dismiss()
        }
        binding.btnAccept.setOnClickListener {
            (parentFragment as? Listener ?: activity as? Listener)?.onAcceptPhotoForUser(userId)
            dismiss()
        }
        return dialog
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
