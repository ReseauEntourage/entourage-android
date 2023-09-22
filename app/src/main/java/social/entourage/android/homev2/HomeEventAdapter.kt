package social.entourage.android.homev2

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Interest
import social.entourage.android.databinding.HomeV2EventItemLayoutBinding
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.Locale

class HomeEventAdapter:RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var events:MutableList<Events> = mutableListOf()


    fun resetData(events:MutableList<Events>){
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.events.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = HomeV2EventItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EventViewHolder) {
            val event = events[position]
            event.metadata?.landscapeUrl?.let {
                Glide.with(holder.binding.root.context)
                    .load(Uri.parse(event.metadata.landscapeUrl))
                    .placeholder(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop(), GranularRoundedCorners(15F, 15F, 0F, 0F))
                    .error(R.drawable.ic_event_placeholder)
                    .into(holder.binding.ivEventItem)
            } ?: run {
                Glide.with(holder.binding.root.context)
                    .load(R.drawable.ic_event_placeholder)
                    .into(holder.binding.ivEventItem)
            }
            event.title.let {
                holder.binding.tvTitleEventItem.text = it
            }
            event.metadata?.displayAddress.let {
                holder.binding.tvPlaceHomeV2EventItem.text = it
            }
            event.metadata?.startsAt?.let {
                holder.binding.tvDateHomeV2EventItem.text = SimpleDateFormat(
                    holder.itemView.context.getString(R.string.event_date_time),
                    Locale.FRANCE
                ).format(
                    it
                )
            }
            event.interests.let {
                if(it.size > 0){
                    val context = holder.binding.root.context
                    holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(Interest.getIconFromId(it[0])))
                    holder.binding.tvTagHomeV2EventItem.text = it[0]
                }
            }
        }
    }
    class EventViewHolder(val binding: HomeV2EventItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}