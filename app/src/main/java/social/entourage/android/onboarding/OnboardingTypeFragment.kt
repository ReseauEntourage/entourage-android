package social.entourage.android.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.fragment_onboarding_type.*
import social.entourage.android.R
import social.entourage.android.tools.Logger

private const val ARG_FIRSTNAME = "firstname"
private const val ARG_USERTYPE = "usertype"

class OnboardingTypeFragment : Fragment() {
    private var firstname: String? = null
    private var userTypeSelected:UserTypeSelection = UserTypeSelection.NONE

    private var callback:OnboardingCallback? = null

    //**********//**********//**********
    // Lifecycle
    //**********//**********//**********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            firstname = it.getString(ARG_FIRSTNAME)
            userTypeSelected = it.getSerializable(ARG_USERTYPE) as UserTypeSelection
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        if (userTypeSelected == UserTypeSelection.NONE) {
            callback?.updateButtonNext(false)
        }
        else {
            callback?.updateButtonNext(true)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    //**********//**********//**********
    // Methods
    //**********//**********//**********

    fun setupViews() {
        ui_onboard_type_layout_neighbour?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_neighbour)
        }
        ui_onboard_type_layout_alone?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_alone)
        }
        ui_onboard_type_layout_assos?.setOnClickListener {
            changeLayoutSelection(ui_onboard_type_layout_assos)
        }

        Logger("Firstname ? $firstname")
        ui_onboard_type_tv_title?.text = String.format(getString(R.string.onboard_type_title),firstname)

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

    fun changeLayoutSelection(selectedLayout:ConstraintLayout?) {
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

        callback?.updateUsertype(userTypeSelected)
        callback?.updateButtonNext(true)
    }

    //**********//**********//**********
    // Companion
    //**********//**********//**********

    companion object {
        @JvmStatic
        fun newInstance(firstname: String?, userTypeSelected: UserTypeSelection) =
                OnboardingTypeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_FIRSTNAME, firstname)
                        putSerializable(ARG_USERTYPE, userTypeSelected)
                    }
                }
    }
}
