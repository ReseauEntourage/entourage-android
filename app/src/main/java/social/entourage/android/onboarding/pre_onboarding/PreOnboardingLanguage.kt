package social.entourage.android.onboarding.pre_onboarding

import social.entourage.android.language.LanguageAdapter
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.PreOnboardingActivityLayoutBinding
import social.entourage.android.language.LanguageItem
import social.entourage.android.language.LanguageManager
import social.entourage.android.language.OnLanguageClicked
import java.util.Locale

class PreOnboardingLanguage:BaseActivity(), OnLanguageClicked {
    private lateinit var binding: PreOnboardingActivityLayoutBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        val phoneLanguageCode = Locale.getDefault().language
        LanguageManager.setLocale(this, phoneLanguageCode)
        super.onCreate(savedInstanceState)
        binding = PreOnboardingActivityLayoutBinding.inflate(layoutInflater)
        handleNextButton()
        fillArray()
        adapter = LanguageAdapter(this, this)
        adapter.setData(languages)
        binding.rvLangue.layoutManager = LinearLayoutManager(this)
        binding.rvLangue.adapter = adapter
        setContentView(binding.root)
    }
    private fun fillArray() {
        val phoneLanguageCode = Locale.getDefault().language
        LanguageManager.saveLanguageToPreferences(this,phoneLanguageCode)
        val selectedLanguageCode = phoneLanguageCode

        languages = mutableListOf(
            //Français - anglais - arabe - ukrainien - espagnol - allemand - roumain - polonais
            LanguageItem("Français", isSelected = LanguageManager.mapLanguageToCode("Français") == selectedLanguageCode),
            LanguageItem("English", isSelected = LanguageManager.mapLanguageToCode("English") == selectedLanguageCode),
            //LanguageItem("العربية", isSelected = LanguageManager.mapLanguageToCode("العربية") == selectedLanguageCode),
            LanguageItem("Українська", isSelected = LanguageManager.mapLanguageToCode("Українська") == selectedLanguageCode),
            LanguageItem("Español", isSelected = LanguageManager.mapLanguageToCode("Español") == selectedLanguageCode),
            LanguageItem("Deutsch", isSelected = LanguageManager.mapLanguageToCode("Deutsch") == selectedLanguageCode),
            LanguageItem("Română", isSelected = LanguageManager.mapLanguageToCode("Română") == selectedLanguageCode),
            LanguageItem("Polski", isSelected = LanguageManager.mapLanguageToCode("Polski") == selectedLanguageCode)

            )
    }


    override fun onLangChanged(langItem: LanguageItem) {
        val langCode = LanguageManager.mapLanguageToCode(langItem.lang)
        LanguageManager.saveLanguageToPreferences(this, langCode)
        LanguageManager.setLocale(this, langCode)
        var k = 0
        for (_lang in languages) {
            if (_lang.lang == langItem.lang) {
                adapter.onItemChanged(k)
            }
            k++
        }
        adapter.notifyDataSetChanged()
        updateTexts()
    }

    private fun updateTexts() {
        binding.titleName.text = getString(R.string.select_language)
        binding.titleLanguage.text = getString(R.string.welcome_user)
        //Modify button width
        binding.validate.text = getString(R.string.new_next)

    }

    private fun handleNextButton(){
        binding.validate.setOnClickListener {
            startActivity(Intent(this, PreOnboardingStartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}