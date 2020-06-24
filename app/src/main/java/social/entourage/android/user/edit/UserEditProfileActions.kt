package social.entourage.android.user.edit

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_user_edit_profile_actions.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.onboarding.UserTypeSelection
import social.entourage.android.onboarding.asso.AssoActivities
import social.entourage.android.onboarding.sdf_neighbour.SdfNeighbourActivities
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import social.entourage.android.view.CustomProgressDialog
import timber.log.Timber

private const val ARG_PARAM1 = "param1"

class UserEditProfileActions : EntourageDialogFragment() {

    private var activitiesSelection: SdfNeighbourActivities? = null
    private var activitiesAssoSelection: AssoActivities? = null

    private var userTypeSelected: UserTypeSelection = UserTypeSelection.NONE

    private var isSdf = true
    private var isAsso = false

    lateinit var alertDialog: CustomProgressDialog

    private var callback:ValidateActionsDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userTypeSelected = it.getSerializable(ARG_PARAM1) as UserTypeSelection
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_edit_profile_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alertDialog = CustomProgressDialog(requireContext())

        when(userTypeSelected) {
            UserTypeSelection.NEIGHBOUR -> {
                isAsso = false
                isSdf = false
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_PROFILE_NEIGHBOR_MOSAIC)
            }
            UserTypeSelection.ALONE -> {
                isAsso = false
                isSdf = true
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_PROFILE_INNEED_MOSAIC)
            }
            UserTypeSelection.ASSOS -> {
                isAsso = true
                isSdf = false
                EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_PROFILE_PRO_MOSAIC)
            }
            else -> {}
        }

        ui_bt_validate?.disable()

        setupTexts()
        setupImages()
        setupViews()
    }

    /********************************
     * Methods
     ********************************/

    fun setupCallback(callback: ValidateActionsDelegate) {
        this.callback = callback
    }

    fun setupTexts() {
        if (isAsso) {
            ui_onboard_type_tv_title?.text = getString(R.string.profile_asso_activity_title)
            ui_onboard_type_tv_info?.text = getString(R.string.onboard_asso_activity_description)

            ui_onboard_sdf_neigbour_activities_tv_1?.text = getString(R.string.onboard_asso_activity_choice_1)
            ui_onboard_sdf_neigbour_activities_tv_2?.text = getString(R.string.onboard_asso_activity_choice_2)
            ui_onboard_sdf_neigbour_activities_tv_3?.text = getString(R.string.onboard_asso_activity_choice_3)
            ui_onboard_sdf_neigbour_activities_tv_4?.text = getString(R.string.onboard_asso_activity_choice_4)
            return
        }

        val _txt = if (isSdf) getString(R.string.profile_sdf_activity_title) else getString(R.string.profile_neighbour_activity_title)
        ui_onboard_type_tv_title?.text = _txt
        val _desc = if (isSdf) getString(R.string.onboard_sdf_activity_description) else getString(R.string.onboard_neighbour_activity_description)
        ui_onboard_type_tv_info?.text = _desc

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
        if (isAsso) {

            ui_onboard_sdf_neigbour_activities_iv_choice_1?.setImageResource(R.drawable.ic_onboard_picto_aide)
            ui_onboard_sdf_neigbour_activities_iv_choice_2?.setImageResource(R.drawable.ic_onboard_picto_culture)
            ui_onboard_sdf_neigbour_activities_iv_choice_3?.setImageResource(R.drawable.ic_onboard_picto_investir)
            ui_onboard_sdf_neigbour_activities_iv_choice_4?.setImageResource(R.drawable.ic_onboard_picto_autre)
            return
        }

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
            if (isAsso)  {changeAssoSelectionViewPosition(1); return@setOnClickListener }
            changeSelectionViewPosition(1)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice2?.setOnClickListener {
            if (isAsso)  {changeAssoSelectionViewPosition(2); return@setOnClickListener }
            changeSelectionViewPosition(2)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice3?.setOnClickListener {
            if (isAsso)  {changeAssoSelectionViewPosition(3); return@setOnClickListener }
            changeSelectionViewPosition(3)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice4?.setOnClickListener {
            if (isAsso)  {changeAssoSelectionViewPosition(4); return@setOnClickListener }
            changeSelectionViewPosition(4)
        }
        ui_onboard_sdf_neigbour_activities_layout_choice5?.setOnClickListener {
            changeSelectionViewPosition(5)
        }

        when {
            isAsso -> {
                ui_onboard_sdf_neigbour_activities_layout_choice5?.visibility = View.GONE
                ui_onboard_sdf_neigbour_activities_layout_choice6?.visibility = View.GONE
            }
            isSdf -> {
                ui_onboard_sdf_neigbour_activities_layout_choice6?.setOnClickListener {
                    changeSelectionViewPosition(6)
                }
            }
            else -> {
                ui_onboard_sdf_neigbour_activities_layout_choice6?.visibility = View.INVISIBLE
            }
        }

        ui_bt_validate?.setOnClickListener {
            updateGoalType()
        }

        ui_bt_back?.setOnClickListener {
            dismiss()
        }
    }

    private fun changeSelectionViewPosition(position:Int) {
        if (activitiesSelection == null) { activitiesSelection = SdfNeighbourActivities() }

        activitiesSelection?.isSdf = this.isSdf
        val currentActivities = this.activitiesSelection!!

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

        this.activitiesSelection = currentActivities


        if (this.activitiesSelection != null && this.activitiesSelection!!.hasOneSelectionMin()) {
            updateButtonNext(true)
        }
        else {
            updateButtonNext(false)
        }
    }

    private fun changeAssoSelectionViewPosition(position:Int) {
        if (activitiesAssoSelection == null) { activitiesAssoSelection = AssoActivities() }

        val currentActivities = this.activitiesAssoSelection!!

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
        }

        this.activitiesAssoSelection = currentActivities


        if (this.activitiesAssoSelection != null && this.activitiesAssoSelection!!.hasOneSelectionMin()) {
            updateButtonNext(true)
        }
        else {
            updateButtonNext(false)
        }
    }

    fun updateButtonNext(isValid:Boolean) {
        if (isValid) {
            ui_bt_validate?.enable()
        }
        else {
            ui_bt_validate?.disable()
        }
    }

    private fun changeColors(layout: ConstraintLayout, textView: TextView, isSelected:Boolean) {
        if (isSelected) {
            layout.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_orange_stroke,null)
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
        else {
            layout.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
            textView.setTypeface(null, Typeface.NORMAL)
        }
    }

    /*************
     * Network
     */

    fun updateGoalType() {
        alertDialog.show(R.string.onboard_waiting_dialog)
        val _currentGoal = userTypeSelected.getGoalString()

        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_CHOOSE_PROFILE_SIGNUP)

        OnboardingAPI.getInstance(EntourageApplication.get()).updateUserGoal(_currentGoal) { isOK, userResponse ->
            if (isOK && userResponse != null) {
                val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                authenticationController.saveUser(userResponse.user)
            }
           updateActivities()
        }
    }

    fun updateActivities() {
        if (isAsso) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_PRO_MOSAIC)
            OnboardingAPI.getInstance(EntourageApplication.get()).updateUserInterests(activitiesAssoSelection!!.getArrayForWs()) { isOK, userResponse ->
                if (isOK && userResponse != null) {
                    val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                    authenticationController.saveUser(userResponse.user)
                }

                alertDialog.dismiss()
                dismiss()
                callback?.validateActions()
            }
        }
        else {
            if (isSdf) { EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_INNEED_MOSAIC) }
            else { EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_PROFILE_NEIGHBOR_MOSAIC) }

            OnboardingAPI.getInstance(EntourageApplication.get()).updateUserInterests(activitiesSelection!!.getArrayForWs()) { isOK, userResponse ->
                if (isOK && userResponse != null) {
                    val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                    authenticationController.saveUser(userResponse.user)
                }

                alertDialog.dismiss()
                dismiss()
                callback?.validateActions()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destroy FG actions")
        callback = null
    }
    companion object {
        const val TAG = "social.entourage.android.user.edit.UserEditProfileActions"

        @JvmStatic
        fun newInstance(userTypeSelection: UserTypeSelection) =
                UserEditProfileActions().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1, userTypeSelection)
                    }
                }
    }
}

interface ValidateActionsDelegate {
    fun validateActions()
}