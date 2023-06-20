package social.entourage.android.welcome

import android.content.Intent
import android.os.Bundle
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityWelcomeFiveBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.WebViewFragment

class WelcomeFiveActivity: BaseActivity() {

    private lateinit var binding:ActivityWelcomeFiveBinding
    val urlToShare = "https://s3-eu-west-1.amazonaws.com/entourage-ressources/store_redirection.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shareTitle = getString(R.string.welcome_five_share_title)
        binding = ActivityWelcomeFiveBinding.inflate(layoutInflater)
        AnalyticsEvents.logEvent(AnalyticsEvents.View_WelcomeOfferHelp_Day11)
        binding.mainButton.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_WelcomeOfferHelp_Day11)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + urlToShare)

            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.welcome_five_share_title)))
        }
        binding.closeButton.setOnClickListener {
            this.finish()
        }
        setContentView(binding.root)
    }
}