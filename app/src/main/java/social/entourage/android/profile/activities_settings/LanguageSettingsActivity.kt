package social.entourage.android.profile.activities_settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.ActivityLanguageSettingsBinding
import social.entourage.android.language.LanguageAdapter
import social.entourage.android.language.LanguageItem
import social.entourage.android.language.LanguageManager
import social.entourage.android.language.OnLanguageClicked
import social.entourage.android.user.UserPresenter


class LanguageSettingsActivity : AppCompatActivity(), OnLanguageClicked {
    private lateinit var binding: ActivityLanguageSettingsBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fillArray()
        handleValidateClick()
        handleCrossButton()
        adapter = LanguageAdapter(this, this)
        adapter.setData(languages)
        binding.rvLangue.layoutManager = LinearLayoutManager(this)
        binding.rvLangue.adapter = adapter

        initTranslationSwitch()
    }

    private fun initTranslationSwitch() {
        val sharedPrefs = getSharedPreferences(getString(R.string.preference_file_key),
            MODE_PRIVATE
        )
        val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
        binding.switchTranslation.isChecked = isTranslatedByDefault
    }

    private fun handleValidateClick() {
        binding.validate.setOnClickListener {
            // Sauvegarde de l'état du switch
            val editor = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).edit()
            editor.putBoolean("translatedByDefault", binding.switchTranslation.isChecked)
            editor.apply()

            // Mettre à jour le profil / logique si nécessaire
            // val id = EntourageApplication.me(this)?.id
            // if (id != null) {
            //     userPresenter.updateTranslationSettings(id, binding.switchTranslation.isChecked)
            // }

            // On ferme l'Activity
            finish()
        }
    }

    private fun fillArray() {
        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(this)
        languages = mutableListOf(
            LanguageItem(
                "Français",
                isSelected = LanguageManager.mapLanguageToCode("Français") == currentLanguageCode
            ),
            LanguageItem(
                "English",
                isSelected = LanguageManager.mapLanguageToCode("English") == currentLanguageCode
            ),
            LanguageItem(
                "العربية",
                isSelected = LanguageManager.mapLanguageToCode("العربية") == currentLanguageCode
            ),
            LanguageItem(
                "Українська",
                isSelected = LanguageManager.mapLanguageToCode("Українська") == currentLanguageCode
            ),
            LanguageItem(
                "Español",
                isSelected = LanguageManager.mapLanguageToCode("Español") == currentLanguageCode
            ),
            LanguageItem(
                "Deutsch",
                isSelected = LanguageManager.mapLanguageToCode("Deutsch") == currentLanguageCode
            ),
            LanguageItem(
                "Română",
                isSelected = LanguageManager.mapLanguageToCode("Română") == currentLanguageCode
            ),
            LanguageItem(
                "Polski",
                isSelected = LanguageManager.mapLanguageToCode("Polski") == currentLanguageCode
            )
        )
    }

    override fun onLangChanged(langItem: LanguageItem) {
        val langCode = LanguageManager.mapLanguageToCode(langItem.lang)
        LanguageManager.saveLanguageToPreferences(this, langCode)
        LanguageManager.setLocale(this, langCode)

        val id = EntourageApplication.me(this)?.id
        if (id != null) {
            userPresenter.updateLanguage(id, langCode)
        }

        languages.forEachIndexed { index, item ->
            if (item.lang == langItem.lang) {
                adapter.onItemChanged(index)
            }
        }

        binding.titleName.text = getString(R.string.select_language)
        binding.validate.text = getString(R.string.validate)
        recreate()
    }

    private fun handleCrossButton() {
        binding.iconCross.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val TAG = "LanguageActivity"
    }
}

