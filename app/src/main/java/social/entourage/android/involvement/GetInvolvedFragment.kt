package social.entourage.android.involvement

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_get_involved.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.BuildConfig
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment

/**
 * Get Involved Menu Fragment
 */
class GetInvolvedFragment  : BaseDialogFragment() {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_get_involved, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populate()
        title_close_button?.setOnClickListener { onCloseButton() }
        get_involved_rate_us_layout?.setOnClickListener { onRateUsClicked() }
        get_involved_facebook_layout?.setOnClickListener { onFacebookClicked() }
        get_involved_instagram_layout?.setOnClickListener { onInstaClicked() }
        get_involved_twitter_layout?.setOnClickListener { onTwitterClicked() }
    }

    private fun populate() {
        get_involved_version?.text = getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME)
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    fun onCloseButton() {
        dismiss()
    }

    fun onRateUsClicked() {
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

    fun onFacebookClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_FACEBOOK)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun onInstaClicked() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

    fun onTwitterClicked() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.twitter_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = GetInvolvedFragment::class.java.simpleName
        fun newInstance(): GetInvolvedFragment {
            return GetInvolvedFragment()
        }
    }
}