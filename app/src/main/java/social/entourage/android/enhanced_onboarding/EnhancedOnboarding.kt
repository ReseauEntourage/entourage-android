package social.entourage.android.enhanced_onboarding

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.MainActivity
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityEnhancedOnboardingLayoutBinding
import social.entourage.android.enhanced_onboarding.fragments.OnboardingActionWishesFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingCategorieFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingCongratsFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingInterestFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingPresentationFragment

class EnhancedOnboarding:BaseActivity() {

    private lateinit var binding: ActivityEnhancedOnboardingLayoutBinding
    private lateinit var viewModel: OnboardingViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnhancedOnboardingLayoutBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)
        viewModel.onboardingFirstStep.observe(this, ::handleOnboardingFirstStep)
        viewModel.onboardingSecondStep.observe(this, ::handleOnboardingSecondStep)
        viewModel.onboardingThirdStep.observe(this, ::handleOnboardingThirdStep)
        viewModel.onboardingFourthStep.observe(this, ::handleOnboardingFourthStep)
        viewModel.onboardingFifthStep.observe(this, ::handleOnboardingFifthStep)
        viewModel.onboardingShouldQuit.observe(this, ::handleOnboardingShouldQuit)
        viewModel.shouldDismissBtnBack.observe(this, ::toggleBtnBack)
        setContentView(binding.root)
    }

    private fun toggleBtnBack(value: Boolean) {
        binding.btnBack.visibility = if (value) View.VISIBLE else View.GONE
    }
    private fun handleOnboardingFirstStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingPresentationFragment()
            supportFragmentManager.beginTransaction().apply {
                // Utilise l'ID directement avec binding.<ID du conteneur>.id
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null) // Optionnel
                commit()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        binding.btnBack.setOnClickListener {
            if(isFromSettingsinterest) {
                isFromSettingsinterest = false
                viewModel.registerAndQuit()
            }else{
                onBackPressed()
            }
        }
        if(isFromSettingsinterest) {
            viewModel.setOnboardingThirdStep(true)
        }else{
            viewModel.setOnboardingFirstStep(true)
        }
    }
    private fun handleOnboardingSecondStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingActionWishesFragment()
            supportFragmentManager.beginTransaction().apply {
                // Utilise l'ID directement avec binding.<ID du conteneur>.id
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null) // Optionnel
                commit()
            }
        }

    }
    private fun handleOnboardingThirdStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingInterestFragment()
            supportFragmentManager.beginTransaction().apply {
                // Utilise l'ID directement avec binding.<ID du conteneur>.id
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null) // Optionnel
                commit()
            }
        }
    }
    private fun handleOnboardingFourthStep(value: Boolean) {
        if (value) {
            if(isFromSettingsinterest) {
                isFromSettingsinterest = false
                viewModel.registerAndQuit()
                return
            }
            val fragment = OnboardingCategorieFragment()
            supportFragmentManager.beginTransaction().apply {
                // Utilise l'ID directement avec binding.<ID du conteneur>.id
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null) // Optionnel
                commit()
            }
        }
    }
    private fun handleOnboardingFifthStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingCongratsFragment()
            supportFragmentManager.beginTransaction().apply {
                // Utilise l'ID directement avec binding.<ID du conteneur>.id
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null) // Optionnel
                commit()
            }
        }
    }

    private fun handleOnboardingShouldQuit(value: Boolean) {
        if (value) {
            MainActivity.shouldLaunchEvent = true
            finish()
        }
    }

    companion object {
        var preference:String = ""
        var isFromSettingsinterest:Boolean = false
    }

}
