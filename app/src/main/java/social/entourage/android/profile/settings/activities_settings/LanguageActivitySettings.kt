package social.entourage.android.language

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.BottomFragmentLanguageFragmentBinding
import social.entourage.android.user.UserPresenter


class LanguageActivitySettings : AppCompatActivity(), OnLanguageClicked {
    private lateinit var binding: BottomFragmentLanguageFragmentBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomFragmentLanguageFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillArray()
        handleValidateClick()
        handleCrossButton()

        adapter = LanguageAdapter(this, this)
        adapter.setData(languages)
        binding.rvLangue.layoutManager = LinearLayoutManager(this)
        binding.rvLangue.adapter = adapter
    }

    private fun handleValidateClick() {
        binding.validate.setOnClickListener {
            // On ferme l'Activity
            finish()
        }
    }

    private fun fillArray() {
        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(this)
        languages = mutableListOf(
            LanguageItem("Français", isSelected = LanguageManager.mapLanguageToCode("Français") == currentLanguageCode),
            LanguageItem("English", isSelected = LanguageManager.mapLanguageToCode("English") == currentLanguageCode),
            LanguageItem("العربية", isSelected = LanguageManager.mapLanguageToCode("العربية") == currentLanguageCode),
            LanguageItem("Українська", isSelected = LanguageManager.mapLanguageToCode("Українська") == currentLanguageCode),
            LanguageItem("Español", isSelected = LanguageManager.mapLanguageToCode("Español") == currentLanguageCode),
            LanguageItem("Deutsch", isSelected = LanguageManager.mapLanguageToCode("Deutsch") == currentLanguageCode),
            LanguageItem("Română", isSelected = LanguageManager.mapLanguageToCode("Română") == currentLanguageCode),
            LanguageItem("Polski", isSelected = LanguageManager.mapLanguageToCode("Polski") == currentLanguageCode)
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

        // Mettre à jour la sélection dans l'adapter
        languages.forEachIndexed { index, item ->
            if (item.lang == langItem.lang) {
                adapter.onItemChanged(index)
            }
        }

        // Mise à jour de l'interface après le changement de langue
        binding.titleName.text = getString(R.string.select_language)
        binding.validate.text = getString(R.string.validate)

        // On recrée l'Activity pour appliquer les changements de langue
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
