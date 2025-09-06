package social.entourage.android.user.languechoose

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.CountDownTimer
import androidx.core.view.isVisible
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Summary
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityChooseLanguageLayoutBinding
import social.entourage.android.user.UserPresenter
import java.util.Locale

class ActivityChooseLanguage:BaseActivity() {

    private lateinit var binding: ActivityChooseLanguageLayoutBinding
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseLanguageLayoutBinding.inflate(layoutInflater)
        userPresenter.isLanguageChanged.observe(this, ::updateLangue)
        handleAllButtons()
        setContentView(binding.root)
    }

    fun handleAllButtons(){
        val id = EntourageApplication.me(this)?.id!!
        binding.tvFr.setOnClickListener { changeLanguage(id, "fr") }
        binding.tvEn.setOnClickListener { changeLanguage(id, "en") }
        binding.tvDe.setOnClickListener { changeLanguage(id, "de") }
        binding.tvPl.setOnClickListener { changeLanguage(id, "pl")  }
        binding.tvRo.setOnClickListener { changeLanguage(id, "ro")  }
        binding.tvUk.setOnClickListener { changeLanguage(id, "uk")  }
        binding.tvAr.setOnClickListener { changeLanguage(id, "ar")  }
    }

    fun changeLanguage(id:Int, langue:String){
        userPresenter.updateLanguage(id, langue)
        setLocale(this, langue)
        
    }


    fun setLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    private fun updateLangue(isChanged:Boolean) {
        this.finish()
    }


}