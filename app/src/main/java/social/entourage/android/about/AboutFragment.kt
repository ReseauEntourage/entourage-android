package social.entourage.android.about

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
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.view.EntourageSnackbar
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.layout_view_title.*

abstract class AboutFragment : EntourageDialogFragment() {
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

    protected open fun populate() {
        about_logo?.setOnLongClickListener { view: View -> handleLongPress(view) }
        title_close_button?.setOnClickListener { dismiss() }
        about_conditions_layout?.setOnClickListener { onTermsClicked() }
        about_oss_licenses?.setOnClickListener { onOSSLicensesClicked() }
        about_privacy_layout?.setOnClickListener { onPrivacyClicked() }
    }

    private fun handleLongPress(view: View): Boolean {
        if (EntourageApplication.get().clearFeedStorage()) {
            EntourageSnackbar.make(view, R.string.about_clearing_entourage_cache, Snackbar.LENGTH_SHORT).show()
        }
        return true
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onTermsClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_CGU)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            about_coordinator_layout?.let{EntourageSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    private fun onOSSLicensesClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_OSS)
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.about_oss_licenses))
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }

    private fun onPrivacyClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ABOUT_PRIVACY)
        (activity as? MainActivity)?.showWebViewForLinkId(Constants.PRIVACY_LINK_ID)
    }
}