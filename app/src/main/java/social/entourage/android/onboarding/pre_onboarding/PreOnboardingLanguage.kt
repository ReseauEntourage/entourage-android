package social.entourage.android.onboarding.pre_onboarding

import LanguageAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.databinding.PreOnboardingActivityLayoutBinding
import social.entourage.android.language.LanguageItem
import social.entourage.android.language.LanguageManager
import social.entourage.android.language.OnLanguageClicked
import social.entourage.android.user.UserPresenter

class PreOnboardingLanguage:Activity(), OnLanguageClicked {
    private lateinit var binding: PreOnboardingActivityLayoutBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
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
        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(this)

        languages = mutableListOf(
            LanguageItem("Français", isSelected = LanguageManager.mapLanguageToCode("Français") == currentLanguageCode),
            LanguageItem("English", isSelected = LanguageManager.mapLanguageToCode("English") == currentLanguageCode),
            LanguageItem("Deutsch", isSelected = LanguageManager.mapLanguageToCode("Deutsch") == currentLanguageCode),
            LanguageItem("Español", isSelected = LanguageManager.mapLanguageToCode("Español") == currentLanguageCode),
            LanguageItem("Polski", isSelected = LanguageManager.mapLanguageToCode("Polski") == currentLanguageCode),
            LanguageItem("Українська", isSelected = LanguageManager.mapLanguageToCode("Українська") == currentLanguageCode),
            LanguageItem("Română", isSelected = LanguageManager.mapLanguageToCode("Română") == currentLanguageCode),
            LanguageItem("العربية", isSelected = LanguageManager.mapLanguageToCode("العربية") == currentLanguageCode)
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
    }

    private fun handleNextButton(){
        binding.validate.setOnClickListener {
            startActivity(Intent(this, PreOnboardingStartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}