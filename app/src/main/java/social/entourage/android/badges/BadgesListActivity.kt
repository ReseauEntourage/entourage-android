package social.entourage.android.badges

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.ActivityBadgesListBinding
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import timber.log.Timber

class BadgesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBadgesListBinding
    private var apiBadges: List<ApiBadge> = emptyList()

    companion object {
        const val EXTRA_API_BADGES = "api_badges"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.badgeContent)

        @Suppress("DEPRECATION")
        apiBadges = intent.getParcelableArrayListExtra(EXTRA_API_BADGES) ?: arrayListOf()

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW__BADGES__LIST)

        binding.btnBack.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__LIST__BACK)
            finish()
        }
        binding.tvFaqLink.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__LIST__FAQ)
            BadgeIntroBottomSheet.newInstance(apiBadges)
                .show(supportFragmentManager, "badge_intro")
        }

        renderBadges(apiBadges)
    }

    private fun renderBadges(badges: List<ApiBadge>) {
        val allProgress = buildProgressFromApi(badges)
        val obtained = allProgress.filter { it.isObtained }
        val inProgress = allProgress.filter { !it.isObtained && it.progress > 0 }
        val notStarted = allProgress.filter { !it.isObtained && it.progress == 0 }
        val total = ALL_BADGE_DEFINITIONS.size

        if (obtained.isEmpty() && inProgress.isEmpty()) {
            showEmptyState(allProgress, badges)
        } else {
            showBadgesList(obtained, inProgress, notStarted, total, badges)
        }
    }

    private fun showEmptyState(allProgress: List<UserBadgeProgress>, badges: List<ApiBadge>) {
        binding.emptyStateHeader.root.visibility = View.VISIBLE

        val firstDef = ALL_BADGE_DEFINITIONS.first()
        binding.emptyStateHeader.tvStartBadgeName.text = getString(
            R.string.badges_empty_start_badge,
            "${getString(firstDef.titleRes)} ${firstDef.emoji}"
        )
        binding.emptyStateHeader.tvBadgesEmojiRow.text =
            ALL_BADGE_DEFINITIONS.joinToString("  ") { it.emoji }

        binding.emptyStateHeader.tvBadgesAllTitle.text =
            getString(R.string.badges_all_title, 0, ALL_BADGE_DEFINITIONS.size)

        binding.emptyStateHeader.btnStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__LIST__START)
            val intent = android.content.Intent(this, social.entourage.android.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        setupAdapter(allProgress.map { BadgeListItem.BadgeItem(it) as BadgeListItem }, badges)
    }

    private fun showBadgesList(
        obtained: List<UserBadgeProgress>,
        inProgress: List<UserBadgeProgress>,
        notStarted: List<UserBadgeProgress>,
        total: Int,
        badges: List<ApiBadge>
    ) {
        binding.emptyStateHeader.root.visibility = View.GONE

        val items = mutableListOf<BadgeListItem>()

        if (obtained.isNotEmpty()) {
            items.add(BadgeListItem.Header(getString(R.string.badges_section_obtained, obtained.size, total)))
            obtained.forEach { items.add(BadgeListItem.BadgeItem(it)) }
        }
        if (inProgress.isNotEmpty()) {
            items.add(BadgeListItem.Header(getString(R.string.badges_section_in_progress, inProgress.size, total)))
            inProgress.forEach { items.add(BadgeListItem.BadgeItem(it)) }
        }
        if (notStarted.isNotEmpty()) {
            items.add(BadgeListItem.Header(getString(R.string.badges_section_not_started, notStarted.size, total)))
            notStarted.forEach { items.add(BadgeListItem.BadgeItem(it)) }
        }

        setupAdapter(items, badges)
    }

    private fun setupAdapter(items: List<BadgeListItem>, badges: List<ApiBadge>) {
        val adapter = BadgesListAdapter(items) { progress ->
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION__BADGES__LIST__BADGE_CLICK)
            BadgeDetailBottomSheet.newInstance(progress, badges)
                .show(supportFragmentManager, "badge_detail")
        }
        binding.rvBadges.layoutManager = LinearLayoutManager(this)
        binding.rvBadges.adapter = adapter
    }
}
