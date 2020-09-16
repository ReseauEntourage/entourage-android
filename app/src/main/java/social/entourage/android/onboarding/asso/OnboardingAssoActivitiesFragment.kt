package social.entourage.android.onboarding.asso

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_onboarding_asso_activities.*
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.onboarding.OnboardingCallback
import java.io.Serializable

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class OnboardingAssoActivitiesFragment : Fragment() {
    private var currentActivities: AssoActivities? = null

    private var callback: OnboardingCallback? = null
    private var username:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentActivities = it.getSerializable(ARG_PARAM1) as? AssoActivities
            username = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_asso_activities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_ONBOARDING_PRO_MOSAIC)
    }

    /********************************
     * Methods
     ********************************/

    fun setupViews() {
        val _txt = getString(R.string.onboard_asso_activity_title)
        ui_onboard_type_tv_title?.text = String.format(_txt,username ?: "")

        ui_onboard_asso_activities_layout_choice1?.setOnClickListener {
            changeSelectionViewPosition(1)
        }
        ui_onboard_asso_activities_layout_choice2?.setOnClickListener {
            changeSelectionViewPosition(2)
        }
        ui_onboard_asso_activities_layout_choice3?.setOnClickListener {
            changeSelectionViewPosition(3)
        }
        ui_onboard_asso_activities_layout_choice4?.setOnClickListener {
            changeSelectionViewPosition(4)
        }

        if (currentActivities != null && currentActivities!!.hasOneSelectionMin()) {
            callback?.updateButtonNext(true)
            changeColors(ui_onboard_asso_activities_layout_choice1,ui_onboard_asso_activities_tv_1,currentActivities!!.choice1Selected)
            changeColors(ui_onboard_asso_activities_layout_choice2,ui_onboard_asso_activities_tv_2,currentActivities!!.choice2Selected)
            changeColors(ui_onboard_asso_activities_layout_choice3,ui_onboard_asso_activities_tv_3,currentActivities!!.choice3Selected)
            changeColors(ui_onboard_asso_activities_layout_choice4,ui_onboard_asso_activities_tv_4,currentActivities!!.choice4Selected)
        }
        else {
            callback?.updateButtonNext(false)
        }
    }

    private fun changeSelectionViewPosition(position:Int) {
        if (currentActivities == null) { currentActivities = AssoActivities() }

        val currentActivities = this.currentActivities!!

        when(position) {
            1 -> {
                currentActivities.choice1Selected = !currentActivities.choice1Selected
                changeColors(ui_onboard_asso_activities_layout_choice1,ui_onboard_asso_activities_tv_1,currentActivities.choice1Selected)
            }
            2 -> {
                currentActivities.choice2Selected = !currentActivities.choice2Selected
                changeColors(ui_onboard_asso_activities_layout_choice2,ui_onboard_asso_activities_tv_2,currentActivities.choice2Selected)
            }
            3 -> {
                currentActivities.choice3Selected = !currentActivities.choice3Selected
                changeColors(ui_onboard_asso_activities_layout_choice3,ui_onboard_asso_activities_tv_3,currentActivities.choice3Selected)
            }
            4 -> {
                currentActivities.choice4Selected = !currentActivities.choice4Selected
                changeColors(ui_onboard_asso_activities_layout_choice4,ui_onboard_asso_activities_tv_4,currentActivities.choice4Selected)
            }
        }

        this.currentActivities = currentActivities

        callback?.updateAssoActivities(currentActivities)

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
        fun newInstance(activities:AssoActivities?,username:String?) =
                OnboardingAssoActivitiesFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1,activities)
                        putString(ARG_PARAM2,username)
                    }
                }
    }
}

/********************************
 * Class AssoActivities
 ********************************/

class AssoActivities : Serializable {
     var choice1Selected = false
     var choice2Selected = false
     var choice3Selected = false
     var choice4Selected = false

    fun hasOneSelectionMin() : Boolean {
        if (choice1Selected || choice2Selected
                || choice3Selected || choice4Selected) return true

        return false
    }

    fun reset() {
        choice1Selected = false
        choice2Selected = false
        choice3Selected = false
        choice4Selected = false
    }

    fun getArrayForWs() : ArrayList<String> {
        val _array = ArrayList<String>()
        if (choice1Selected) _array.add("aide_pers_asso")
        if (choice2Selected) _array.add("cult_sport_asso")
        if (choice3Selected) _array.add("serv_pub_asso")
        if (choice4Selected) _array.add("autre_asso")

        return _array
    }
}