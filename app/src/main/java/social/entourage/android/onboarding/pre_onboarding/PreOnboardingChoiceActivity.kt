package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.onboard.OnboardingStartActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.databinding.ActivityPreOnboardingChoiceBinding

class PreOnboardingChoiceActivity : BaseActivity() {

    private lateinit var binding: ActivityPreOnboardingChoiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreOnboardingChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isFromOnboarding = intent.getBooleanExtra("isFromOnboarding", false)

        if (isFromOnboarding) {
            goLogin()
        }


        binding.uiButtonSignup.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_action_signup)
            val intent = Intent(this, OnboardingStartActivity::class.java)
            //intent.putExtra("fromChoice","signup")
            startActivity(intent)
            finish()
        }
        binding.uiButtonLogin.setOnClickListener {
            goLogin()
        }
        binding.uiButtonAbout.setOnClickListener {
            val chartIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website_url)))
            startActivity(chartIntent)
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_view_choice)
    }


    private fun goLogin() {
        AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_action_signin)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
    }
}