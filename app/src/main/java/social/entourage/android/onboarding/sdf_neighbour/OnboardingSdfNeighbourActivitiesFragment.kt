package social.entourage.android.onboarding.sdf_neighbour

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_onboarding_sdf_neighbour_activities.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.onboarding.OnboardingCallback
import java.io.Serializable

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class OnboardingSdfNeighbourActivitiesFragment : Fragment() {
    private var currentActivities: SdfNeighbourActivities? = null

    private var callback: OnboardingCallback? = null
    private var username:String? = null

    private var isSdf = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentActivities = it.getSerializable(ARG_PARAM1) as? SdfNeighbourActivities
            username = it.getString(ARG_PARAM2)
            isSdf = it.getBoolean(ARG_PARAM3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_sdf_neighbour_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTexts()
        setupImages()
        setupViews()

        if (isSdf) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_INNEED_MOSAIC)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_NEIGHBOR_MOSAIC)
        }
    }

    /********************************
     * Methods
     ********************************/

    fun setupTexts() {
        val _txt = if (isSdf) getString(R.string.onboard_sdf_activity_title) else getString(R.string.onboard_neighbour_activity_title)
        ui_onboard_type_tv_title?.text = String.format(_txt,username ?: "")
        val _desc = if (isSdf) getString(R.string.onboard_sdf_activity_description) else getString(R.string.onboard_neighbour_activity_description)
        ui_onboard_type_tv_info?.text = String.format(_desc,username ?: "")

        val _choice1Key = if (isSdf) getString(R.string.onboard_sdf_activity_choice_1) else getString(R.string.onboard_neighbour_activity_choice_1)
        val _choice2Key = if (isSdf) getString(R.string.onboard_sdf_activity_choice_2) else getString(R.string.onboard_neighbour_activity_choice_2)
        val _choice3Key = if (isSdf) getString(R.string.onboard_sdf_activity_choice_3) else getString(R.string.onboard_neighbour_activity_choice_3)
        val _choice4Key = if (isSdf) getString(R.string.onboard_sdf_activity_choice_4) else getString(R.string.onboard_neighbour_activity_choice_4)
        val _choice5Key = if (isSdf) getString(R.string.onboard_sdf_activity_choice_5) else getString(R.string.onboard_neighbour_activity_choice_5)
        val _choice6Key = getString(R.string.onboard_sdf_activity_choice_6)

        ui_onboard_sdf_neigbour_activities_tv_1?.text = _choice1Key
        ui_onboard_sdf_neigbour_activities_tv_2?.text = _choice2Key
        ui_onboard_sdf_neigbour_activities_tv_3?.text = _choice3Key
        ui_onboard_sdf_neigbour_activities_tv_4?.text = _choice4Key
        ui_onboard_sdf_neigbour_activities_tv_5?.text = _choice5Key
        ui_onboard_sdf_neigbour_activities_tv_6?.text = _choice6Key
    }

    fun setupImages() {
        val choiceRes1 = if (isSdf) R.drawable.ic_sdf_circle else R.drawable.ic_neighbour_info
        val choiceRes2 = if (isSdf) R.drawable.ic_sdf_events else R.drawable.ic_neighbour_events
        val choiceRes3 = if (isSdf) R.drawable.ic_sdf_question else R.drawable.ic_neighbour_entourer
        val choiceRes4 = if (isSdf) R.drawable.ic_sdf_help else R.drawable.ic_neighbour_gift
        val choiceRes5 = if (isSdf) R.drawable.ic_sdf_orienter else R.drawable.ic_neighbour_investir
        val choiceRes6 = R.drawable.ic_sdf_search

        ui_onboard_sdf_neigbour_activities_iv_choice_1?.setImageResource(choiceRes1)
        ui_onboard_sdf_neigbour_activities_iv_choice_2?.setImageResource(choiceRes2)
        ui_onboard_sdf_neigbour_activities_iv_choice_3?.setImageResource(choiceRes3)
        ui_onboard_sdf_neigbour_activities_iv_choice_4?.setImageResource(choiceRes4)
        ui_onboard_sdf_neigbour_activities_iv_choice_5?.setImageResource(choiceRes5)
        ui_onboard_sdf_neigbour_activities_iv_choice_6?.setImageResource(choiceRes6)
    }

    fun setupViews() {
        ui_onboard_sdf_neigbour_activities_layout_choice1?.setOnClickListener {
            changeSelectionViewPosition(1)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice2?.setOnClickListener {
            changeSelectionViewPosition(2)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice3?.setOnClickListener {
            changeSelectionViewPosition(3)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice4?.setOnClickListener {
            changeSelectionViewPosition(4)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice5?.setOnClickListener {
            changeSelectionViewPosition(5)
        }
        if (isSdf) {
            ui_onboard_sdf_neigbour_activities_layout_choice6?.setOnClickListener {
                changeSelectionViewPosition(6)
            }
        }
        else {
            ui_onboard_sdf_neigbour_activities_layout_choice6?.visibility = View.INVISIBLE
        }

        if (currentActivities != null && currentActivities!!.hasOneSelectionMin()) {
            callback?.updateButtonNext(true)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice1,ui_onboard_sdf_neigbour_activities_tv_1,currentActivities!!.choice1Selected)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice2,ui_onboard_sdf_neigbour_activities_tv_2,currentActivities!!.choice2Selected)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice3,ui_onboard_sdf_neigbour_activities_tv_3,currentActivities!!.choice3Selected)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice4,ui_onboard_sdf_neigbour_activities_tv_4,currentActivities!!.choice4Selected)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice5,ui_onboard_sdf_neigbour_activities_tv_5,currentActivities!!.choice5Selected)
            changeColors(ui_onboard_sdf_neigbour_activities_layout_choice6,ui_onboard_sdf_neigbour_activities_tv_6,currentActivities!!.choice6Selected)
        }
        else {
            callback?.updateButtonNext(false)
        }
    }

    private fun changeSelectionViewPosition(position:Int) {
        if (currentActivities == null) { currentActivities = SdfNeighbourActivities() }

        currentActivities?.isSdf = this.isSdf
        val currentActivities = this.currentActivities!!

        when(position) {
            1 -> {
                currentActivities.choice1Selected = !currentActivities.choice1Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice1,ui_onboard_sdf_neigbour_activities_tv_1,currentActivities.choice1Selected)
            }
            2 -> {
                currentActivities.choice2Selected = !currentActivities.choice2Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice2,ui_onboard_sdf_neigbour_activities_tv_2,currentActivities.choice2Selected)
            }
            3 -> {
                currentActivities.choice3Selected = !currentActivities.choice3Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice3,ui_onboard_sdf_neigbour_activities_tv_3,currentActivities.choice3Selected)
            }
            4 -> {
                currentActivities.choice4Selected = !currentActivities.choice4Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice4,ui_onboard_sdf_neigbour_activities_tv_4,currentActivities.choice4Selected)
            }
            5 -> {
                currentActivities.choice5Selected = !currentActivities.choice5Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice5,ui_onboard_sdf_neigbour_activities_tv_5,currentActivities.choice5Selected)
            }
            6 -> {
                currentActivities.choice6Selected = !currentActivities.choice6Selected
                changeColors(ui_onboard_sdf_neigbour_activities_layout_choice6,ui_onboard_sdf_neigbour_activities_tv_6,currentActivities.choice6Selected)
            }
        }

        this.currentActivities = currentActivities

        callback?.updateSdfNeighbourActivities(currentActivities, isSdf)

        if (this.currentActivities != null && this.currentActivities!!.hasOneSelectionMin()) {
            callback?.updateButtonNext(true)
        }
        else {
            callback?.updateButtonNext(false)
        }
    }

    private fun changeColors(layout:ConstraintLayout, textView:TextView, isSelected:Boolean) {
        if (isSelected) {
            layout.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_orange_stroke,null)
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
        else {
            layout.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
            textView.setTypeface(null, Typeface.NORMAL)
        }
    }

    /********************************
     * Overrides
     ********************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    /********************************
     * Companion
     ********************************/

    companion object {
        fun newInstance(activities: SdfNeighbourActivities?, username:String?,isSdf:Boolean) =
                OnboardingSdfNeighbourActivitiesFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1,activities)
                        putString(ARG_PARAM2,username)
                        putBoolean(ARG_PARAM3,isSdf)
                    }
                }
    }
}

