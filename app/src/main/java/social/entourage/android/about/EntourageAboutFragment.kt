package social.entourage.android.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_about.*
import social.entourage.android.Constants
import social.entourage.android.EntourageEvents
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.view.EntourageSnackbar

class EntourageAboutFragment : AboutFragment() {
    override fun populate() {
        super.populate()
        about_suggestion_layout?.setOnClickListener { onSuggestionClicked() }
        about_feedback_layout?.setOnClickListener { onFeedbackClicked() }
        faq_website_layout?.setOnClickListener { onFAQClicked() }
        about_tutorial_layout?.setOnClickListener { onTutorialClicked() }
        about_email_layout?.setOnClickListener { onEmailClicked() }
        about_website_layout?.setOnClickListener { onWebsiteClicked() }
    }

    private fun onWebsiteClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_WEBSITE)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            about_coordinator_layout?.let {EntourageSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onEmailClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_EMAIL)
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        if (activity != null && intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            about_coordinator_layout?.let {EntourageSnackbar.make(it, R.string.error_no_email, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onFAQClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_FAQ)
        (activity as? MainActivity)?.showWebViewForLinkId(Constants.FAQ_LINK_ID)
    }

    private fun onTutorialClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_TUTORIAL)
        (activity as? MainActivity)?.showTutorial(true)
    }

    private fun onSuggestionClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_SUGGESTION)
        (activity as? MainActivity)?.showWebViewForLinkId(Constants.SUGGESTION_ID)
    }

    private fun onFeedbackClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_FEEDBACK)
        (activity as? MainActivity)?.showWebViewForLinkId(Constants.FEEDBACK_ID)
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        @JvmField
        val TAG = EntourageAboutFragment::class.java.simpleName
    }
}