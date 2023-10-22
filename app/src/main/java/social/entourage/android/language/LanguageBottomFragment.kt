package social.entourage.android.language

import LanguageAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.BottomFragmentLanguageFragmentBinding

interface OnLanguageClicked {
    fun onLangChanged(langItem: LanguageItem)
}

class LanguageBottomFragment : BottomSheetDialogFragment(), OnLanguageClicked {
    private lateinit var binding: BottomFragmentLanguageFragmentBinding
    private var languages: MutableList<LanguageItem> = mutableListOf()
    private lateinit var adapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomFragmentLanguageFragmentBinding.inflate(inflater, container, false)

        fillArray()
        handleCrossButton()

        adapter = LanguageAdapter(requireContext(), this)
        adapter.setData(languages)
        binding.rvLangue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLangue.adapter = adapter

        return binding.root
    }

    private fun fillArray() {
        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(requireContext())

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

        LanguageManager.saveLanguageToPreferences(requireContext(), langCode)
        LanguageManager.setLocale(requireContext(), langCode)

        var k = 0
        for (_lang in languages) {
            if (_lang.lang == langItem.lang) {
                adapter.onItemChanged(k)
            }
            k++
        }
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