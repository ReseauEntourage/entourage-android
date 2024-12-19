package social.entourage.android.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.TranslationBottomFragmentLayoutBinding
import social.entourage.android.profile.settings.ProfilFullViewModel

class TranslationBottomFragment: BottomSheetDialogFragment() {

    private lateinit var binding:TranslationBottomFragmentLayoutBinding
    private lateinit var profilFullViewModel: ProfilFullViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TranslationBottomFragmentLayoutBinding.inflate(layoutInflater)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        profilFullViewModel = ViewModelProvider(requireActivity()).get(ProfilFullViewModel::class.java)
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
            val editor = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
            editor.putBoolean("translatedByDefault", binding.switchTranslation.isChecked)
            editor.apply()
            profilFullViewModel.updateProfile()
            dismiss()
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