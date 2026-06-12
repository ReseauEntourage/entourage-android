package social.entourage.android.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Suggestion
import social.entourage.android.databinding.HomeSuggestionConnectionItemBinding

class HomeSuggestionConnectionAdapter(
    private val onActionClicked: (Suggestion) -> Unit,
    private val onDismissClicked: (Suggestion) -> Unit
) : RecyclerView.Adapter<HomeSuggestionConnectionAdapter.ViewHolder>() {

    private var suggestion: Suggestion? = null

    fun setSuggestion(s: Suggestion?) {
        suggestion = s
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (suggestion != null) 1 else 0

    override fun getItemViewType(position: Int): Int = 300

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeSuggestionConnectionItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val s = suggestion ?: return
        val userInfo = s.suggestedUserInfo

        holder.binding.tvSuggestionName.text = userInfo?.firstName ?: ""
        holder.binding.tvSuggestionReason.text = s.reason ?: ""

        userInfo?.avatarUrl?.let { url ->
            Glide.with(holder.binding.root.context)
                .load(url)
                .placeholder(R.drawable.placeholder_user)
                .circleCrop()
                .into(holder.binding.ivSuggestionAvatar)
        } ?: holder.binding.ivSuggestionAvatar.setImageResource(R.drawable.placeholder_user)

        holder.binding.btnSuggestionAction.setOnClickListener { onActionClicked(s) }
        holder.binding.btnSuggestionDismiss.setOnClickListener { onDismissClicked(s) }
    }

    class ViewHolder(val binding: HomeSuggestionConnectionItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
