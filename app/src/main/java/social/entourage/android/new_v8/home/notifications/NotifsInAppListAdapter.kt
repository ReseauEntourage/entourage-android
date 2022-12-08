package social.entourage.android.new_v8.home.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import kotlinx.android.synthetic.main.new_notif_detail.view.*
import kotlinx.android.synthetic.main.new_notif_detail.view.title
import social.entourage.android.R
import social.entourage.android.new_v8.models.*


interface OnItemClick {
    fun onItemClick(notif: NotifInApp, position:Int)
}

class NotifsInAppListAdapter(
    var notifs: List<NotifInApp>,
    private var onItemClickListener: OnItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_notif_detail, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(notif: NotifInApp, position:Int) {

            binding.card.setOnClickListener {
                onItemClickListener.onItemClick(notif,position)
            }
            binding.title.text = notif.content
            binding.date.text = notif.dateFormattedString(binding.context)

            notif.imageUrl?.let {
                Glide.with(binding.image_card.context)
                    .load(it)
                    .error(R.drawable.ic_new_placeholder_notif)
                    .transform(CircleCrop())
                    .into(binding.image_card)
            } ?: run {
                Glide.with(binding.image_card.context)
                    .load(R.drawable.ic_new_placeholder_notif)
                    .transform(CircleCrop())
                    .into(binding.image_card)
            }

            if (notif.isRead()) {
                binding.card.setBackgroundColor(binding.card.context.resources.getColor(R.color.white))
                binding.separator.setBackgroundColor(binding.separator.context.resources.getColor(R.color.beige))
            }
            else {
                binding.card.setBackgroundColor(binding.card.context.resources.getColor(R.color.beige))
                binding.separator.setBackgroundColor(binding.separator.context.resources.getColor(R.color.white))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ViewHolder)?.bind(notifs[position], position)
    }

    override fun getItemCount(): Int {
        return notifs.size
    }
}