package social.entourage.android.badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.databinding.FragmentBadgeDetailBinding
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class BadgeDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentBadgeDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_BADGE_KEY = "badge_key"
        private const val ARG_IS_OBTAINED = "is_obtained"
        private const val ARG_PROGRESS = "progress"
        private const val ARG_OBTAINED_DATE = "obtained_date"
        private const val ARG_OBTAINED_KEYS = "obtained_keys"

        fun newInstance(progress: UserBadgeProgress, obtainedKeys: List<String> = emptyList()): BadgeDetailBottomSheet {
            return BadgeDetailBottomSheet().apply {
                arguments = bundleOf(
                    ARG_BADGE_KEY to progress.definition.key.apiKey,
                    ARG_IS_OBTAINED to progress.isObtained,
                    ARG_PROGRESS to progress.progress,
                    ARG_OBTAINED_DATE to progress.obtainedDate,
                    ARG_OBTAINED_KEYS to ArrayList(obtainedKeys)
                )
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
        val obtainedDate = arguments?.getString(ARG_OBTAINED_DATE)
        val obtainedKeys = arguments?.getStringArrayList(ARG_OBTAINED_KEYS) ?: arrayListOf()

        val def = BadgeKey.fromApiKey(apiKey)?.let { key ->
            ALL_BADGE_DEFINITIONS.firstOrNull { it.key == key }
        } ?: return

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__DETAIL)

        val userProgress = UserBadgeProgress(def, isObtained, progress, obtainedDate)
        bindView(userProgress, obtainedKeys.toList())

        binding.btnClose.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__CLOSE)
            dismiss()
        }
        binding.tvSeeAllBadges.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__SEE_ALL)
            val intent = android.content.Intent(requireContext(), BadgesListActivity::class.java).apply {
                putStringArrayListExtra(BadgesListActivity.EXTRA_OBTAINED_KEYS, obtainedKeys)
            }
            startActivity(intent)
            dismiss()
        }
    }

    private fun bindView(up: UserBadgeProgress, obtainedKeys: List<String> = emptyList()) {
        val ctx = requireContext()
        val def = up.definition

        binding.tvDetailEmoji.text = def.emoji
        binding.tvDetailTitle.text = ctx.getString(def.titleRes)
        binding.tvHowItWorks.text = ctx.getString(def.howItWorksRes)
        binding.tvWhatItMeans.text = ctx.getString(def.whatItMeansRes)
        binding.tvMechanism.text = ctx.getString(def.mechanismRes)
        binding.btnCta.text = ctx.getString(def.ctaLabelRes)

        if (up.isObtained) {
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(ctx, R.color.green_light)
            )
            binding.layoutObtained.visibility = View.VISIBLE
            binding.layoutNotObtained.visibility = View.GONE
            val dateText = up.obtainedDate ?: ""
            binding.tvObtainedDate.text = if (dateText.isNotEmpty()) {
                ctx.getString(R.string.badge_obtained_on, dateText)
            } else {
                ctx.getString(R.string.badge_obtained_on, "")
            }
        } else {
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(ctx, R.color.primary_light)
            )
            binding.layoutObtained.visibility = View.GONE
            binding.layoutNotObtained.visibility = View.VISIBLE

            if (up.progress > 0 && def.maxProgress > 1) {
                binding.layoutProgressContainer.visibility = View.VISIBLE
                val pct = (up.progress * 100 / def.maxProgress)
                binding.progressBarDetail.progress = pct
                binding.tvProgressDetail.text = "${up.progress}/${def.maxProgress}"
            } else {
                binding.layoutProgressContainer.visibility = View.GONE
            }
        }

        val mechanismIconRes = if (def.isReversible) R.drawable.ic_close_round else R.drawable.ic_check_green
        binding.ivMechanismIcon.setImageResource(mechanismIconRes)

        binding.btnCta.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__DETAIL__CTA)
            if (up.isObtained) {
                handleCtaClick(def.key)
            } else {
                showUnlockedPopup(def.key, obtainedKeys.toList())
            }
        }
    }

    private fun showUnlockedPopup(key: BadgeKey, obtainedKeys: List<String>) {
        BadgeUnlockedBottomSheet.newInstance(key, obtainedKeys)
            .show(parentFragmentManager, "badge_unlocked")
        dismiss()
    }

    private fun handleCtaClick(key: BadgeKey) {
        when (key) {
            BadgeKey.PREMIER_PAS -> {
                Timber.d("Badge CTA: Premier pas — navigate to first action")
                // TODO: deeplink or navigate to first action flow
            }
            BadgeKey.PREMIER_LIEN -> {
                Timber.d("Badge CTA: Premier lien — navigate to messages/discussions")
                // TODO: deeplink or navigate to discussions
            }
            BadgeKey.DIFFUSEUR_LIENS -> {
                Timber.d("Badge CTA: Diffuseur de liens — navigate to create event")
                // TODO: deeplink or navigate to create outing
            }
            BadgeKey.AS_PAPOTAGE -> {
                Timber.d("Badge CTA: As du papotage — navigate to papotages list")
                // TODO: deeplink or navigate to papotages
            }
            BadgeKey.TISSEUR_LIENS -> {
                Timber.d("Badge CTA: Tisseur de liens — navigate to neighborhoods/groups")
                // TODO: deeplink or navigate to neighborhoods
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
