package social.entourage.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_plus.*
import kotlinx.android.synthetic.main.layout_plus_overlay.*
import social.entourage.android.api.model.User
import social.entourage.android.base.BackPressable
import social.entourage.android.tools.log.AnalyticsEvents

class PlusFragment : Fragment(), BackPressable {
    override fun onResume() {
        super.onResume()
        EntourageApplication.get().components.authenticationController.savedTour?.let {
            layout_line_add_tour_encounter?.visibility = View.VISIBLE
        } ?: run {
            layout_line_add_tour_encounter?.visibility = View.GONE
        }
        if (EntourageApplication.me(activity)?.isPro == true) {
            layout_line_start_tour_launcher?.visibility = View.VISIBLE
            (ui_image_plus?.tag as String?)?.let { tag ->
                if (tag == "normal") {
                    ui_image_plus?.visibility = View.GONE
                    plus_image_subtitle?.visibility = View.GONE
                }
            }
        }
        else {
            layout_line_start_tour_launcher?.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_PLUS_SCREEN)

        plus_help_button?.setOnClickListener {onHelpButton()}
        layout_line_create_entourage_ask_help?.setOnClickListener {
            onAction(KEY_CREATE_DEMAND, AnalyticsEvents.ACTION_PLUS_CREATE_ASKFORHELP)
        }
        layout_line_create_entourage_contribute?.setOnClickListener {
            onAction(KEY_CREATE_CONTRIBUTION, AnalyticsEvents.ACTION_PLUS_CREATE_CONTRIBUTE)
        }
        layout_line_create_outing?.setOnClickListener {
            onAction(KEY_CREATE_OUTING, AnalyticsEvents.ACTION_PLUS_CREATE_OUTING)
        }
        layout_line_start_tour_launcher?.setOnClickListener {
            onAction(KEY_START_TOUR, AnalyticsEvents.ACTION_PLUS_START_TOUR)
        }
        layout_line_add_tour_encounter?.setOnClickListener {
            onAction(KEY_ADD_ENCOUNTER,AnalyticsEvents.ACTION_PLUS_ADD_ENCOUNTER)
        }
        fragment_plus_overlay?.setOnClickListener {onBackPressed()}
        layout_line_create_good_waves?.setOnClickListener { onShowGoodWaves() }

        setupTexts()
    }

    private fun setupTexts() {
       EntourageApplication.get().me()?.let { currentUser ->
           if (currentUser.isUserTypeAlone) {
               plus_help_button?.text = getString(R.string.agir_help_button_title_alone)
               ui_tv_agir_good_waves_subtitle?.text = getString(R.string.agir_bonnes_ondes_alone)
           }
           else if (currentUser.goal.equals(User.USER_GOAL_ASSO)) {
               plus_help_button?.text = getString(R.string.agir_help_button_help_asso)
               ui_tv_agir_good_waves_subtitle?.text = getString(R.string.agir_bonnes_ondes_others)
           }
           else {
               plus_help_button?.text = getString(R.string.agir_help_button_help_others)
               ui_tv_agir_good_waves_subtitle?.text = getString(R.string.agir_bonnes_ondes_others)
           }

           val isUserHelp = currentUser.goal?.contains("ask_for_help", true) ==true
                   || currentUser.goal?.contains("offer_help", true)==true
           val hasPartnerId = currentUser.partner?.id != null
           val isAmbassador = currentUser.roles?.contains("ambassador")==true

           val showAddEvent = !isUserHelp || hasPartnerId || isAmbassador
           layout_line_create_outing?.visibility = if (showAddEvent) View.VISIBLE else View.GONE
       }
    }

    private fun onHelpButton() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PLUS_HELP)
        EntourageApplication.get().me()?.let { currentUser ->
            when {
                currentUser.isUserTypeAlone -> mainActivity?.showWebViewForLinkId(Constants.AGIR_FAQ_ID)
                currentUser.goal.equals(User.USER_GOAL_ASSO) -> mainActivity?.showWebViewForLinkId(Constants.ASSO_AGIR_LINK_ID)
                else -> mainActivity?.showWebViewForLinkId(Constants.SCB_LINK_ID, R.string.webview_share_text)
            }
        }
    }

    private fun onAction(action: String, eventName: String) {
        AnalyticsEvents.logEvent(eventName)
        val newIntent = Intent(context, MainActivity::class.java).apply { this.action = action }
        startActivity(newIntent)
        mainActivity?.showFeed()
    }

    private fun onShowGoodWaves() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PLUS_GOOD_WAVES)
        mainActivity?.showWebViewForLinkId(Constants.GOOD_WAVES_ID)
    }

    override fun onBackPressed(): Boolean {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PLUS_BACK)
        mainActivity?.showFeed()
        return true
    }

    val mainActivity: MainActivity?
        get() = activity as? MainActivity

    companion object {
        const val TAG = "social.entourage.android.fragment_plus"
        const val KEY_START_TOUR = "social.entourage.android.KEY_START_TOUR"
        const val KEY_ADD_ENCOUNTER = "social.entourage.android.KEY_ADD_ENCOUNTER"
        const val KEY_CREATE_DEMAND = "social.entourage.android.KEY_CREATE_DEMAND"
        const val KEY_CREATE_CONTRIBUTION = "social.entourage.android.KEY_CREATE_CONTRIBUTION"
        const val KEY_CREATE_OUTING = "social.entourage.android.KEY_CREATE_OUTING"
    }
}