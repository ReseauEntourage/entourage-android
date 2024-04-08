package social.entourage.android.language

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.BottomFragmentLanguageFragmentBinding
import social.entourage.android.user.UserPresenter

interface OnLanguageClicked {
    fun onLangChanged(langItem: LanguageItem)
}

class LanguageBottomFragment : BottomSheetDialogFragment(), OnLanguageClicked {
    private lateinit var binding: BottomFragmentLanguageFragmentBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomFragmentLanguageFragmentBinding.inflate(inflater, container, false)

        fillArray()
        handleValidateCLick()
        handleCrossButton()

        adapter = LanguageAdapter(requireContext(), this)
        adapter.setData(languages)
        binding.rvLangue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLangue.adapter = adapter
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setBottomSheetBehavior()
    }

    private fun handleValidateCLick(){
        binding.validate.setOnClickListener {
            this.dismiss()
        }
    }
    private fun setBottomSheetBehavior(){
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    //TODO : REFACTOR WITH LANGUAGE CODE
    private fun fillArray() {
        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(requireContext())
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

        LanguageManager.saveLanguageToPreferences(requireContext(), langCode)
        LanguageManager.setLocale(requireContext(), langCode)
        val id = EntourageApplication.me(requireContext())?.id!!
        userPresenter.updateLanguage(id, langCode)
        var k = 0
        for (_lang in languages) {
            if (_lang.lang == langItem.lang) {
                adapter.onItemChanged(k)
            }
            k++
        }
        binding.titleName.text = getString(R.string.select_language)
        binding.validate.text = getString(R.string.validate)
    }

    private fun handleCrossButton() {
        binding.iconCross.setOnClickListener {
            this.dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().recreate()
    }

    companion object {
        const val TAG = "LanguageBottomFragment"

        fun newInstance(): LanguageBottomFragment {
            return LanguageBottomFragment()
        }
    }
}