package social.entourage.android.onboarding.pre_onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityPreOnboardingChoiceBinding
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.onboard.OnboardingStartActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

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

        setImage()

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
        updatePaddingTopForEdgeToEdge(binding.logos)
    }

    private fun goLogin() {
        AnalyticsEvents.logEvent(AnalyticsEvents.PreOnboard_action_signin)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setImage() {
        binding.uiLogo2.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.logo_entourage_rvb_horizontal))

        val width = 500  // Remplace par la largeur souhaitée en pixels
        val height = 250 // Remplace par la hauteur souhaitée en pixels
        val params = ConstraintLayout.LayoutParams(width, height)
        val marginStart = 70  // En pixels
        val marginTop = 55   // En pixels
        params.marginStart = marginStart
        params.topMargin = marginTop
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        binding.uiLogo2.scaleType = ImageView.ScaleType.FIT_START
        binding.uiLogo2.layoutParams = params

        // Vérifie si la langue est en mode RTL (comme l'arabe)
        val isRtl = resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL
        if (isRtl) {
            // Applique un miroir horizontal à l'image
            binding.imageMosaic.scaleX = -1f
        } else {
            // Réinitialise la transformation si ce n'est pas en RTL
            binding.imageMosaic.scaleX = 1f
        }
    }
}