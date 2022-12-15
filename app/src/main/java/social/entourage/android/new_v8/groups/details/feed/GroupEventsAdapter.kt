package social.entourage.android.new_v8.groups.details.feed

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.NewEventItemLayoutBinding
import social.entourage.android.new_v8.events.details.feed.FeedActivity
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.Const
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
        holder.binding.name.text = eventsList[position].title
        holder.binding.address.text = eventsList[position].metadata?.placeName
        eventsList[position].metadata?.startsAt?.let {
            holder.binding.date.text = SimpleDateFormat(
                holder.itemView.context.getString(R.string.event_date),
                Locale.FRANCE
            ).format(
                it
            )
        }
        eventsList[position].metadata?.landscapeThumbnailUrl?.let {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(it))
                .transform(CenterCrop(), GranularRoundedCorners(20F, 20F, 0F, 0F))
                .placeholder(R.drawable.placeholder_user)
                .into(holder.binding.image)
        }
        holder.binding.layout.setOnClickListener { view->
            (view.context as? Activity)?.startActivityForResult(
                Intent(
                    view.context,
                    FeedActivity::class.java
                ).putExtra(
                    Const.EVENT_ID,
                    eventsList[position].id
                ), 0
            )
        }
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }
}