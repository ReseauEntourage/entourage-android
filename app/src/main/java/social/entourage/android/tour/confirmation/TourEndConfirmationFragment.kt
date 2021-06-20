package social.entourage.android.tour.confirmation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_tour_end_confirmation.*
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.Utils
import java.util.*

class TourEndConfirmationFragment  : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private lateinit var tour: Tour

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.layout_tour_end_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentTour = arguments?.getSerializable(Tour.KEY_TOUR) as Tour?
        if(currentTour==null) {
            //dismiss fragment if no tour given
            dismiss()
            return
        }
        tour = currentTour
        initializeView()
        confirmation_end_button?.setOnClickListener { startTourActivity(KEY_END_TOUR)}
        confirmation_resume_button?.setOnClickListener { startTourActivity(KEY_RESUME_TOUR)}
    }

    override val slideStyle: Int
        get() = R.style.CustomDialogFragmentSlide

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initializeView() {
        tour_end_total_encounters?.text = getString(R.string.encounter_count_format, tour.encounters.size)
        tour_end_distance?.text = getString(R.string.tour_end_distance_value_in_km, tour.distance / 1000.0f)
        tour_end_duration?.text =  Utils.getDateStringFromSeconds(Date().time - tour.getStartTime().time)
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
    private fun startTourActivity(action: String) {
        val args = Bundle()
        args.putBoolean(action, true)
        args.putSerializable(Tour.KEY_TOUR, tour)
        val tourIntent = Intent(activity, MainActivity::class.java)
        tourIntent.putExtras(args)
        tourIntent.action = action
        startActivity(tourIntent)
        dismiss()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG: String? = TourEndConfirmationFragment::class.java.simpleName
        const val KEY_END_TOUR = "social.entourage.android.KEY_END_TOUR"
        const val KEY_RESUME_TOUR = "social.entourage.android.KEY_RESUME_TOUR"

        fun newInstance(tour: Tour): TourEndConfirmationFragment {
            val fragment = TourEndConfirmationFragment()
            val args = Bundle()
            args.putSerializable(Tour.KEY_TOUR, tour)
            fragment.arguments = args
            return fragment
        }
    }
}