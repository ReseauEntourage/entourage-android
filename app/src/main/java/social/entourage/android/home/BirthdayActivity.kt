package social.entourage.android.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.ActivityBirthdayBinding
import java.util.Calendar

class BirthdayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthdayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHome.setOnClickListener {
            finish()
        }
    }

    private fun saveBirthdayShown() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val prefs = EntourageApplication.get().sharedPreferences
        prefs.edit {
            putInt("PREF_BIRTHDAY_SHOWN_YEAR", currentYear)
        }
    }

}