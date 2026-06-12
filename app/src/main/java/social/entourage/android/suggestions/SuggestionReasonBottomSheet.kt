package social.entourage.android.suggestions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.BottomSheetSuggestionReasonBinding

class SuggestionReasonBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSuggestionReasonBinding? = null
    private val binding get() = _binding!!

    private var suggestion: Suggestion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        suggestion = arguments?.getSerializable(ARG_SUGGESTION) as? Suggestion
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSuggestionReasonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val s = suggestion ?: run { dismiss(); return }

        binding.llReasonsContainer.removeAllViews()
        s.reasons.forEach { reason ->
            val reasonView = TextView(requireContext()).apply {
                text = reason.text ?: ""
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#444444"))
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 8.dpToPx(requireContext())
                layoutParams = params
            }
            binding.llReasonsContainer.addView(reasonView)
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val ARG_SUGGESTION = "suggestion"

        fun newInstance(suggestion: Suggestion): SuggestionReasonBottomSheet {
            return SuggestionReasonBottomSheet().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SUGGESTION, suggestion as java.io.Serializable)
                }
            }
        }
    }
}
