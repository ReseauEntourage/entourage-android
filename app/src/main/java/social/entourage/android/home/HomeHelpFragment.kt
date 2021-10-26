package social.entourage.android.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_help.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.Constants
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Created on 3/26/21.
 */
class HomeHelpFragment: BaseDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title_close_button?.setOnClickListener {
            dismiss()
        }

        ui_home_bt_1?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_expertFeed_HelpReporter)
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            val homeReport = HomeHelpReporterFragment()
            homeReport.show(activity.supportFragmentManager, HomeHelpReporterFragment.TAG)
        }
        ui_home_bt_2?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_expertFeed_HelpGift)
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.showWebView(activity.getLink(Constants.DONATE_LINK_ID))
        }
        ui_home_bt_3?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_expertFeed_HelpShare)
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.showWebView(activity.getString(R.string.facebook_url))
        }
        ui_home_bt_4?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_expertFeed_HelpAmbassador)
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.showWebView(activity.getLink(Constants.AMBASSADOR_ID))
        }
        ui_home_bt_5?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_expertFeed_HelpLinkedout)
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.showWebView(activity.getString(R.string.url_linkedout))
        }
    }

    companion object {
        val TAG: String? = HomeHelpFragment::class.java.simpleName
    }
}