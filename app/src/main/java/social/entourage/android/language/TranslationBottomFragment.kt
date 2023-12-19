package social.entourage.android.language

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        initView()
        return binding.root
    }

    fun initView() {
        binding.validate.setOnClickListener {
            //Stock a boolean to know if the user want to translate all the app by default. On true, it will be translated.
            binding.validate.setOnClickListener {
                val editor = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
                editor.putBoolean("translatedByDefault", true)
                editor.apply()
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