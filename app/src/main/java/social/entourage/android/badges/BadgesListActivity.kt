package social.entourage.android.badges

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.databinding.ActivityBadgesListBinding
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import timber.log.Timber

class BadgesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBadgesListBinding
    private var obtainedKeys: List<String> = emptyList()

    companion object {
        const val EXTRA_OBTAINED_KEYS = "obtained_keys"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgesListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.badgeContent)

        obtainedKeys = intent.getStringArrayListExtra(EXTRA_OBTAINED_KEYS) ?: arrayListOf()

        binding.btnBack.setOnClickListener { finish() }
        binding.tvFaqLink.setOnClickListener {
            Timber.d("Badges FAQ link clicked")
            // TODO: open FAQ webview or deeplink
        }

        val demoKeys = listOf("bienvenue", "premier_contact", "fidele_papotages")

        if (BuildConfig.DEBUG) {
            binding.switchBadgesDemo.visibility = View.VISIBLE
            binding.switchBadgesDemo.isChecked = false
            binding.switchBadgesDemo.setOnCheckedChangeListener { _, isChecked ->
                renderBadges(if (isChecked) demoKeys else emptyList())
            }
        }

        renderBadges(obtainedKeys)
    }

    private fun renderBadges(keys: List<String>) {
        val allProgress = buildHardcodedProgress(keys)
        val obtained = allProgress.filter { it.isObtained }
        val inProgress = allProgress.filter { !it.isObtained && it.progress > 0 }
        val notStarted = allProgress.filter { !it.isObtained && it.progress == 0 }
        val total = ALL_BADGE_DEFINITIONS.size

        val isEmptyState = obtained.isEmpty() && inProgress.isEmpty()

        if (isEmptyState) {
            showEmptyState(allProgress, keys)
        } else {
            showBadgesList(obtained, inProgress, notStarted, total, keys)
        }
    }

    private fun showEmptyState(allProgress: List<UserBadgeProgress>, keys: List<String> = emptyList()) {
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
            Timber.d("Badges empty CTA: navigate to first action")
            // TODO: deeplink or navigate to first action
        }

        val items = allProgress.map { BadgeListItem.BadgeItem(it) as BadgeListItem }
        setupAdapter(items, keys)

    }

    private fun showBadgesList(
        obtained: List<UserBadgeProgress>,
        inProgress: List<UserBadgeProgress>,
        notStarted: List<UserBadgeProgress>,
        total: Int,
        keys: List<String>
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

        setupAdapter(items, keys)
    }

    private fun setupAdapter(items: List<BadgeListItem>, currentKeys: List<String> = emptyList()) {
        val adapter = BadgesListAdapter(items) { progress ->
            BadgeDetailBottomSheet.newInstance(progress, currentKeys)
                .show(supportFragmentManager, "badge_detail")
        }
        binding.rvBadges.layoutManager = LinearLayoutManager(this)
        binding.rvBadges.adapter = adapter
    }
}
