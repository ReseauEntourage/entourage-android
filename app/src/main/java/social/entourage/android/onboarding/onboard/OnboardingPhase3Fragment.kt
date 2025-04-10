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
    private var isBothEntour = false
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
        initOnboardingViews()
        binding.location.setOnClickListener {
            val fg = OnboardingAddPlaceFragment()
            fg.callback = this
            fg.show(parentFragmentManager,"")
        }

        //if click on asso , reset all other values
        isBeEntour = false
        isEntour = false
        updateBackgroundLayoutEntour(false)
        updateBackgroundLayoutBeenEntour(false)
        updateBackgroundLayoutBothEntour(false)
        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_profile)
        EnhancedOnboarding.shouldNotDisplayCampain = true
    }

    private fun initOnboardingViews() {
        // Initialisation des textes depuis les strings.xml
        binding.uiLayoutEntour.tvInterestTitleFromRight.text = getString(R.string.option_surround)
        binding.uiLayoutEntour.tvInterestSubTitleFromRight.text = getString(R.string.option_surround_desc)
        binding.uiLayoutEntour.ivInterestIcon.setImageResource(R.drawable.onboarding_entour) // Ajouter l'icône spécifique
        binding.uiLayoutEntour.tvInterestTitle.visibility = View.GONE

        binding.uiLayoutBeentour.tvInterestTitleFromRight.text = getString(R.string.option_supported)
        binding.uiLayoutBeentour.tvInterestSubTitleFromRight.text = getString(R.string.option_supported_desc)
        binding.uiLayoutBeentour.ivInterestIcon.setImageResource(R.drawable.onboarding_been_entour) // Ajouter l'icône spécifique
        binding.uiLayoutBeentour.tvInterestTitle.visibility = View.GONE

        binding.uiLayoutEntourBeentourBoth.tvInterestTitleFromRight.text = getString(R.string.option_both)
        binding.uiLayoutEntourBeentourBoth.tvInterestSubTitleFromRight.text = getString(R.string.option_both_desc)
        binding.uiLayoutEntourBeentourBoth.ivInterestIcon.setImageResource(R.drawable.onboarding_both_entour_been_entour) // Ajouter l'icône spécifique
        binding.uiLayoutEntourBeentourBoth.tvInterestTitle.visibility = View.GONE

        // Appliquer les styles initiaux
        updateBackgroundLayoutEntour(isEntour)
        updateBackgroundLayoutBeenEntour(isBeEntour)
        updateBackgroundLayoutBothEntour(isBothEntour)

        // Gestion des clics pour basculer les états
        binding.uiLayoutEntour.view.setOnClickListener {
            isEntour = !isEntour
            isBeEntour = false
            isBothEntour = false
            updateBackgroundLayoutEntour(isEntour)
            updateBackgroundLayoutBeenEntour(false)
            updateBackgroundLayoutBothEntour(false)
            updateTypes()
        }

        binding.uiLayoutBeentour.view.setOnClickListener {
            isBeEntour = !isBeEntour
            isEntour = false
            isBothEntour = false
            EnhancedOnboarding.preference = if (isBeEntour) "contribution" else ""
            updateBackgroundLayoutBeenEntour(isBeEntour)
            updateBackgroundLayoutEntour(false)
            updateBackgroundLayoutBothEntour(false)
            updateTypes()
        }

        binding.uiLayoutEntourBeentourBoth.view.setOnClickListener {
            isBothEntour = !isBothEntour
            isEntour = false
            isBeEntour = false
            updateBackgroundLayoutBothEntour(isBothEntour)
            updateBackgroundLayoutBeenEntour(false)
            updateBackgroundLayoutEntour(false)
            updateTypes()
        }

        binding.uiLayoutAsso?.setOnClickListener {
            isAsso = !isAsso
            if(isAsso){
                binding.uiIvAssoCheck.setImageResource(R.drawable.new_bg_selected_filter)
            }else{
                binding.uiIvAssoCheck.setImageResource(R.drawable.new_bg_unselected_filter)
            }
            updateTypes()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = (activity as? OnboardingStartCallback)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    private fun updateBackgroundLayoutEntour(isSelected: Boolean) {
        val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
        binding.uiLayoutEntour.view.setBackgroundResource(backgroundResource)
        binding.uiLayoutEntour.ivInterestCheck.setImageResource(if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck)
    }
    private fun updateBackgroundLayoutBeenEntour(isSelected: Boolean) {
        val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
        binding.uiLayoutBeentour.view.setBackgroundResource(backgroundResource)
        binding.uiLayoutBeentour.ivInterestCheck.setImageResource(if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck)
    }
    // Fonction pour mettre à jour l'affichage de "Les deux"
    private fun updateBackgroundLayoutBothEntour(isSelected: Boolean) {
        val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
        binding.uiLayoutEntourBeentourBoth.view.setBackgroundResource(backgroundResource)
        binding.uiLayoutEntourBeentourBoth.ivInterestCheck.setImageResource(
            if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck
        )
    }

    private fun updateTypes() {
        callback?.updateUsertypeAndAddress(isEntour,isBeEntour,isBothEntour,isAsso,address)

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