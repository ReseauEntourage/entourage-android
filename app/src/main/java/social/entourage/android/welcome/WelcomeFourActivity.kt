package social.entourage.android.welcome

import android.os.Bundle
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeFourBinding
import social.entourage.android.tools.view.WebViewFragment

class WelcomeFourActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeFourBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutWelcomeFourBinding.inflate(layoutInflater)
        binding.mainButton.setOnClickListener {
            val urlString = "https://kahoot.it/challenge/45371e80-fe50-4be5-afec-b37e3d50ede2_1683299255475"
            WebViewFragment.newInstance(urlString, 0, true)
                .show(supportFragmentManager, WebViewFragment.TAG)
        }
        setContentView(binding.root)
    }
}