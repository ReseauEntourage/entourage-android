package social.entourage.android.home.neo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_neo_street.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.home.HomeFragment
import social.entourage.android.tools.log.AnalyticsEvents

class HomeNeoStreetFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_street, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? HomeFragment)?.onBackPressed()
        }
        ui_layout_street1?.setOnClickListener {
            (parentFragment as? HomeFragment)?.showWebLink(Constants.SLUG_HOME_NEO_STREET_1)
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDFIRST_OnlineTraining)
        }

        ui_layout_street2?.setOnClickListener {
            (parentFragment as? HomeFragment)?.showWebLink(Constants.SLUG_HOME_NEO_STREET_2)
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDFIRST_SCBonjour)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.home.neo.street"
    }
}