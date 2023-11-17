package social.entourage.android.homev2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Interest
import social.entourage.android.databinding.HomeV2EventItemLayoutBinding
import social.entourage.android.events.details.feed.FeedActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
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
            holder.binding.layoutItemHomeEvent.setOnClickListener { view ->
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Event_Detail)
                (view.context as? Activity)?.startActivityForResult(
                    Intent(
                        view.context,
                        FeedActivity::class.java
                    ).putExtra(
                        Const.EVENT_ID,
                        event.id
                    ), 0
                )
            }


            event.metadata?.landscapeUrl?.let {
                Glide.with(holder.binding.root.context)
                    .load(Uri.parse(event.metadata.landscapeUrl))
                    .placeholder(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop(), GranularRoundedCorners(45F, 45F, 0F, 0F))
                    .error(R.drawable.ic_event_placeholder)
                    .into(holder.binding.ivEventItem)
            } ?: run {
                Glide.with(holder.binding.root.context)
                    .load(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop(), GranularRoundedCorners(45F, 45F, 0F, 0F))
                    .into(holder.binding.ivEventItem)
            }
            event.title.let {
                holder.binding.tvTitleEventItem.text = it
            }
            event.metadata?.displayAddress.let {
                holder.binding.tvPlaceHomeV2EventItem.text = it
            }

            event.metadata?.startsAt?.let {
                var locale = Locale.getDefault()
                holder.binding.tvDateHomeV2EventItem.text = SimpleDateFormat(
                    holder.itemView.context.getString(R.string.post_date),
                    locale
                ).format(
                    it
                )
            }
            event.interests.let {
                if(it.size > 0){
                    val context = holder.binding.root.context
                    holder.binding.tvTagHomeV2EventItem.text = EventUtils.showTagTranslated(context ,it[0]).replaceFirstChar { char -> char.uppercaseChar() }
                    holder.binding.homeV2ItemEventLayoutTag.visibility = View.VISIBLE
                    if(it[0].equals("other")){
                        holder.binding.tvTagHomeV2EventItem.text = context.getString(R.string.tag_other)
                    }
                    when (it[0]) {
                        Interest.animals -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_animals))
                        Interest.wellBeing -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_wellbeing))
                        Interest.cooking -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_cooking))
                        Interest.culture -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_art))
                        Interest.games -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_games))
                        Interest.nature -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_nature))
                        Interest.sport -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_sport))
                        Interest.activities -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_drawing))
                        Interest.marauding -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_encounters))
                        else -> holder.binding.ivTagHomeV2EventItem.setImageDrawable(context.getDrawable(R.drawable.new_others))
                    }
                }else{
                    holder.binding.homeV2ItemEventLayoutTag.visibility = View.GONE
                }
            }
        }
    }
    class EventViewHolder(val binding: HomeV2EventItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}