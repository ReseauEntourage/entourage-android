package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_pre_onboarding_choice.*
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.onboard.OnboardingStartActivity
import social.entourage.android.tools.log.AnalyticsEvents

class PreOnboardingChoiceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_onboarding_choice)
        Log.wtf("wtf", "jsuis là")
        val isFromOnboarding = intent.getBooleanExtra("isFromOnboarding",false)

        if (isFromOnboarding) {
            goLogin()
        }

        ui_button_signup?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_action_signup)
            val intent = Intent(this, OnboardingStartActivity::class.java)
            //intent.putExtra("fromChoice","signup")
            startActivity(intent)
            finish()
        }
        ui_button_login?.setOnClickListener {
            goLogin()
        }
        ui_button_about?.setOnClickListener {
            showWebView(getString(R.string.website_url))
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_view_choice)
    }

    fun goLogin() {
        AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_action_signin)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
