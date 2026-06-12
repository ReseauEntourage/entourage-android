package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.ItemHomeSuggestionBinding

interface HomeSuggestionListener {
    fun onInfoTap(suggestion: Suggestion)
    fun onSeeAllTap()
    fun onCtaTap(suggestion: Suggestion)
}

class HomeSuggestionAdapter(
    private val listener: HomeSuggestionListener
) : RecyclerView.Adapter<HomeSuggestionAdapter.SuggestionViewHolder>() {

    private var suggestion: Suggestion? = null

    fun setSuggestion(s: Suggestion?) {
        suggestion = s
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (suggestion != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemHomeSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val s = suggestion ?: return
        holder.bind(s)
    }

    inner class SuggestionViewHolder(
        private val binding: ItemHomeSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(s: Suggestion) {
            binding.tvSuggestionTitle.text = s.title ?: ""

            // Type badge
            val typeLabel = when (s.type) {
                "outing" -> "Événement"
                "group" -> "Groupe"
                "action" -> "Action"
                else -> s.type ?: ""
            }
            binding.tvSuggestionType.text = typeLabel

            // First reason
            val firstReason = s.reasons.firstOrNull()
            if (firstReason != null) {
                binding.tvSuggestionReason.text = firstReason.text ?: ""
                binding.llSuggestionReason.visibility = View.VISIBLE
            } else {
                binding.llSuggestionReason.visibility = View.GONE
            }

            // CTA label
            val ctaLabel = when (s.cta) {
                "participate" -> "Participer"
                "join" -> "Rejoindre"
                "help" -> "Aider"
                else -> "Voir"
            }
            binding.btnSuggestionCta.text = ctaLabel

            binding.ivSuggestionInfo.setOnClickListener { listener.onInfoTap(s) }
            binding.btnSuggestionCta.setOnClickListener { listener.onCtaTap(s) }
            binding.tvSuggestionSeeAll.setOnClickListener { listener.onSeeAllTap() }
        }
    }
}
