package social.entourage.android.user.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_user_edit_profile_type.*
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.onboarding.onboard.UserTypeSelection
import social.entourage.android.tools.disable
import social.entourage.android.tools.enable
import timber.log.Timber


class UserEditProfileType : BaseDialogFragment(),ValidateActionsDelegate {

    private var userTypeSelected: UserTypeSelection = UserTypeSelection.NONE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_edit_profile_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        if (userTypeSelected == UserTypeSelection.NONE) {
            updateButtonNext(false)
        }
        else {
            updateButtonNext(true)
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_VIEW_ONBOARDING_CHOOSE_PROFILE)
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    fun setupViews() {
        ui_bt_validate?.setOnClickListener {
            val frag = UserEditProfileActionsFragment.newInstance(userTypeSelected)
            frag.setupCallback(this)
            frag.show(parentFragmentManager, UserEditProfileActionsFragment.TAG)
        }

        ui_bt_back?.setOnClickListener {
            dismiss()
        }

        ui_onboard_type_layout_neighbour?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_neighbour)
        }
        ui_onboard_type_layout_alone?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_alone)
        }
        ui_onboard_type_layout_assos?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_assos)
        }

        selectInitialType()
    }

    fun selectInitialType() {
        when(userTypeSelected) {
            UserTypeSelection.ALONE -> changeLayoutSelection(ui_onboard_type_layout_alone)
            UserTypeSelection.ASSOS -> changeLayoutSelection(ui_onboard_type_layout_assos)
            UserTypeSelection.NEIGHBOUR -> changeLayoutSelection(ui_onboard_type_layout_neighbour)
            else -> return
        }
    }

    fun changeLayoutSelection(selectedLayout: ConstraintLayout?) {
        when(selectedLayout) {
            ui_onboard_type_layout_neighbour -> {
                userTypeSelected = UserTypeSelection.NEIGHBOUR
                ui_onboard_type_layout_neighbour?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_orange_stroke,null)
                ui_onboard_type_layout_alone?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
                ui_onboard_type_layout_assos?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
            }
            ui_onboard_type_layout_alone -> {
                userTypeSelected = UserTypeSelection.ALONE
                ui_onboard_type_layout_neighbour?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
                ui_onboard_type_layout_alone?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_orange_stroke,null)
                ui_onboard_type_layout_assos?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
            }
            ui_onboard_type_layout_assos -> {
                userTypeSelected = UserTypeSelection.ASSOS
                ui_onboard_type_layout_neighbour?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
                ui_onboard_type_layout_alone?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_grey_plain,null)
                ui_onboard_type_layout_assos?.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_rounded_onboard_orange_stroke,null)
            }
        }

        updateButtonNext(true)
    }

    fun updateButtonNext(isActive:Boolean) {
        if (isActive) {
            ui_bt_validate?.enable()
        }
        else {
            ui_bt_validate?.disable()
        }

    }

    override fun validateActions() {
        Timber.d("Validate Actions close callback")
        dismiss()
    }

    companion object {
        const val TAG = "social.entourage.android.user.edit.UserEditProfileType"
    }
}