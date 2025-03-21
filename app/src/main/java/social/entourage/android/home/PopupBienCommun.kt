package social.entourage.android.home

import social.entourage.android.databinding.PopupNuitbiencommunBinding


import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import social.entourage.android.R

class PopupBienCommun : DialogFragment() {

    interface BienCommunConfirmationListener {
        fun onConfirmParticipation()
    }

    var listener: BienCommunConfirmationListener? = null
    private var _binding: PopupNuitbiencommunBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = PopupNuitbiencommunBinding.inflate(layoutInflater)


        binding.validateBtn.setOnClickListener {
            listener?.onConfirmParticipation()
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


        fun newInstance(): PopupBienCommun {

            return PopupBienCommun().apply {
            }
        }
    }
}
