package social.entourage.android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_plus.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import kotlinx.android.synthetic.main.layout_plus_overlay.*
import social.entourage.android.authentication.AuthenticationController

class PlusFragment : Fragment(), BackPressable {
    override fun onResume() {
        super.onResume()
        val savedTour = EntourageApplication.get().entourageComponent?.getAuthenticationController()?.savedTour
        if (savedTour != null) {
            layout_line_add_tour_encounter?.visibility = View.VISIBLE
            layout_line_start_tour_launcher?.visibility = View.GONE
        } else {
            layout_line_add_tour_encounter?.visibility = View.GONE
            layout_line_start_tour_launcher?.visibility = if (EntourageApplication.me(activity)?.isPro == true) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        plus_help_button.setOnClickListener {onHelpButton()}
        layout_line_create_entourage_ask_help.setOnClickListener {onCreateEntourageHelpAction()}
        layout_line_create_entourage_contribute.setOnClickListener {onCreateEntourageContributionAction()}
        layout_line_create_outing?.setOnClickListener {onCreateOuting()}
        layout_line_start_tour_launcher?.setOnClickListener {onStartTourLauncher()}
        layout_line_add_tour_encounter?.setOnClickListener {onAddEncounter()}
        map_longclick_button_create_encounter?.setOnClickListener {onAddEncounter()}
        fragment_plus_overlay.setOnClickListener {onBackPressed()}
    }

    private fun onHelpButton() {
        (activity as MainActivity?)?.showWebViewForLinkId(Constants.SCB_LINK_ID)
    }

    private fun onCreateEntourageHelpAction() {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = KEY_CREATE_DEMAND
        startActivity(newIntent)
        (activity as MainActivity?)?.showFeed()
    }

    private fun onCreateEntourageContributionAction() {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = KEY_CREATE_CONTRIBUTION
        startActivity(newIntent)
        (activity as MainActivity?)?.showFeed()
    }

    private fun onCreateOuting() {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = KEY_CREATE_OUTING
        startActivity(newIntent)
        (activity as MainActivity?)?.showFeed()
    }

    private fun onStartTourLauncher() {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = KEY_START_TOUR
        startActivity(newIntent)
        (activity as MainActivity?)?.showFeed()
    }

    private fun onAddEncounter() {
        val newIntent = Intent(context, MainActivity::class.java)
        newIntent.action = KEY_ADD_ENCOUNTER
        startActivity(newIntent)
        (activity as MainActivity?)?.showFeed()
    }

    override fun onBackPressed(): Boolean {
        (activity as MainActivity?)?.showFeed()
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