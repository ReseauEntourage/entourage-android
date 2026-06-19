package social.entourage.android.badges

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.FragmentBadgeDetailBinding
import social.entourage.android.events.create.CreateEventActivity
import social.entourage.android.tools.log.AnalyticsEvents
import java.text.SimpleDateFormat
import java.util.Locale

class BadgeDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentBadgeDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_BADGE_KEY = "badge_key"
        private const val ARG_IS_OBTAINED = "is_obtained"
        private const val ARG_PROGRESS = "progress"
        private const val ARG_MAX_PROGRESS = "max_progress"
        private const val ARG_OBTAINED_DATE = "obtained_date"
        private const val ARG_API_BADGES = "api_badges"

        fun newInstance(progress: UserBadgeProgress, apiBadges: List<ApiBadge> = emptyList()): BadgeDetailBottomSheet {
            return BadgeDetailBottomSheet().apply {
                arguments = bundleOf(
                    ARG_BADGE_KEY to progress.definition.key.apiKey,
                    ARG_IS_OBTAINED to progress.isObtained,
                    ARG_PROGRESS to progress.progress,
                    ARG_MAX_PROGRESS to progress.maxProgress,
                    ARG_OBTAINED_DATE to progress.obtainedDate,
                    ARG_API_BADGES to ArrayList(apiBadges)
                )
            }
        }

        private fun formatDate(iso8601: String?): String {
            if (iso8601.isNullOrEmpty()) return ""
            return try {
                val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                val outputFmt = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val date = inputFmt.parse(iso8601) ?: return ""
                outputFmt.format(date)
            } catch (e: Exception) {
                iso8601.substringBefore("T")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBadgeDetailBinding.inflate(inflater, container, false)
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
        val isObtained = arguments?.getBoolean(ARG_IS_OBTAINED, false) ?: false
        val progress = arguments?.getInt(ARG_PROGRESS, 0) ?: 0
        val maxProgress = arguments?.getInt(ARG_MAX_PROGRESS, 1) ?: 1
        val obtainedDate = arguments?.getString(ARG_OBTAINED_DATE)
        @Suppress("DEPRECATION")
        val apiBadges: ArrayList<ApiBadge> = arguments?.getParcelableArrayList(ARG_API_BADGES) ?: arrayListOf()

        val def = BadgeKey.fromApiKey(apiKey)?.let { key ->
            ALL_BADGE_DEFINITIONS.firstOrNull { it.key == key }
        } ?: return

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__DETAIL)

        val userProgress = UserBadgeProgress(def, isObtained, progress, maxProgress, obtainedDate)
        bindView(userProgress, apiBadges.toList())

        binding.btnClose.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__CLOSE)
            dismiss()
        }
        binding.tvSeeAllBadges.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__SEE_ALL)
            openBadgesList(apiBadges.toList())
        }
    }

    private fun bindView(up: UserBadgeProgress, apiBadges: List<ApiBadge>) {
        val ctx = requireContext()
        val def = up.definition

        binding.tvDetailEmoji.text = def.emoji
        binding.tvDetailTitle.text = ctx.getString(def.titleRes)
        binding.tvHowItWorks.text = ctx.getString(def.howItWorksRes)
        binding.tvWhatItMeans.text = ctx.getString(def.whatItMeansRes)
        binding.tvMechanism.text = ctx.getString(def.mechanismRes)

        if (up.isObtained) {
            binding.btnCta.text = ctx.getString(R.string.badge_cta_see_my_badges)
            binding.tvSeeAllBadges.visibility = View.GONE
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(ctx, R.color.green_light)
            )
            binding.layoutObtained.visibility = View.VISIBLE
            binding.layoutNotObtained.visibility = View.GONE
            val formattedDate = formatDate(up.obtainedDate)
            binding.tvObtainedDate.text = if (formattedDate.isNotEmpty()) {
                ctx.getString(R.string.badge_obtained_on, formattedDate)
            } else {
                ctx.getString(R.string.badge_obtained_on, "")
            }
        } else {
            binding.btnCta.text = ctx.getString(def.ctaLabelRes)
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(ctx, R.color.primary_light)
            )
            binding.layoutObtained.visibility = View.GONE
            binding.layoutNotObtained.visibility = View.VISIBLE

            if (up.progress > 0 && up.maxProgress > 1) {
                binding.layoutProgressContainer.visibility = View.VISIBLE
                val pct = (up.progress * 100 / up.maxProgress)
                binding.progressBarDetail.progress = pct
                binding.tvProgressDetail.text = "${up.progress}/${up.maxProgress}"
            } else {
                binding.layoutProgressContainer.visibility = View.GONE
            }
        }

        val mechanismIconRes = if (def.isReversible) R.drawable.ic_close_round else R.drawable.ic_check_green
        binding.ivMechanismIcon.setImageResource(mechanismIconRes)

        binding.btnCta.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__CTA)
            if (up.isObtained) {
                openBadgesList(apiBadges)
            } else {
                navigateForBadge(def.key)
            }
        }
    }

    private fun openBadgesList(apiBadges: List<ApiBadge>) {
        val intent = Intent(requireContext(), BadgesListActivity::class.java).apply {
            putParcelableArrayListExtra(BadgesListActivity.EXTRA_API_BADGES, ArrayList(apiBadges))
        }
        startActivity(intent)
        dismiss()
    }

    private fun navigateForBadge(key: BadgeKey) {
        when (key) {
            BadgeKey.PREMIER_PAS -> navigateToMainTab("home")
            BadgeKey.PREMIER_LIEN -> navigateToMainTab("messages")
            BadgeKey.AS_PAPOTAGE -> navigateToMainTab("events")
            BadgeKey.DIFFUSEUR_LIENS -> {
                startActivity(Intent(requireContext(), CreateEventActivity::class.java))
                dismiss()
            }
            BadgeKey.TISSEUR_LIENS -> navigateToMainTab("groups")
        }
    }

    private fun navigateToMainTab(tab: String) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_BADGE_NAV_TAB, tab)
        }
        startActivity(intent)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
