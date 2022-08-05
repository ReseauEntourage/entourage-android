package social.entourage.android.new_v8.groups.details.feed

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.NewEventItemLayoutBinding
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.px
import java.text.SimpleDateFormat
import java.util.*

class GroupEventsAdapter(
    var eventsList: List<Events>,
) : RecyclerView.Adapter<GroupEventsAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewEventItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewEventItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(eventsList[position]) {
                binding.name.text = title
                binding.address.text = metadata?.placeName
                metadata?.startsAt?.let {
                    binding.date.text = SimpleDateFormat(
                        holder.itemView.context.getString(R.string.event_date),
                        Locale.FRANCE
                    ).format(
                        it
                    )
                }
                Glide.with(holder.itemView.context)
                    .load(Uri.parse(this.metadata?.landscapeThumbnailUrl))
                    .transform(GranularRoundedCorners(20F, 20F, 0F, 0F))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .into(holder.binding.image)
            }
        }
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}