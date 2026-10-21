package social.entourage.android.profile

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Stats
import social.entourage.android.api.model.Summary
import social.entourage.android.databinding.ActivityLayoutProfileBinding
import social.entourage.android.databinding.ItemImpactLineBinding
import social.entourage.android.language.LanguageManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object ProfileImpactHelper {

    private const val LOW_ACTIVITY_MAX_TYPES = 2

    private data class ImpactLine(val label: Int, val emoji: String)

    fun bindOwnImpact(context: Context, binding: ActivityLayoutProfileBinding, summary: Summary) {
        val lines = listOf(
            ImpactLine(R.string.impact_line_events, "🎉") to (summary.pastOutingParticipationsCount ?: 0),
            ImpactLine(R.string.impact_line_actions, "🤝") to (summary.actionCreationsCount ?: 0),
            ImpactLine(R.string.impact_line_groups, "🏘️") to (summary.neighborhoodMessagesCount ?: 0),
            ImpactLine(R.string.impact_line_conversations, "💬") to (summary.conversationMembersCount ?: 0)
        ).filter { it.second > 0 }

        binding.tvImpactTitle.text = context.getString(R.string.impact_title_mine)
        if (lines.isEmpty()) {
            binding.layoutImpactFilled.visibility = View.GONE
            binding.layoutImpactEmpty.visibility = View.VISIBLE
            binding.buttonImpactDiscover.setOnClickListener {
                context.startActivity(
                    Intent(context, MainActivity::class.java).putExtra("isFromWidget", true)
                )
            }
            return
        }
        binding.layoutImpactEmpty.visibility = View.GONE
        binding.tvImpactSectionTitle.visibility = View.VISIBLE
        binding.tvImpactHeroLabel.text = context.getString(R.string.impact_hero_label_mine)
        binding.tvImpactHeroSubtitle.text = context.getString(
            if (lines.size <= LOW_ACTIVITY_MAX_TYPES) R.string.impact_hero_subtitle_low
            else R.string.impact_hero_subtitle
        )
        showLines(context, binding, lines)
    }

    fun bindPublicImpact(context: Context, binding: ActivityLayoutProfileBinding, stats: Stats?) {
        val lines = listOf(
            ImpactLine(R.string.impact_other_events, "🎉") to (stats?.outingsCount ?: 0),
            ImpactLine(R.string.impact_other_actions, "🤝") to ((stats?.contribCreationCount ?: 0) + (stats?.askCreationCount ?: 0)),
            ImpactLine(R.string.impact_other_groups, "🏘️") to (stats?.neighborhoodsCount ?: 0)
        ).filter { it.second > 0 }

        binding.tvImpactTitle.text = context.getString(R.string.impact_title_other)
        binding.layoutImpactEmpty.visibility = View.GONE
        if (lines.isEmpty()) {
            binding.layoutAchievement.visibility = View.GONE
            return
        }
        binding.layoutAchievement.visibility = View.VISIBLE
        binding.tvImpactSectionTitle.visibility = View.GONE
        binding.tvImpactHeroLabel.text = context.getString(R.string.impact_hero_label_other)
        binding.tvImpactHeroSubtitle.visibility = View.GONE
        showLines(context, binding, lines)
    }

    private fun showLines(
        context: Context,
        binding: ActivityLayoutProfileBinding,
        lines: List<Pair<ImpactLine, Int>>
    ) {
        binding.tvImpactHero.text = lines.sumOf { it.second }.toString()
        binding.layoutImpactLines.removeAllViews()
        lines.forEach { (impactLine, count) ->
            val line = ItemImpactLineBinding.inflate(LayoutInflater.from(context), binding.layoutImpactLines, false)
            line.tvImpactLineCount.text = count.toString()
            line.tvImpactLineLabel.text = context.getString(impactLine.label)
            line.tvImpactLineIcon.text = impactLine.emoji
            binding.layoutImpactLines.addView(line.root)
        }
        binding.layoutImpactFilled.visibility = View.VISIBLE
    }

    fun bindMemberSince(context: Context, binding: ActivityLayoutProfileBinding, createdAt: Date?) {
        val text = createdAt?.let { memberSinceText(context, it) }
        binding.tvMemberSince.text = text
        binding.tvMemberSince.visibility = if (text == null) View.GONE else View.VISIBLE
    }

    private fun memberSinceText(context: Context, createdAt: Date): String {
        val locale = LanguageManager.getLocaleFromPreferences(context)
        val monthYear = SimpleDateFormat("LLLL yyyy", locale).format(createdAt)
        val start = Calendar.getInstance().apply { time = createdAt }
        val now = Calendar.getInstance()
        var months = (now.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
                now.get(Calendar.MONTH) - start.get(Calendar.MONTH)
        if (now.get(Calendar.DAY_OF_MONTH) < start.get(Calendar.DAY_OF_MONTH)) months--
        val years = months / 12
        return when {
            years >= 1 -> context.resources.getQuantityString(R.plurals.member_since_years, years, years, monthYear)
            months >= 1 -> context.resources.getQuantityString(R.plurals.member_since_months, months, months, monthYear)
            else -> context.getString(R.string.member_since_this_month, monthYear)
        }
    }
}
