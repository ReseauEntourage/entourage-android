package social.entourage.android.events.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Status
import social.entourage.android.databinding.LayoutItemMyEventBinding
import social.entourage.android.events.EventsFragment
import social.entourage.android.events.details.feed.FeedActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.calculateIfEventPassed
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.Locale

class MyEventRVAdapter(var context: Context) :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var events:MutableList<Events> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventItemViewHolder {
        val binding = LayoutItemMyEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyEventItemViewHolder(binding)
    }

    fun resetData(events:MutableList<Events>){
        this.events.addAll(events)

        notifyDataSetChanged()
    }

    fun clearList(){
        this.events.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyEventItemViewHolder) {
            holder.bind(events[position])

        }
    }

    override fun getItemCount() = events.size

    inner class MyEventItemViewHolder(private val binding: LayoutItemMyEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Events) {
            binding.entireCardLayoutItem.setOnClickListener { view ->
                EventsFragment.isFromDetails = true
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
            binding.titleMyEvent.text = event.title
            event.metadata?.startsAt?.let {
                var locale = LanguageManager.getLocaleFromPreferences(context)
                binding.dateMyEvent.text = SimpleDateFormat(
                    itemView.context.getString(R.string.post_date),
                    locale
                ).format(
                    it
                )
            }
            binding.placeMyEvent.text = event.metadata?.displayAddress
            event.metadata?.landscapeUrl?.let {
                Glide.with(binding.root.context)
                    .load(Uri.parse(event.metadata.landscapeUrl))
                    .placeholder(R.drawable.placeholder_my_event)
                    .error(R.drawable.placeholder_my_event)
                    .transform(CenterCrop(), GranularRoundedCorners(14F, 14F, 0F, 0F))
                    .into(binding.imageItemMyEvent)
            } ?: run {
                Glide.with(binding.root.context)
                    .load(R.drawable.placeholder_my_event)
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.imageItemMyEvent)
            }

            binding.titleMyEvent.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (event.status == Status.CLOSED) R.color.grey else R.color.black)
            )

            if(event.calculateIfEventPassed()){
                binding.titleMyEvent.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.grey)
                )
                //TODO reduce margin left
            }else{
                binding.titleMyEvent.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.black)
                )
            }
        }
    }
}