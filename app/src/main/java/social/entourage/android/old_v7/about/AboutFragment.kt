package social.entourage.android.old_v7.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.*
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.view.EntSnackbar
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.old_v7.MainActivity_v7
import social.entourage.android.tools.log.AnalyticsEvents

class AboutFragment : BaseDialogFragment() {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populate()
    }

    // ----------------------------------
    // ----------------------------------
    private fun populate() {
        title_close_button?.setOnClickListener { dismiss() }
        about_conditions_layout?.setOnClickListener { onTermsClicked() }
        about_oss_licenses?.setOnClickListener { onOSSLicensesClicked() }
        about_privacy_layout?.setOnClickListener { onPrivacyClicked() }
        about_suggestion_layout?.setOnClickListener { onSuggestionClicked() }
        about_feedback_layout?.setOnClickListener { onFeedbackClicked() }
        faq_website_layout?.setOnClickListener { onFAQClicked() }
        about_tutorial_layout?.setOnClickListener { onTutorialClicked() }
        about_email_layout?.setOnClickListener { onEmailClicked() }
        about_website_layout?.setOnClickListener { onWebsiteClicked() }
        get_involved_rate_us_layout?.setOnClickListener { onRateUsClicked() }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onTermsClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_CGU)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            about_coordinator_layout?.let{ EntSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onOSSLicensesClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_OSS)
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_oss_licenses))
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }

    private fun onPrivacyClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_PRIVACY)
        (activity as? MainActivity_v7)?.showWebViewForLinkId(Constants.PRIVACY_LINK_ID)
    }

    //ENTOURAGE SPECIFIC
    private fun onRateUsClicked() {
        activity?.let {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_RATING)
            val goToMarket = Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.market_url, it.packageName)))
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.playstore_url, it.packageName))))
            }
        }
    }

    private fun onWebsiteClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_WEBSITE)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            about_coordinator_layout?.let { EntSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onEmailClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_EMAIL)
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            about_coordinator_layout?.let { EntSnackbar.make(it, R.string.error_no_email, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onFAQClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_FAQ)
        (activity as? MainActivity_v7)?.showWebViewForLinkId(Constants.FAQ_LINK_ID)
    }

    private fun onTutorialClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_TUTORIAL)
        (activity as? MainActivity_v7)?.showTutorial(true)
    }

    private fun onSuggestionClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_SUGGESTION)
        (activity as? MainActivity_v7)?.showWebViewForLinkId(Constants.SUGGESTION_ID)
    }

    private fun onFeedbackClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_FEEDBACK)
        (activity as? MainActivity_v7)?.showWebViewForLinkId(Constants.FEEDBACK_ID)
    }

    companion object {
        val TAG: String = AboutFragment::class.java.simpleName
    }
}