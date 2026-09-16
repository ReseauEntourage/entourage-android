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
        fun newInstance(): BadgeIntroBottomSheet = BadgeIntroBottomSheet()
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

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__INTRO)

        val iconViews = listOf(
            binding.ivBadgeIcon0,
            binding.ivBadgeIcon1,
            binding.ivBadgeIcon2,
            binding.ivBadgeIcon3,
            binding.ivBadgeIcon4
        )
        ALL_BADGE_DEFINITIONS.forEachIndexed { index, def ->
            if (index < iconViews.size) iconViews[index].loadBadgeSvg(def.svgRes)
        }

        binding.btnDiscoverBadges.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__INTRO__DISCOVER)
            startActivity(android.content.Intent(requireContext(), BadgesListActivity::class.java))
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
