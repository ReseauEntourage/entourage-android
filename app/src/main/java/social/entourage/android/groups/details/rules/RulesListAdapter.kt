package social.entourage.android.groups.details.rules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Rules
import social.entourage.android.databinding.EventRulesSectionItemBinding
import social.entourage.android.databinding.NewRulesItemBinding

class RulesListAdapter(
    private var rulesList: List<Rules>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SECTION = 1
        private const val TYPE_RULE = 2
    }

    inner class RuleViewHolder(val binding: NewRulesItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class SectionViewHolder(val binding: EventRulesSectionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        val item = rulesList[position]
        return if (item.content.isNullOrBlank()) TYPE_SECTION else TYPE_RULE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SECTION) {
            val binding = EventRulesSectionItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            SectionViewHolder(binding)
        } else {
            val binding = NewRulesItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            RuleViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = rulesList[position]
        when (holder) {
            is RuleViewHolder -> {
                holder.binding.title.text = item.title
                holder.binding.content.text = item.content
                holder.binding.position.text = ""
            }
            is SectionViewHolder -> {
                holder.binding.titleSection.text = item.title
            }
        }
    }

    override fun getItemCount(): Int = rulesList.size
}
