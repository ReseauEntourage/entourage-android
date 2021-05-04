package social.entourage.android.newsfeed.v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home_neo_tour_start.*
import kotlinx.android.synthetic.main.fragment_home_neo_tour_start.ui_bt_back
import social.entourage.android.R
import social.entourage.android.tools.log.AnalyticsEvents

class HomeNeoTourStartFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_tour_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.onBackPressed()
        }

        ui_home_neo_button_tour_start?.setOnClickListener {
            (parentFragment as? NewHomeFeedFragment)?.goTourList()
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_NEOFEEDFIRST_GoTour)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.home.neo.tour.start"
    }
}