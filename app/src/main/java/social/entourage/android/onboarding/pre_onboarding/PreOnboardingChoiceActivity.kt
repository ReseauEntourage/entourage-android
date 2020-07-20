package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_pre_onboarding_choice.*
import social.entourage.android.base.EntourageActivity
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.onboarding.login.LoginNewActivity
import social.entourage.android.onboarding.OnboardingMainActivity

class PreOnboardingChoiceActivity : EntourageActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_onboarding_choice)

        EntourageEvents.logEvent(EntourageEvents.EVENT_VIEW_START_SIGNUPLOGIN)

        val isFromOnboarding = intent.getBooleanExtra("isFromOnboarding",false)

        if (isFromOnboarding) {
            goLogin()
        }

        ui_button_signup?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_START_SIGNUPSTART)
            val intent = Intent(this, OnboardingMainActivity::class.java)
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
    }

    fun goLogin() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_ACTION_START_LOGINSTART)
        val intent = Intent(this, LoginNewActivity::class.java)
        startActivity(intent)
        finish()
    }
}
