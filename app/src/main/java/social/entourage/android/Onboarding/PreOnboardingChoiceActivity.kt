package social.entourage.android.Onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pre_onboarding_choice.*
import social.entourage.android.EntourageActivity
import social.entourage.android.R
import social.entourage.android.authentication.login.LoginActivity

class PreOnboardingChoiceActivity : EntourageActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_onboarding_choice)

        ui_button_signup?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fromChoice","signup")
            startActivity(intent)
            finish()
        }
        ui_button_login?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("fromChoice","Login")
            startActivity(intent)
            finish()
        }
        ui_button_about?.setOnClickListener {
            showWebView(getString(R.string.website_url))
        }
    }
}
