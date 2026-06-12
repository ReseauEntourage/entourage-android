package social.entourage.android.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Zuggestion
import social.entourage.android.databinding.HomeZuggestionConnectionItemBinding

class HomeZuggestionConnectionAdapter(
    private val onActionClicked: (Zuggestion) -> Unit,
    private val onDismissClicked: (Zuggestion) -> Unit
) : RecyclerView.Adapter<HomeZuggestionConnectionAdapter.ViewHolder>() {

    private var suggestion: Zuggestion? = null

    fun setSuggestion(s: Zuggestion?) {
        suggestion = s
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = if (suggestion != null) 1 else 0

    override fun getItemViewType(position: Int): Int = 300

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeZuggestionConnectionItemBinding.inflate(
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

    class ViewHolder(val binding: HomeZuggestionConnectionItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
