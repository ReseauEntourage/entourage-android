package social.entourage.android.welcome

import android.content.Intent
import android.os.Bundle
import social.entourage.android.MainActivity
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeFourBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.WebViewFragment

class WelcomeFourActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeFourBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.updateLanguage()
        super.onCreate(savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.View_WelcomeOfferHelp_Day8)

        binding = ActivityLayoutWelcomeFourBinding.inflate(layoutInflater)
        binding.mainButton.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_WelcomeOfferHelp_Day8)
            val urlString = "https://kahoot.it/challenge/45371e80-fe50-4be5-afec-b37e3d50ede2_1687252158038"
            WebViewFragment.newInstance(urlString, 0, true)
                .show(supportFragmentManager, WebViewFragment.TAG)
        }
        binding.closeButton.setOnClickListener {
            this.finish()
        }
        setContentView(binding.root)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this.startActivity(intent)
    }
}