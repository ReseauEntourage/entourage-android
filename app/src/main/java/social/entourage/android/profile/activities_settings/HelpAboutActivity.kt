package social.entourage.android.profile.activities_settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.NewFragmentHelpAboutBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar

class HelpAboutActivity : BaseActivity() {

    private lateinit var binding: NewFragmentHelpAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentHelpAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeView()
        populate()
        handleCloseButton()
    }

    private fun initializeView() {
        binding.licence.divider.visibility = View.GONE
    }

    private fun populate() {
        with(binding) {
            cgu.layout.setOnClickListener { onTermsClicked() }
            confidentiality.layout.setOnClickListener { onPrivacyClicked() }
            licence.layout.setOnClickListener { onOSSLicensesClicked() }
            feedback.layout.setOnClickListener { onRateUsClicked() }
            faq.layout.setOnClickListener { onFAQClicked() }
            donation.layout.setOnClickListener { onDonate() }
            ambassadorProgram.layout.setOnClickListener { onAmbassadorClicked() }
            ethic.layout.setOnClickListener { onEthicChartClicked() }
            partner.layout.setOnClickListener { onPartnerClicked() }
            childRules.layout.setOnClickListener { onChildRuleClicked()}
        }
    }

    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onTermsClicked() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            showNoBrowserError()
        }
    }

    private fun onPrivacyClicked() {
        val privacyUrl = getString(R.string.privacy_policy_url)
        openLink(privacyUrl)
    }

    private fun onChildRuleClicked() {
        val privacyUrl = getString(R.string.child_rule_url)
        openLink(privacyUrl)
    }

    private fun onOSSLicensesClicked() {
        startActivity(Intent(this, OssLicensesMenuActivity::class.java))
    }

    private fun onRateUsClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_RATING)
        val goToMarket = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.market_url, packageName))
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        }
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.playstore_url, packageName))
                )
            )
        }
    }

    private fun onDonate() {
        // À l'origine, on récupérait un lien spécifique via ProfileActivity.
        // On utilise ici un lien par défaut défini dans les ressources.
        val link = getString(R.string.donate_default_link)
        openLink(link)
    }

    private fun onFAQClicked() {
        val FAQUrl = getString(R.string.faq_link_public)
        openLink(FAQUrl)
    }

    private fun onAmbassadorClicked() {
        val ambassadorUrl = getString(R.string.ambassadeur_link_public)
        openLink(ambassadorUrl)
    }

    private fun onEthicChartClicked() {
        val chartUrl = getString(R.string.disclaimer_link_public)
        openLink(chartUrl)
    }

    private fun onPartnerClicked() {
        val partnerUrl = getString(R.string.url_app_partner)
        openLink(partnerUrl)
    }

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (ex: Exception) {
            showNoBrowserError()
        }
    }

    private fun showNoBrowserError() {
        EntSnackbar.make(
            binding.aboutCoordinatorLayout,
            R.string.no_browser_error,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun handleCloseButton() {
        binding.header.headerHelpIconCross.setOnClickListener {
            finish()
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, HelpAboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}
