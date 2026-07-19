package social.entourage.android.onboarding.onboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import social.entourage.android.databinding.ActivityOnboardingEndBinding
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingEndActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityOnboardingEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            windowInsets
        }

        binding.uiOnboardingBtEnd.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic__Start__Onboarding__Confirmation)
            goNotifChoicePage()
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.View__Onboarding__Confirmation)
    }
    fun goNotifChoicePage() {
        val intent = Intent(this, NotificationDemandActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}