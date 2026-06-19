package social.entourage.android.badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentBadgeUnlockedBinding
import social.entourage.android.tools.log.AnalyticsEvents

class BadgeUnlockedBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentBadgeUnlockedBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_BADGE_KEY = "badge_key"
        private const val ARG_API_BADGES = "api_badges"

        fun newInstance(badgeKey: BadgeKey, apiBadges: List<ApiBadge> = emptyList()): BadgeUnlockedBottomSheet {
            return BadgeUnlockedBottomSheet().apply {
                arguments = bundleOf(
                    ARG_BADGE_KEY to badgeKey.apiKey,
                    ARG_API_BADGES to ArrayList(apiBadges)
                )
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgeUnlockedBinding.inflate(inflater, container, false)
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

        val apiKey = arguments?.getString(ARG_BADGE_KEY) ?: return
        @Suppress("DEPRECATION")
        val apiBadges: ArrayList<ApiBadge> = arguments?.getParcelableArrayList(ARG_API_BADGES) ?: arrayListOf()

        val key = BadgeKey.fromApiKey(apiKey) ?: return
        val def = ALL_BADGE_DEFINITIONS.firstOrNull { it.key == key } ?: return

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__UNLOCKED)

        val firstName = EntourageApplication.me(requireContext())?.firstName ?: ""
        binding.tvBravoTitle.text = getString(R.string.badge_unlocked_bravo, firstName)
        binding.tvUnlockedEmoji.text = def.emoji
        binding.tvBadgeNameHighlight.text = getString(def.titleRes)
        binding.tvUnlockedDescription.text = getString(def.unlockedMessageRes)

        binding.btnClose.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__UNLOCKED__CLOSE)
            dismiss()
        }

        binding.tvContinue.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__UNLOCKED__CONTINUE)
            dismiss()
        }

        binding.btnSeeBadges.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__UNLOCKED__SEE_BADGES)
            val intent = android.content.Intent(requireContext(), BadgesListActivity::class.java).apply {
                putParcelableArrayListExtra(BadgesListActivity.EXTRA_API_BADGES, apiBadges)
            }
            startActivity(intent)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
