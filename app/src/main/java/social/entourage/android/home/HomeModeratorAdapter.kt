package social.entourage.android.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.R
import social.entourage.android.api.model.Summary
import social.entourage.android.databinding.HomeModeratorItemBinding

class HomeModeratorAdapter(private val onModeratorClick: (Int) -> Unit) : RecyclerView.Adapter<HomeModeratorAdapter.ViewHolder>() {

    private var summary: Summary? = null

    fun updateSummary(summary: Summary) {
        this.summary = summary
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeModeratorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        summary?.moderator?.let { moderator ->
            with(holder.binding) {
                // Title
                tvTitle.text = root.context.getString(
                    R.string.home_moderator_title,
                    moderator.displayName ?: ""
                )

                // Message
                tvMessage.text = root.context.getString(R.string.home_moderator_message)

                // Avatar
                Glide.with(root)
                    .load(moderator.imageURL)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .transform(CircleCrop())
                    .into(ivModerator)

                // Click listener
                root.setOnClickListener {
                    moderator.id?.let { id -> onModeratorClick(id) }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (summary?.moderator != null) 1 else 0
    }

    class ViewHolder(val binding: HomeModeratorItemBinding) : RecyclerView.ViewHolder(binding.root)
}
