package social.entourage.android.onboarding.onboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.databinding.ActivityOnboardingEndBinding
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingEndActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityOnboardingEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uiOnboardingBtEnd.setOnClickListener {
            goMain()
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_end)
    }

    private fun goMain() {
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
                    .putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, true)
                    .apply()
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, false)
                    .apply()
                sharedPreferences.edit()
                    .putBoolean(EntourageApplication.KEY_MIGRATION_V7_OK,true)
                    .apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}