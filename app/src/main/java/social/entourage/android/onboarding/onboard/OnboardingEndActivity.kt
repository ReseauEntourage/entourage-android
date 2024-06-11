package social.entourage.android.onboarding.onboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.databinding.ActivityOnboardingEndBinding
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingEndActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityOnboardingEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.uiOnboardingBtEnd.setOnClickListener {
            goNotifChoicePage()
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.Onboard_end)
    }
    fun goNotifChoicePage(){
        val intent = Intent(this, NotificationDemandActivity::class.java)
       this.startActivity(intent)
    }

}