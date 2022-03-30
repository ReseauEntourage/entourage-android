package social.entourage.android.new_v8.profile.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_about.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentHelpAboutBinding
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar

class HelpAboutFragment : Fragment() {

    private var _binding: NewFragmentHelpAboutBinding? = null
    val binding: NewFragmentHelpAboutBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentHelpAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        setBackButton()
        populate()
    }

    private fun initializeView() {
        binding.licence.divider.visibility = View.GONE
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun populate() {
        binding.cgu.layout.setOnClickListener { onTermsClicked() }
        binding.confidentiality.layout.setOnClickListener { onPrivacyClicked() }
        binding.licence.layout.setOnClickListener { onOSSLicensesClicked() }
        binding.feedback.layout.setOnClickListener { onRateUsClicked() }
        binding.faq.layout.setOnClickListener { onFAQClicked() }
        binding.donation.layout.setOnClickListener { onDonate() }
        binding.ambassadorProgram.layout.setOnClickListener { onAmbassadorClicked() }
        binding.ethic.layout.setOnClickListener { onEthicChartClicked() }
    }


    // ----------------------------------
    // BUTTON HANDLING
    // ----------------------------------
    private fun onTermsClicked() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_url)))
        try {
            startActivity(browserIntent)
        } catch (ex: ActivityNotFoundException) {
            about_coordinator_layout?.let {
                EntSnackbar.make(
                    it,
                    R.string.no_browser_error,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
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
        try {
            activity?.startActivity(donationIntent)
        } catch (ex: Exception) {
            Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }


    private fun onFAQClicked() {
        (activity as? ProfileActivity)?.showWebViewForLinkId(Constants.FAQ_LINK_ID)
    }

    private fun onAmbassadorClicked() {
        val ambassadorIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse((activity as? ProfileActivity)?.getLink(Constants.AMBASSADOR_ID))
            )
        try {
            activity?.startActivity(ambassadorIntent)
        } catch (ex: Exception) {
            Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onEthicChartClicked() {
        val chartUrl = activity?.getString(R.string.disclaimer_link_public)
        val chartIntent = Intent(Intent.ACTION_VIEW, Uri.parse(chartUrl))
        try {
            activity?.startActivity(chartIntent)
        } catch (ex: Exception) {
            Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
        }
    }

}