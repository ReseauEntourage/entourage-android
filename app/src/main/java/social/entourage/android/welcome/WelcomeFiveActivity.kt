package social.entourage.android.welcome

import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityWelcomeFiveBinding
import social.entourage.android.tools.log.AnalyticsEvents

class WelcomeFiveActivity: BaseActivity() {

    private lateinit var binding:ActivityWelcomeFiveBinding
    val urlToShare = "https://s3-eu-west-1.amazonaws.com/entourage-ressources/store_redirection.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.updateLanguage()
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.closeButton) { view, windowInsets ->
            // Get the insets for the statusBars() type:
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(
                top = insets.top
            )
            // Return the original insets so they aren’t consumed
            windowInsets
        }
    }
    @Deprecated("Deprecated in kt 1.9.0")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this.startActivity(intent)
    }
}