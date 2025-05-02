package social.entourage.android.enhanced_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityEnhancedOnboardingLayoutBinding
import social.entourage.android.enhanced_onboarding.fragments.*

class EnhancedOnboarding : BaseActivity() {

    private lateinit var binding: ActivityEnhancedOnboardingLayoutBinding
    private lateinit var viewModel: OnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnhancedOnboardingLayoutBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)

        // Observateurs pour chaque étape
        viewModel.onboardingFirstStep.observe(this, ::handleOnboardingFirstStep)
        viewModel.onboardingSecondStep.observe(this, ::handleOnboardingSecondStep)
        viewModel.onboardingThirdStep.observe(this, ::handleOnboardingThirdStep)
        viewModel.onboardingFourthStep.observe(this, ::handleOnboardingFourthStep)
        viewModel.onboardingDisponibilityStep.observe(this, ::handleOnboardingDisponibilityStep)
        viewModel.onboardingFifthStep.observe(this, ::handleOnboardingFifthStep)
        viewModel.onboardingShouldQuit.observe(this, ::handleOnboardingShouldQuit)
        viewModel.shouldDismissBtnBack.observe(this, ::toggleBtnBack)

        viewModel.user = EntourageApplication.me(this)
        setContentView(binding.root)
    }

    private fun toggleBtnBack(value: Boolean) {
        binding.btnBack.visibility = if (value) View.VISIBLE else View.GONE
    }

    // Gestion de l'étape de présentation
    private fun handleOnboardingFirstStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingPresentationFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    // Gestion de l'étape des souhaits d'actions
    private fun handleOnboardingSecondStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingActionWishesFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    // Gestion de l'étape des intérêts
    private fun handleOnboardingThirdStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingInterestFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    // Gestion de l'étape des catégories
    private fun handleOnboardingFourthStep(value: Boolean) {
        if (value) {
            if (isFromSettingsinterest) {
                viewModel.registerAndQuit()
                return
            }
            val fragment = OnboardingCategorieFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    // Gestion de l'étape de disponibilités
    private fun handleOnboardingDisponibilityStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingDisponibilityFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    // Gestion de l'étape finale de félicitations
    private fun handleOnboardingFifthStep(value: Boolean) {
        if (value) {
            val fragment = OnboardingCongratsFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun handleOnboardingShouldQuit(value: Boolean) {
        if (value) {
            when (viewModel.selectedCategory) {
                "both_actions" -> MainActivity.shouldLaunchActionCreation = true
                "event" -> MainActivity.shouldLaunchEvent = true
                "no_event" -> MainActivity.shouldLaunchActionCreation = true
                "resources" -> MainActivity.shouldLaunchQuizz = true
                "neighborhoods" -> MainActivity.shouldLaunchWelcomeGroup = true
            }
            if (isFromSettingsinterest) {
                isFromSettingsinterest = false
                MainActivity.shouldLaunchEvent = false
                MainActivity.shouldLaunchProfile = true
            }
            if (MainActivity.isFromProfile) {
                MainActivity.shouldLaunchEvent = false
                MainActivity.shouldLaunchProfile = true
            }
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.btnBack.setOnClickListener {
            if (isFromSettingsinterest) {
                viewModel.registerAndQuit()
            } else {
                viewModel.register()
                onBackPressed()
                viewModel.step -= 1
                if (viewModel.step < 1) {
                    viewModel.registerAndQuit()
                }
            }
        }

        if (isFromSettingsinterest) {
            viewModel.setOnboardingThirdStep(true)
        } else {
            when (viewModel.step) {
                1 -> viewModel.setOnboardingFirstStep(true)
                2 -> viewModel.setOnboardingSecondStep(true)
                3 -> viewModel.setOnboardingThirdStep(true)
                4 -> viewModel.setOnboardingFourthStep(true)
                5 -> viewModel.onboardingDisponibilityStep.postValue(true) // Étape des disponibilités
                6 -> viewModel.setOnboardingFifthStep(true)
            }
        }
    }

    companion object {
        var preference: String = ""
        var isFromSettingsinterest: Boolean = false
        var shouldNotDisplayCampain:Boolean = false
    }
}
