package social.entourage.android.enhanced_onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
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
        viewModel.user = EntourageApplication.me(this)
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
                viewModel.register()
                onBackPressed()
                viewModel.step = viewModel.step - 1
                if(viewModel.step < 1) {
                    viewModel.registerAndQuit()
                }
            }
        }
        if(isFromSettingsinterest) {
            viewModel.setOnboardingThirdStep(true)
        }else{
            //log all steps onboarding
            when (viewModel.step) {
                1 -> {
                    viewModel.setOnboardingFirstStep(true)
                }
                2 -> {
                    viewModel.setOnboardingSecondStep(true)
                }
                3 -> {
                    viewModel.setOnboardingThirdStep(true)
                }
                4 -> {
                    viewModel.setOnboardingFourthStep(true)
                }
                5 -> {
                    viewModel.setOnboardingFifthStep(true)
                }
            }
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
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    companion object {
        var preference:String = ""
        var isFromSettingsinterest:Boolean = false
    }

}
