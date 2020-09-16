package social.entourage.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_plus.*
import kotlinx.android.synthetic.main.layout_plus_overlay.*
import social.entourage.android.base.BackPressable
import social.entourage.android.tools.log.EntourageEvents

class PlusFragment : Fragment(), BackPressable {
    override fun onResume() {
        super.onResume()
        EntourageApplication.get().entourageComponent.authenticationController.savedTour?.let {
            layout_line_add_tour_encounter?.visibility = View.VISIBLE
            layout_line_start_tour_launcher?.visibility = View.GONE
            val tag = layout_line_start_tour_launcher?.tag as String
            if (tag.equals("normal")) {
                ui_image_plus?.visibility = View.GONE
            }
        } ?: run {
            layout_line_add_tour_encounter?.visibility = View.GONE

            if (EntourageApplication.me(activity)?.isPro == true) {
                layout_line_start_tour_launcher?.visibility = View.VISIBLE
                val tag = layout_line_start_tour_launcher?.tag as String
                if (tag.equals("normal")) {
                    ui_image_plus?.visibility = View.GONE
                }
            }
            else {
                layout_line_start_tour_launcher?.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plus_help_button?.setOnClickListener {onHelpButton()}
        layout_line_create_entourage_ask_help?.setOnClickListener {onAction(KEY_CREATE_DEMAND)}
        layout_line_create_entourage_contribute?.setOnClickListener {onAction(KEY_CREATE_CONTRIBUTION)}
        layout_line_create_outing?.setOnClickListener {onAction(KEY_CREATE_OUTING)}
        layout_line_start_tour_launcher?.setOnClickListener {onAction(KEY_START_TOUR)}
        layout_line_add_tour_encounter?.setOnClickListener {onAction(KEY_ADD_ENCOUNTER)}
        fragment_plus_overlay?.setOnClickListener {onBackPressed()}
        layout_line_create_good_waves?.setOnClickListener { onShowGoodWaves() }

        setupTexts()
    }

    private fun setupTexts() {
       EntourageApplication.get().me()?.let { currentUser ->
           if (currentUser.isUserTypeAlone) {
               plus_help_button?.text = getString(R.string.agir_new_button_title_alone)
               ui_tv_agir_good_waves_subtitle?.text = getString(R.string.agir_bonnes_ondes_alone)
           }
           else {
               plus_help_button?.text = getString(R.string.plus_help_text)
               ui_tv_agir_good_waves_subtitle?.text = getString(R.string.agir_bonnes_ondes_others)
           }
       }
    }

    private fun onHelpButton() {
        EntourageApplication.get().me()?.let { currentUser ->
            (activity as? MainActivity)?.showWebViewForLinkId(
                    if (currentUser.isUserTypeAlone)
                        Constants.AGIR_FAQ_ID
                    else
                        Constants.SCB_LINK_ID
            )
        }
    }

    private fun onAction(action: String) {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = action
        startActivity(newIntent)
        (activity as? MainActivity)?.showFeed()
    }

    private fun onShowGoodWaves() {
        EntourageEvents.logEvent(EntourageEvents.ACTION_PLUS_GOOD_WAVES)
        val url = (activity as MainActivity?)?.getLink(Constants.GOOD_WAVES_ID)

        (activity as MainActivity?)?.showWebView(url)
    }

    override fun onBackPressed(): Boolean {
        (activity as? MainActivity)?.showFeed()
        return true
    }

    companion object {
        const val TAG = "social.entourage.android.fragment_plus"
        const val KEY_START_TOUR = "social.entourage.android.KEY_START_TOUR"
        const val KEY_ADD_ENCOUNTER = "social.entourage.android.KEY_ADD_ENCOUNTER"
        const val KEY_CREATE_DEMAND = "social.entourage.android.KEY_CREATE_DEMAND"
        const val KEY_CREATE_CONTRIBUTION = "social.entourage.android.KEY_CREATE_CONTRIBUTION"
        const val KEY_CREATE_OUTING = "social.entourage.android.KEY_CREATE_OUTING"
    }
}