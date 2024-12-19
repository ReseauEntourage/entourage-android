package social.entourage.android.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.TranslationBottomFragmentLayoutBinding

class TranslationBottomFragment: BottomSheetDialogFragment() {

    private lateinit var binding:TranslationBottomFragmentLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TranslationBottomFragmentLayoutBinding.inflate(layoutInflater)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        initView()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setFullScreenBehavior()
    }

    private fun setFullScreenBehavior() {
        val dialog = dialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as? ViewGroup
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    fun initView() {
        val sharedPrefs = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
        binding.switchTranslation.isChecked = isTranslatedByDefault
        binding.validate.setOnClickListener {
            //Stock a boolean to know if the user want to translate all the app by default. On true, it will be translated.
            binding.validate.setOnClickListener {
                val editor = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
                editor.putBoolean("translatedByDefault", binding.switchTranslation.isChecked)
                editor.apply()
                dismiss()
            }
        }
        binding.iconCross.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "TranslationBottomFragment"

        fun newInstance(): TranslationBottomFragment {
            return TranslationBottomFragment()
        }
    }
}