package social.entourage.android.discussions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.BottomSheetDiscussionsFilterBinding

/**
 * EN-9489 : filtre multi-sélection de l'onglet Discussions (individuelles / événements /
 * bonnes ondes), ouvert depuis l'icône curseurs du header. Remplace l'ancienne ligne de
 * chips à sélection unique de DiscussionsMainFragment.
 */
class DiscussionsFilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDiscussionsFilterBinding? = null
    private val binding get() = _binding!!

    var onApply: (Set<FilterMode>) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDiscussionsFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selected = arguments?.getStringArrayList(ARG_SELECTED).orEmpty()
        binding.checkboxPrivate.isChecked = FilterMode.PRIVATE.name in selected
        binding.checkboxOutings.isChecked = FilterMode.OUTINGS.name in selected
        binding.checkboxSmalltalks.isChecked = FilterMode.SMALLTALKS.name in selected

        binding.btnCross.setOnClickListener { dismiss() }

        binding.buttonReset.setOnClickListener {
            binding.checkboxPrivate.isChecked = false
            binding.checkboxOutings.isChecked = false
            binding.checkboxSmalltalks.isChecked = false
        }

        binding.buttonApply.setOnClickListener {
            val result = mutableSetOf<FilterMode>()
            if (binding.checkboxPrivate.isChecked) result.add(FilterMode.PRIVATE)
            if (binding.checkboxOutings.isChecked) result.add(FilterMode.OUTINGS)
            if (binding.checkboxSmalltalks.isChecked) result.add(FilterMode.SMALLTALKS)
            onApply(result)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DiscussionsFilterBottomSheet"
        private const val ARG_SELECTED = "selected"

        fun newInstance(selected: Set<FilterMode>): DiscussionsFilterBottomSheet {
            return DiscussionsFilterBottomSheet().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_SELECTED, ArrayList(selected.map { it.name }))
                }
            }
        }
    }
}
