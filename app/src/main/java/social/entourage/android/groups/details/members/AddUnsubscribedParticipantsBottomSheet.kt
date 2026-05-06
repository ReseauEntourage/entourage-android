package social.entourage.android.groups.details.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.BottomSheetAddUnsubscribedParticipantsBinding

class AddUnsubscribedParticipantsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddUnsubscribedParticipantsBinding? = null
    val binding: BottomSheetAddUnsubscribedParticipantsBinding get() = _binding!!

    private var initialIsolesCount = 0
    private var initialRiverainsCount = 0
    private var initialFemmesIsoleesCount = 0

    private var isolesCount = 0
    private var riverainsCount = 0
    private var femmesIsoleesCount = 0

    var onValidateCounts: ((isolesCount: Int, riverainsCount: Int, femmesIsoleesCount: Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddUnsubscribedParticipantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            initialIsolesCount = it.getInt(ARG_ISOLES_COUNT, 0)
            initialRiverainsCount = it.getInt(ARG_RIVERAINS_COUNT, 0)
            initialFemmesIsoleesCount = it.getInt(ARG_FEMMES_ISOLEES_COUNT, 0)
        }

        isolesCount = initialIsolesCount
        riverainsCount = initialRiverainsCount
        femmesIsoleesCount = initialFemmesIsoleesCount

        updateCounters()

        binding.btnMinusIsoles.setOnClickListener {
            if (isolesCount > 0) {
                isolesCount--
                updateCounters()
            }
        }

        binding.btnPlusIsoles.setOnClickListener {
            isolesCount++
            updateCounters()
        }

        binding.btnMinusRiverains.setOnClickListener {
            if (riverainsCount > 0) {
                riverainsCount--
                updateCounters()
            }
        }

        binding.btnPlusRiverains.setOnClickListener {
            riverainsCount++
            updateCounters()
        }

        binding.btnMinusFemmesIsolees.setOnClickListener {
            if (femmesIsoleesCount > 0) {
                femmesIsoleesCount--
                updateCounters()
            }
        }

        binding.btnPlusFemmesIsolees.setOnClickListener {
            femmesIsoleesCount++
            updateCounters()
        }

        binding.btnValidate.setOnClickListener {
            onValidateCounts?.invoke(isolesCount, riverainsCount, femmesIsoleesCount)
            dismiss()
        }
    }

    private fun updateCounters() {
        binding.tvCountIsoles.text = isolesCount.toString()
        binding.tvCountRiverains.text = riverainsCount.toString()
        binding.tvCountFemmesIsolees.text = femmesIsoleesCount.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ISOLES_COUNT = "ARG_ISOLES_COUNT"
        private const val ARG_RIVERAINS_COUNT = "ARG_RIVERAINS_COUNT"
        private const val ARG_FEMMES_ISOLEES_COUNT = "ARG_FEMMES_ISOLEES_COUNT"

        fun newInstance(isolesCount: Int, riverainsCount: Int, femmesIsoleesCount: Int): AddUnsubscribedParticipantsBottomSheet {
            val args = Bundle()
            args.putInt(ARG_ISOLES_COUNT, isolesCount)
            args.putInt(ARG_RIVERAINS_COUNT, riverainsCount)
            args.putInt(ARG_FEMMES_ISOLEES_COUNT, femmesIsoleesCount)
            val fragment = AddUnsubscribedParticipantsBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}
