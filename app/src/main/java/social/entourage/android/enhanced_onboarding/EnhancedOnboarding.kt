package social.entourage.android.enhanced_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
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
import social.entourage.android.tools.updatePaddingBottomForEdgeToEdge

class EnhancedOnboarding : BaseActivity() {
    private lateinit var binding: ActivityEnhancedOnboardingLayoutBinding
    private lateinit var viewModel: OnboardingViewModel
    private var backCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Permet au layout de se redimensionner quand le clavier apparaît
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        binding = ActivityEnhancedOnboardingLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)
        viewModel.user = EntourageApplication.me(this)

        val userGoal = viewModel.user?.goal
        val isAssoRole = viewModel.user?.partner != null && (viewModel.user?.roles?.contains("Association") == true || viewModel.user?.roles?.contains("Équipe Entourage") == true)

        if ((userGoal != null && userGoal.equals(User.USER_GOAL_ASSO, ignoreCase = true)) || isAssoRole) {
            isAssociationFromSummary = true
            EntourageApplication.get().sharedPreferences.edit()
                .putBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, true)
                .apply()
        }

        setupObservers()

        binding.btnBack.setOnClickListener {
            handleBackNavigation()
        }

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback!!)

        updatePaddingBottomForEdgeToEdge(binding.fragmentContainer)
    }

    private fun setupObservers() {
        viewModel.onboardingFirstStep.observe(this, ::handleOnboardingFirstStep)
        viewModel.onboardingSecondStep.observe(this, ::handleOnboardingSecondStep)
        viewModel.onboardingThirdStep.observe(this, ::handleOnboardingThirdStep)
        viewModel.onboardingFourthStep.observe(this, ::handleOnboardingFourthStep)
        viewModel.onboardingDisponibilityStep.observe(this, ::handleOnboardingDisponibilityStep)
        viewModel.onboardingFifthStep.observe(this, ::handleOnboardingFifthStep)
        viewModel.onboardingShouldQuit.observe(this, ::handleOnboardingShouldQuit)
        viewModel.shouldDismissBtnBack.observe(this, ::toggleBtnBack)
    }

    override fun onResume() {
        super.onResume()
        if (supportFragmentManager.backStackEntryCount == 0) {
            loadInitialStep()
        }
    }

    private fun loadInitialStep() {
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
                else -> viewModel.setOnboardingFirstStep(true)
            }
        }
    }

    private fun handleBackNavigation() {
        val currentFragment = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)

        // Logique spécifique pour sortir du mode Association vers l'étape 3 (intérêts)
        if (isAssociationMode() && currentFragment is EnhancedOnboardingAssoFragment) {
            viewModel.step = 3
            supportFragmentManager.popBackStack()
            return
        }

        if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
            viewModel.registerAndQuit()
        } else {
            if (supportFragmentManager.backStackEntryCount > 1) {
                viewModel.step -= 1
                supportFragmentManager.popBackStack()
            } else {
                viewModel.registerAndQuit()
            }
        }
    }

    private fun toggleBtnBack(shouldShow: Boolean) {
        binding.btnBack.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    private fun isAssociationMode(): Boolean {
        return isAssociationFromSummary ?: EntourageApplication.get().sharedPreferences
            .getBoolean(PREF_IS_ASSOCIATION_FROM_SUMMARY, false)
    }

    private fun isIsolatedPersonMode(): Boolean {
        return viewModel.user?.isUserTypeAlone == true
    }

    private fun handleOnboardingFirstStep(value: Boolean) {
        if (value) replaceFragment(OnboardingPresentationFragment())
    }

    private fun handleOnboardingSecondStep(value: Boolean) {
        if (value) replaceFragment(OnboardingActionWishesFragment())
    }

    private fun handleOnboardingThirdStep(value: Boolean) {
        if (value) replaceFragment(OnboardingInterestFragment())
    }

    private fun handleOnboardingFourthStep(value: Boolean) {
        if (value) {
            if (isAssociationMode()) {
                viewModel.setOnboardingFifthStep(true)
            } else {
                replaceFragment(OnboardingCategorieFragment())
            }
        }
    }

    private fun handleOnboardingDisponibilityStep(value: Boolean) {
        if (value) {
            if (isAssociationMode() || (isIsolatedPersonMode() && !isFromSettingsDisponibility)) {
                viewModel.setOnboardingFifthStep(true)
            } else {
                replaceFragment(OnboardingDisponibilityFragment())
            }
        }
    }

    private fun handleOnboardingFifthStep(value: Boolean) {
        if (value) {
            if (isAssociationMode()) {
                viewModel.register { success ->
                    viewModel.step = 6
                    replaceFragment(EnhancedOnboardingAssoFragment())
                }
            } else {
                replaceFragment(OnboardingCongratsFragment())
            }
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainer.id, fragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun handleOnboardingShouldQuit(value: Boolean) {
        if (value) {
            EntourageApplication.get().sharedPreferences.edit {
                putBoolean("PREF_ENHANCED_ONBOARDING_COMPLETED", true)
            }

            val navigation: OnboardingNavigation = if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie || MainActivity.isFromProfile) {
                isFromSettingsinterest = false
                isFromSettingsDisponibility = false
                isFromSettingsWishes = false
                isFromSettingsActionCategorie = false
                OnboardingNavigation.Profile
            } else {
                when (viewModel.selectedCategory) {
                    "neighborhoods" -> OnboardingNavigation.WelcomeGroup
                    "event" -> OnboardingNavigation.Events
                    "contribution" -> OnboardingNavigation.Donations
                    "both_actions" -> OnboardingNavigation.CreateActionDemand
                    "resources" -> OnboardingNavigation.Quiz
                    "no_event" -> OnboardingNavigation.CreateActionDemand
                    else -> OnboardingNavigation.Home
                }
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("extra_onboarding_navigation", navigation)

            // Nettoyage de la stack pour repartir proprement sur MainActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }

    companion object {
        // Flags de provenance (Settings)
        var isFromSettingsinterest = false
        var isFromSettingsDisponibility = false
        var isFromSettingsWishes = false
        var isFromSettingsActionCategorie = false

        // Mode Association
        var isAssociationFromSummary: Boolean? = null
        const val PREF_IS_ASSOCIATION_FROM_SUMMARY = "is_asso_from_summary"

        // Variables manquantes signalées par ton build
        var preference: String? = null
        var shouldNotDisplayCampain = false
    }
}