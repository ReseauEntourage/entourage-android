package social.entourage.android.tools.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.FragmentApiErrorBottomSheetBinding

class ApiErrorBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentApiErrorBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "ApiErrorBottomSheet"
        private const val ARG_CODE = "error_code"

        fun newInstance(code: Int) = ApiErrorBottomSheet().apply {
            arguments = bundleOf(ARG_CODE to code)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentApiErrorBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val code = arguments?.getInt(ARG_CODE) ?: 0
        bindError(code)
        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun bindError(code: Int) {
        binding.tvErrorCode.text = if (code > 0) code.toString() else ""
        binding.tvErrorCode.visibility = if (code > 0) View.VISIBLE else View.GONE

        val (titleRes, descRes) = when (code) {
            400 -> R.string.api_error_title_400 to R.string.api_error_desc_400
            403 -> R.string.api_error_title_403 to R.string.api_error_desc_403
            404 -> R.string.api_error_title_404 to R.string.api_error_desc_404
            408 -> R.string.api_error_title_408 to R.string.api_error_desc_408
            409 -> R.string.api_error_title_409 to R.string.api_error_desc_409
            422 -> R.string.api_error_title_422 to R.string.api_error_desc_422
            429 -> R.string.api_error_title_429 to R.string.api_error_desc_429
            in 500..503 -> R.string.api_error_title_500 to R.string.api_error_desc_500
            in 504..599 -> R.string.api_error_title_503 to R.string.api_error_desc_503
            else -> R.string.api_error_title_default to R.string.api_error_desc_default
        }
        binding.tvErrorTitle.setText(titleRes)
        binding.tvErrorDescription.setText(descRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
