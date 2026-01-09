package social.entourage.android.enhanced_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityEnhancedOnboardingLayoutBinding
import social.entourage.android.enhanced_onboarding.fragments.EnhancedOnboardingAssoFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingActionWishesFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingCategorieFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingCongratsFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingDisponibilityFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingInterestFragment
import social.entourage.android.enhanced_onboarding.fragments.OnboardingPresentationFragment

class EnhancedOnboarding : BaseActivity() {

    private lateinit var binding: ActivityEnhancedOnboardingLayoutBinding
    private lateinit var viewModel: OnboardingViewModel
    private var backCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        binding = ActivityEnhancedOnboardingLayoutBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)
        viewModel.user = EntourageApplication.me(this)

        val userGoal = viewModel.user?.goal
        if (userGoal != null && userGoal.equals(User.USER_GOAL_ASSO, ignoreCase = true)) {
            isAssociationFromSummary = true
            EntourageApplication.get().sharedPreferences.edit().putBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, true).apply()
        }

        viewModel.onboardingFirstStep.observe(this, ::handleOnboardingFirstStep)
        viewModel.onboardingSecondStep.observe(this, ::handleOnboardingSecondStep)
        viewModel.onboardingThirdStep.observe(this, ::handleOnboardingThirdStep)
        viewModel.onboardingFourthStep.observe(this, ::handleOnboardingFourthStep)
        viewModel.onboardingDisponibilityStep.observe(this, ::handleOnboardingDisponibilityStep)
        viewModel.onboardingFifthStep.observe(this, ::handleOnboardingFifthStep)
        viewModel.onboardingShouldQuit.observe(this, ::handleOnboardingShouldQuit)
        viewModel.shouldDismissBtnBack.observe(this, ::toggleBtnBack)

        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            handleBackNavigation()
        }

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)
    }

    override fun onResume() {
        super.onResume()

        if (isFromSettingsinterest) {
            viewModel.setOnboardingThirdStep(true)
        } else if (isFromSettingsDisponibility) {
            viewModel.onboardingDisponibilityStep.postValue(true)
        } else if (isFromSettingsWishes) {
            viewModel.setOnboardingSecondStep(true)
        } else if (isFromSettingsActionCategorie) {
            viewModel.setOnboardingFourthStep(true)
        } else {
            when (viewModel.step) {
                1 -> viewModel.setOnboardingFirstStep(true)
                2 -> viewModel.setOnboardingSecondStep(true)
                3 -> viewModel.setOnboardingThirdStep(true)
                4 -> viewModel.setOnboardingFourthStep(true)
                5 -> viewModel.onboardingDisponibilityStep.postValue(true)
                6 -> viewModel.setOnboardingFifthStep(true)
            }
        }
    }

    override fun onDestroy() {
        backCallback?.remove()
        backCallback = null
        super.onDestroy()
    }

    private fun handleBackNavigation() {
        if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
            viewModel.registerAndQuit()
        } else {
            viewModel.register()
            viewModel.step -= 1
            if (viewModel.step < 1) {
                viewModel.registerAndQuit()
            } else {
                supportFragmentManager.popBackStack()
            }
        }
    }

    private fun updateUser(user: User) {
        viewModel.user = user
    }

    private fun toggleBtnBack(value: Boolean) {
        binding.btnBack.visibility = if (value) View.VISIBLE else View.GONE
    }

    private fun isAssociationMode(): Boolean {
        val local = isAssociationFromSummary
        if (local != null) return local
        return EntourageApplication.get().sharedPreferences.getBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, false)
    }

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

    private fun handleOnboardingFourthStep(value: Boolean) {
        if (value) {
            if (isAssociationMode()) {
                viewModel.setOnboardingFifthStep(true)
                return
            }
            if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes) {
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

    private fun handleOnboardingDisponibilityStep(value: Boolean) {
        if (value) {
            if (isAssociationMode()) {
                viewModel.setOnboardingFifthStep(true)
                return
            }
            val fragment = OnboardingDisponibilityFragment()
            supportFragmentManager.beginTransaction().apply {
                replace(binding.fragmentContainer.id, fragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun handleOnboardingFifthStep(value: Boolean) {
        if (value) {
            if (isAssociationMode()) {
                val fragment = EnhancedOnboardingAssoFragment()
                supportFragmentManager.beginTransaction().apply {
                    replace(binding.fragmentContainer.id, fragment)
                    addToBackStack(null)
                    commit()
                }
            } else {
                val fragment = OnboardingCongratsFragment()
                supportFragmentManager.beginTransaction().apply {
                    replace(binding.fragmentContainer.id, fragment)
                    addToBackStack(null)
                    commit()
                }
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
            if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    companion object {
        var preference: String = ""
        var isFromSettingsinterest: Boolean = false
        var isFromSettingsDisponibility: Boolean = false
        var isFromSettingsWishes: Boolean = false
        var isFromSettingsActionCategorie: Boolean = false
        var shouldNotDisplayCampain: Boolean = false
        var isAssociationFromSummary: Boolean? = null
        private const val PREF_IS_ASSOCIATION_FROM_SUMMARY = "PREF_IS_ASSOCIATION_FROM_SUMMARY"
    }
}