/********************************
 * Class SdfNeighbourActivities
 ********************************/

class SdfNeighbourActivities : Serializable {
     var choice1Selected = false
     var choice2Selected = false
     var choice3Selected = false
     var choice4Selected = false
     var choice5Selected = false
     var choice6Selected = false

    var isSdf = true

    fun hasOneSelectionMin() : Boolean {
        if (isSdf) {
            if (choice1Selected || choice2Selected
                    || choice3Selected || choice4Selected
                    || choice5Selected || choice6Selected) return true
        }
        else {
            if (choice1Selected || choice2Selected
                    || choice3Selected || choice4Selected || choice5Selected) return true
        }

        return false
    }

    fun reset() {
        choice1Selected = false
        choice2Selected = false
        choice3Selected = false
        choice4Selected = false
        choice5Selected = false
        choice6Selected = false
    }

    fun getArrayForWs() : ArrayList<String> {
        val _array = ArrayList<String>()
        if (choice1Selected) {
            val _choice = if (isSdf) "rencontrer_sdf" else "m_informer_riverain"
            _array.add(_choice)
        }
        if (choice2Selected) {
            val _choice = if (isSdf) "event_sdf" else "event_riverain"
            _array.add(_choice)
        }
        if (choice3Selected) {
            val _choice = if (isSdf) "questions_sdf" else "entourer_riverain"
            _array.add(_choice)
        }
        if (choice4Selected) {
            val _choice = if (isSdf) "aide_sdf" else "dons_riverain"
            _array.add(_choice)
        }
        if (choice5Selected) {
            val _choice = if (isSdf) "m_orienter_sdf" else "benevolat_riverain"
            _array.add(_choice)
        }
        if (choice6Selected) _array.add("trouver_asso_sdf")

        return _array
    }

    fun setupForSdf(isSdf:Boolean) {
        this.isSdf = isSdf

        choice1Selected = true
        choice2Selected = true
        choice3Selected = true
        choice4Selected = true
        choice5Selected = true

        choice6Selected = isSdf
    }
}