package social.entourage.android.profile.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentHelpAboutBinding
import social.entourage.android.profile.ProfileActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.view.EntSnackbar

class HelpAboutFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentHelpAboutBinding? = null
    val binding: NewFragmentHelpAboutBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentHelpAboutBinding.inflate(inflater, container, false)

        updatePaddingTopForEdgeToEdge(binding.header.headerHelpLayout)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        initializeView()
        populate()
        handleCloseButton()
    }

    override fun onResume() {
        super.onResume()
        setFullScreenBehavior()
    }

    private fun setFullScreenBehavior() {
        val dialog = dialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? ViewGroup
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
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
            EntSnackbar.make(
                binding.aboutCoordinatorLayout,
                R.string.no_browser_error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun onOSSLicensesClicked() {
        startActivity(Intent(context, OssLicensesMenuActivity::class.java))
    }

    private fun onPrivacyClicked() {
        (activity as? ProfileActivity)?.showWebViewForLinkId(Constants.PRIVACY_LINK_ID)
    }

    private fun onRateUsClicked() {
        activity?.let {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ABOUT_RATING)
            val goToMarket = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.market_url, it.packageName))
            )
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.playstore_url, it.packageName))
                    )
                )
            }
        }
    }

    private fun onDonate() {
        val donationIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse((activity as? ProfileActivity)?.getLink(Constants.DONATE_LINK_ID))
        )
        launchActivity(donationIntent)
    }

    private fun onFAQClicked() {
        val FAQIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.faq_link_public))
            )
        launchActivity(FAQIntent)

    }

    private fun onAmbassadorClicked() {
        val ambassadorIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.ambassadeur_link_public))
            )
        launchActivity(ambassadorIntent)

    }

    private fun onEthicChartClicked() {
        val chartUrl = activity?.getString(R.string.disclaimer_link_public)
        val chartIntent = Intent(Intent.ACTION_VIEW, Uri.parse(chartUrl))
        launchActivity(chartIntent)
    }

    private fun onPartnerClicked() {
        val chartUrl = activity?.getString(R.string.url_app_partner)
        val chartIntent = Intent(Intent.ACTION_VIEW, Uri.parse(chartUrl))
        launchActivity(chartIntent)
    }

    private fun launchActivity(intent: Intent) {
        try {
            startActivityForResult(intent, 0)
        } catch (ex: Exception) {
            Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCloseButton() {
        binding.header.headerHelpIconCross.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "HelpAboutFragment"
        fun newInstance(): HelpAboutFragment {
            return HelpAboutFragment()
        }
    }

}