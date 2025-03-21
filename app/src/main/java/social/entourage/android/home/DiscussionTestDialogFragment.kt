package social.entourage.android.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.DialogDiscussionMainTestBinding
import social.entourage.android.tools.log.AnalyticsEvents


class DiscussionTestDialogFragment : DialogFragment() {

    private var _binding: DialogDiscussionMainTestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogDiscussionMainTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.discussion_plural_view)

        binding.closeButton.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.discussion_plural_deny)
            handleUserResponse(false)
            dismiss()
        }
        binding.btnNotInterested.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.discussion_plural_deny)
            handleUserResponse(false)
            dismiss()
        }

        binding.validateBtn.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.discussion_plural_accept)
            handleUserResponse(true)
            Toast.makeText(context, getString(R.string.toast_message_participate), Toast.LENGTH_LONG).show()
            dismiss()
        }
    }

    private fun handleUserResponse(isInterested: Boolean) {
        val sharedPreferences = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putBoolean("DISCUSSION_INTERESTED", isInterested)?.apply()
        if (!isInterested) {
            // Enregistrer un marqueur pour bloquer définitivement la popup si l'utilisateur refuse
            sharedPreferences?.edit()?.putBoolean("USER_REFUSED_POPUP", true)?.apply()
        }
    }



    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
