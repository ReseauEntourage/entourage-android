package social.entourage.android.enhanced_onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityEnhancedOnboardingLayoutBinding
import social.entourage.android.enhanced_onboarding.fragments.*
import social.entourage.android.user.UserPresenter
import timber.log.Timber

class EnhancedOnboarding : BaseActivity() {

    private lateinit var binding: ActivityEnhancedOnboardingLayoutBinding
    private lateinit var viewModel: OnboardingViewModel
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnhancedOnboardingLayoutBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(OnboardingViewModel::class.java)
        userPresenter.getUser(viewModel.user?.id ?: 0)
        viewModel.user = EntourageApplication.me(this)
        Timber.wtf("wtf" + viewModel.user?.interests)
        // Observateurs pour chaque étape
        viewModel.onboardingFirstStep.observe(this, ::handleOnboardingFirstStep)
        viewModel.onboardingSecondStep.observe(this, ::handleOnboardingSecondStep)
        viewModel.onboardingThirdStep.observe(this, ::handleOnboardingThirdStep)
        viewModel.onboardingFourthStep.observe(this, ::handleOnboardingFourthStep)
        viewModel.onboardingDisponibilityStep.observe(this, ::handleOnboardingDisponibilityStep)
        viewModel.onboardingFifthStep.observe(this, ::handleOnboardingFifthStep)
        viewModel.onboardingShouldQuit.observe(this, ::handleOnboardingShouldQuit)
        viewModel.shouldDismissBtnBack.observe(this, ::toggleBtnBack)

        userPresenter.user.observe(this, ::updateUser)

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        userPresenter.getUser(viewModel.user?.id ?: 0)
        binding.btnBack.setOnClickListener {
            if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
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
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFromSettingsinterest || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
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
        })

        if (isFromSettingsinterest) {
            viewModel.setOnboardingThirdStep(true)
        }else if (isFromSettingsDisponibility) {
            viewModel.onboardingDisponibilityStep.postValue(true)
        }else if (isFromSettingsWishes) {
            viewModel.setOnboardingSecondStep(true)
        }else if(isFromSettingsActionCategorie){
            viewModel.setOnboardingFourthStep(true)
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

    private fun updateUser(user: User){
        viewModel.user = user

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
            if (isFromSettingsinterest  || isFromSettingsDisponibility || isFromSettingsWishes || isFromSettingsActionCategorie) {
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





    companion object {
        var preference: String = ""
        var isFromSettingsinterest: Boolean = false
        var isFromSettingsDisponibility: Boolean = false
        var isFromSettingsWishes: Boolean = false
        var isFromSettingsActionCategorie: Boolean = false
        var shouldNotDisplayCampain:Boolean = false
    }
}
