package social.entourage.android.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.ItemSuggestionBinding

interface SuggestionAdapterListener {
    fun onInfoTap(suggestion: Suggestion)
    fun onCtaTap(suggestion: Suggestion)
}

class SuggestionAdapter(
    private val listener: SuggestionAdapterListener
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    private val items = mutableListOf<Suggestion>()

    fun resetData(list: List<Suggestion>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun appendData(list: List<Suggestion>) {
        val start = items.size
        items.addAll(list)
        notifyItemRangeInserted(start, list.size)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SuggestionViewHolder(
        private val binding: ItemSuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(s: Suggestion) {
            binding.tvSuggestionTitle.text = s.title ?: ""

            val typeLabel = when (s.type) {
                "outing" -> "Événement"
                "group" -> "Groupe"
                "action" -> "Action"
                else -> s.type ?: ""
            }
            binding.tvSuggestionType.text = typeLabel

            s.distance?.let {
                binding.tvSuggestionDistance.text = "${"%.1f".format(it)} km"
                binding.tvSuggestionDistance.visibility = View.VISIBLE
            } ?: run {
                binding.tvSuggestionDistance.visibility = View.GONE
            }

            val firstReason = s.reasons.firstOrNull()
            binding.tvSuggestionReason.text = firstReason?.text ?: ""

            val ctaLabel = when (s.cta) {
                "participate" -> "Participer"
                "join" -> "Rejoindre"
                "help" -> "Aider"
                else -> "Voir"
            }
            binding.btnSuggestionCta.text = ctaLabel

            binding.ivSuggestionInfo.setOnClickListener { listener.onInfoTap(s) }
            binding.btnSuggestionCta.setOnClickListener { listener.onCtaTap(s) }
        }
    }
}
