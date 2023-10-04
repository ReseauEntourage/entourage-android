package social.entourage.android.events.list

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Status
import social.entourage.android.databinding.LayoutSectionHeaderEventV2Binding
import social.entourage.android.databinding.LayoutSectionHeaderMyEventBinding
import social.entourage.android.databinding.NewEventItemBinding
import social.entourage.android.events.details.feed.FeedActivity
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.Locale

class AllEventAdapterV2(var userId: Int?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_EVENT = 1
    var events:MutableList<Events> = mutableListOf()
    override fun getItemCount(): Int {
        return events.size // 2 pour les en-têtes
    }

    fun resetData(events:MutableList<Events>){
        this.events.addAll(events)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.events.clear()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_EVENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         val binding = NewEventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
         if (holder is EventViewHolder) {
            if(position < events.size){
                val event = events[position]
                holder.binding.layout.setOnClickListener { view ->
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
                holder.binding.eventName.text = event.title
                event.metadata?.startsAt?.let {
                    holder.binding.date.text = SimpleDateFormat(
                        holder.itemView.context.getString(R.string.event_date_time),
                        Locale.FRANCE
                    ).format(
                        it
                    )
                }
                holder.binding.location.text = event.metadata?.displayAddress
                holder.binding.participants.text = event.membersCount.toString()

                val participantsCount = event.membersCount ?: 0

                holder.binding.participants.text =
                    holder.binding.root.context.resources.getQuantityString(
                        R.plurals.number_of_people,
                        participantsCount,
                        participantsCount
                    )

                event.metadata?.landscapeUrl?.let {
                    Glide.with(holder.binding.root.context)
                        .load(Uri.parse(event.metadata.landscapeUrl))
                        .placeholder(R.drawable.ic_event_placeholder)
                        .error(R.drawable.ic_event_placeholder)
                        .apply(RequestOptions().override(90.px, 90.px))
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(holder.binding.image)
                } ?: run {
                    Glide.with(holder.binding.root.context)
                        .load(R.drawable.ic_event_placeholder)
                        .apply(RequestOptions().override(90.px, 90.px))
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(holder.binding.image)
                }

                holder.binding.star.isVisible = event.author?.userID == userId
                holder.binding.admin.isVisible = event.author?.userID == userId
                holder.binding.canceled.isVisible = event.status == Status.CLOSED
                holder.binding.ivCanceled.isVisible = event.status == Status.CLOSED
                holder.binding.eventName.setTextColor(
                    ContextCompat.getColor(
                        holder.binding.root.context,
                        if (event.status == Status.CLOSED) R.color.grey else R.color.black)
                )

                if(event.calculateIfEventPassed()){
                    holder.binding.eventName.setTextColor(
                        ContextCompat.getColor(
                            holder.binding.root.context,
                            R.color.grey)
                    )
                    holder.binding.blackLayout.visibility = View.VISIBLE
                }else{
                    holder.binding.eventName.setTextColor(
                        ContextCompat.getColor(
                            holder.binding.root.context,
                            R.color.black)
                    )
                    holder.binding.blackLayout.visibility = View.GONE
                }
            }
        }
    }
    class EventViewHolder(val binding: NewEventItemBinding) : RecyclerView.ViewHolder(binding.root)
}