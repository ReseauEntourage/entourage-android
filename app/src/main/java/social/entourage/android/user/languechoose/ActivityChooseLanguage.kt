package social.entourage.android.user.languechoose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.core.view.isVisible
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Summary
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityChooseLanguageLayoutBinding
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.user.UserPresenter
import java.util.Locale

class ActivityChooseLanguage:BaseActivity() {

    private lateinit var binding:ActivityChooseLanguageLayoutBinding
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
//        userPresenter.updateLanguage(id, langue)
//        setLocale(this, langue)
//        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("language", langue)
//        editor.apply()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("languages", langue)
        startActivity(intent)
    }


    fun setLocale(activity: Activity, languageCode: String) {
        val contextWrapper = LanguageContextWrapper.wrap(applicationContext, languageCode)
        @Suppress("DEPRECATION")
        activity.baseContext.resources.updateConfiguration(
            contextWrapper.resources.configuration,
            contextWrapper.resources.displayMetrics
        )
    }


    private fun updateLangue(isChanged:Boolean) {
        if(isChanged){

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finishAffinity()
        }
    }


}


class LanguageContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var context = context
            val config = context.resources.configuration
            val sysLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales[0]
            } else {
                @Suppress("DEPRECATION")
                config.locale
            }

            if (language != "" && sysLocale.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val localeList = android.os.LocaleList(locale)
                    android.os.LocaleList.setDefault(localeList)
                    config.setLocales(localeList)
                } else {
                    @Suppress("DEPRECATION")
                    config.locale = locale
                    @Suppress("DEPRECATION")
                    context.resources.updateConfiguration(config, context.resources.displayMetrics)
                }

                context = context.createConfigurationContext(config)
            }

            return LanguageContextWrapper(context)
        }
    }
}