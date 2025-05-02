package social.entourage.android.onboarding.onboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.FragmentOnboardingPhase3Binding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.tools.log.AnalyticsEvents

private const val ARG_ENTOUR = "entour"
private const val ARG_BEENTOUR = "beentour"
private const val ARG_ASSO = "asso"
private const val ARG_ADDRESS = "address"

class OnboardingPhase3Fragment : Fragment(), OnboardingChoosePlaceCallback {

    private var isEntour = false
    private var isBeEntour = false
    private var isAsso = false
    private lateinit var binding:FragmentOnboardingPhase3Binding
    private var address: User.Address? = null
    private var callback:OnboardingStartCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isEntour = it.getBoolean(ARG_ENTOUR)
            isBeEntour = it.getBoolean(ARG_BEENTOUR)
            isAsso = it.getBoolean(ARG_ASSO)
            address = it.getSerializable(ARG_ADDRESS) as? User.Address
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingPhase3Binding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.location.setOnClickListener {
            val fg = OnboardingAddPlaceFragment()
            fg.callback = this
            fg.show(parentFragmentManager,"")
        }

        binding.uiLayoutEntour?.setOnClickListener {
            isEntour = ! isEntour
            changeSelections()
            updateTypes()
        }
        binding.uiLayoutBeentour?.setOnClickListener {

            isBeEntour = ! isBeEntour
            if(isBeEntour){
                EnhancedOnboarding.preference = "contribution"
            }else{
                EnhancedOnboarding.preference = ""
            }
            changeSelections()
            updateTypes()
        }
        binding.uiLayoutAsso?.setOnClickListener {
            isAsso = ! isAsso
            changeSelections()
            updateTypes()
        }

        changeSelections()
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_profile)
        EnhancedOnboarding.shouldNotDisplayCampain = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    private fun changeSelections() {
        changeLayoutAndImage(binding.uiLayoutEntour,binding.uiIvEntourCheck, isEntour)
        changeLayoutAndImage(binding.uiLayoutBeentour,binding.uiIvBeentourCheck, isBeEntour)
        changeLayoutAndImage(binding.uiLayoutAsso,binding.uiIvAssoCheck, isAsso)
    }

    private fun changeLayoutAndImage(layout:ConstraintLayout?,image:ImageView?, isOn:Boolean) {
        if (isOn) {
            layout?.background =  AppCompatResources.getDrawable(requireContext(),R.drawable.new_bg_rounded_button_beige_orange_stroke_radius20)
            image?.setImageDrawable( AppCompatResources.getDrawable(requireContext(),R.drawable.new_bg_selected_filter))
        }
        else {
            layout?.background =  AppCompatResources.getDrawable(requireContext(),R.drawable.new_bg_rounded_button_light_beige_orange_stroke_radius20)
            image?.setImageDrawable( AppCompatResources.getDrawable(requireContext(),R.drawable.new_bg_unselected_filter))
        }
    }

    private fun updateTypes() {
        callback?.updateUsertypeAndAddress(isEntour,isBeEntour,isAsso,address)
    }

    companion object {
        @JvmStatic
        fun newInstance(isEntour:Boolean, isBeentour:Boolean, isAsso:Boolean, address: User.Address?) =
            OnboardingPhase3Fragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_ENTOUR,isEntour)
                    putBoolean(ARG_BEENTOUR,isBeentour)
                    putBoolean(ARG_ASSO,isAsso)
                    putSerializable(ARG_ADDRESS,address)
                }
            }
    }

    override fun updatePlace(address: User.Address?) {
        this.address = address
        binding.location.setText(address?.displayAddress)
        updateTypes()
    }
}