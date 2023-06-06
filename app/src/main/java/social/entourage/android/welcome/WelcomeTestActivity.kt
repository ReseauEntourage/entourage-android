package social.entourage.android.welcome

import android.content.Intent
import android.os.Bundle
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.WelcomeTestLayoutBinding

class WelcomeTestActivity:BaseActivity() {

    private lateinit var binding:WelcomeTestLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WelcomeTestLayoutBinding.inflate(layoutInflater)
        setButtons()
        setContentView(binding.root)

    }

    private fun setButtons(){
        binding.welcomeOne.setOnClickListener {
            val intent = Intent(this, WelcomeOneActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.welcomeTwo.setOnClickListener {
            val intent = Intent(this, WelcomeTwoActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.welcomeThree.setOnClickListener {
            val intent = Intent(this, WelcomeThreeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}