package social.entourage.android.small_talks

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.ItemSmallTalkGroupFoundUserBinding

class SmallTalkGroupFoundAdapter(
    private val users: List<User>
) : RecyclerView.Adapter<SmallTalkGroupFoundAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemSmallTalkGroupFoundUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemSmallTalkGroupFoundUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context

        holder.binding.smallTalkGroupFoundName.text = user.displayName ?: user.firstName ?: "???"

        Glide.with(holder.binding.smallTalkGroupFoundAvatar)
            .load(user.avatarURL)
            .placeholder(R.drawable.place_holder_large)
            .into(holder.binding.smallTalkGroupFoundAvatar)

        holder.binding.smallTalkGroupFoundInterests.removeAllViews()
        val formattedInterests = user.getFormattedInterests(context)
        formattedInterests.split(",").map { it.trim() }.forEach { interest ->
            val chip = TextView(context).apply {
                text = interest
                setPadding(24, 8, 24, 8)
                background = ContextCompat.getDrawable(context, R.drawable.chip_background_selector)
                setTextColor(ContextCompat.getColor(context, R.color.orange))
                textSize = 12f
                typeface = ResourcesCompat.getFont(context, R.font.quicksand_bold)
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
            }
            holder.binding.smallTalkGroupFoundInterests.addView(chip)
        }

    }

    override fun getItemCount(): Int = users.size
}
