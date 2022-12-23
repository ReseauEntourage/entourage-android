package social.entourage.android.onboarding.onboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_onboarding_end.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingEndActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_end)

        ui_onboarding_bt_end?.setOnClickListener {
            goMain()
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_end)
    }

    fun goMain() {
        EntourageApplication.get().authenticationController.me?.let { me ->
            OnboardingAPI.getInstance().getUser(me.id) { isOK, userResponse ->
                if (isOK) {
                    if (userResponse != null) {
                        userResponse.user.phone = me.phone
                        EntourageApplication.get().authenticationController.saveUser(userResponse.user)
                    }
                }
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, true).apply()
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, false)
                    .apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}