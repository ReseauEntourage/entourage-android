package social.entourage.android.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.HomeSuggestionNextStepItemBinding

class HomeSuggestionNextStepAdapter(
    private val onActionClicked: (Suggestion) -> Unit,
    private val onDismissClicked: (Suggestion) -> Unit
) : RecyclerView.Adapter<HomeSuggestionNextStepAdapter.ViewHolder>() {

    private var suggestion: Suggestion? = null

    fun setSuggestion(s: Suggestion?) {
        suggestion = s
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (suggestion != null) 1 else 0

    override fun getItemViewType(position: Int): Int = 301

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeSuggestionNextStepItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val s = suggestion ?: return
        val context = holder.binding.root.context

        val entourageInfo = s.suggestedEntourageInfo
        holder.binding.tvNextStepText.text = entourageInfo?.title ?: s.reason ?: ""
        holder.binding.tvNextStepReason.text = s.reason ?: ""

        val ctaText = when (s.suggestedAction) {
            "join_event" -> context.getString(R.string.suggestion_next_step_cta_join_event)
            "join_group" -> context.getString(R.string.suggestion_next_step_cta_join_group)
            "write_group" -> context.getString(R.string.suggestion_next_step_cta_write_group)
            "say_hello" -> context.getString(R.string.suggestion_next_step_cta_say_hello)
            "create_action" -> context.getString(R.string.suggestion_next_step_cta_create_action)
            "welcome_member" -> context.getString(R.string.suggestion_next_step_cta_welcome_member)
            "create_event" -> context.getString(R.string.suggestion_next_step_cta_create_event)
            else -> context.getString(R.string.suggestion_next_step_cta_join_event)
        }
        holder.binding.btnNextStepAction.text = ctaText

        holder.binding.btnNextStepAction.setOnClickListener { onActionClicked(s) }
        holder.binding.btnNextStepDismiss.setOnClickListener { onDismissClicked(s) }
    }

    class ViewHolder(val binding: HomeSuggestionNextStepItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
