package social.entourage.android.badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.FragmentBadgeIntroBinding
import social.entourage.android.tools.log.AnalyticsEvents

class BadgeIntroBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentBadgeIntroBinding? = null
    private val binding get() = _binding!!

    var onDiscoverClicked: (() -> Unit)? = null

    companion object {
        private const val ARG_API_BADGES = "api_badges"

        fun newInstance(apiBadges: List<ApiBadge> = emptyList()): BadgeIntroBottomSheet {
            return BadgeIntroBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_API_BADGES, ArrayList(apiBadges))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgeIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("DEPRECATION")
        val apiBadges: ArrayList<ApiBadge> = arguments?.getParcelableArrayList(ARG_API_BADGES) ?: arrayListOf()

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__INTRO)

        binding.tvIntroEmojiRow.text =
            ALL_BADGE_DEFINITIONS.joinToString("   ") { it.emoji }

        binding.btnDiscoverBadges.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__INTRO__DISCOVER)
            val intent = android.content.Intent(requireContext(), BadgesListActivity::class.java).apply {
                putParcelableArrayListExtra(BadgesListActivity.EXTRA_API_BADGES, apiBadges)
            }
            startActivity(intent)
            dismiss()
        }

        binding.tvLater.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__INTRO__LATER)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
