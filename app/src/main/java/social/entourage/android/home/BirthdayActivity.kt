package social.entourage.android.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import social.entourage.android.databinding.ActivityBirthdayBinding

class BirthdayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthdayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.iconClose.setOnClickListener {
            finish()
        }
    }
}
