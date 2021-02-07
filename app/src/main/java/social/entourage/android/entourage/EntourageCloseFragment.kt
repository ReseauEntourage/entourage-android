package social.entourage.android.entourage

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events.OnFeedItemCloseRequestEvent
import social.entourage.android.tools.EntBus
import kotlinx.android.synthetic.main.fragment_entourage_close.*
import social.entourage.android.api.model.EntourageEvent

/**
 * Offers options when closing an entourage
 * Use the [EntourageCloseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EntourageCloseFragment : DialogFragment() {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    private var feedItem: FeedItem? = null

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedItem = arguments?.getSerializable(FeedItem.KEY_FEEDITEM) as FeedItem?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.window?.let {
            it.requestFeature(Window.FEATURE_NO_TITLE)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        // Inflate the layout for this fragment
        val layoutId = if(feedItem is EntourageEvent) R.layout.fragment_event_close else R.layout.fragment_entourage_close

        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        entourage_close_close_button?.setOnClickListener {onCloseClicked()}
        entourage_close_cancel_button?.setOnClickListener {onCloseClicked()}
        entourage_close_success_button?.setOnClickListener {onSuccessClicked()}
        entourage_close_failed_button?.setOnClickListener {onFailedClicked()}
        entourage_close_help_button?.setOnClickListener {onHelpClicked()}
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.CustomDialogFragmentSlide
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    fun onCloseClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL)
        dismiss()
    }

    private fun onSuccessClicked() {
        feedItem?.let {
            EntBus.post(OnFeedItemCloseRequestEvent(it, showUI = false, success = true))
            showEmail(R.string.entourage_close_email_title_success)
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CLOSE_POPUP_SUCCESS)
            dismiss()
        }
    }

    private fun onFailedClicked() {
        feedItem?.let {
            EntBus.post(OnFeedItemCloseRequestEvent(it, showUI = false, success = false))
            showEmail(R.string.entourage_close_email_title_failed)
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CLOSE_POPUP_FAILURE)
            dismiss()
        }
    }

    private fun onHelpClicked() {
        showEmail(R.string.entourage_close_email_title_help)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_CLOSE_POPUP_HELP)
        dismiss()
    }

    // ----------------------------------
    // INNER METHODS
    // ----------------------------------
    private fun showEmail(@StringRes emailSubjectFormat: Int): Boolean {
        if (feedItem == null) return false
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        // Set the subject
        val title = feedItem?.getTitle() ?: ""
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(emailSubjectFormat, title))
        if (activity != null && intent.resolveActivity(requireActivity().packageManager) != null) {
            // Start the intent
            startActivity(intent)
        } else {
            // No Email clients
            Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        val TAG = EntourageCloseFragment::class.java.simpleName

        fun newInstance(feedItem: FeedItem?): EntourageCloseFragment {
            val fragment = EntourageCloseFragment()
            val args = Bundle()
            args.putSerializable(FeedItem.KEY_FEEDITEM, feedItem)
            fragment.arguments = args
            return fragment
        }
    }
}