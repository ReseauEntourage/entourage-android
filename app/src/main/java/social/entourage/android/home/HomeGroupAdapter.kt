package social.entourage.android.home

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
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.HomeV2GroupItemLayoutBinding
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

class HomeGroupAdapter: RecyclerView.Adapter<HomeGroupAdapter.GroupViewHolder>() {

    var groups:MutableList<Group> = mutableListOf()


    fun resetData(groups:MutableList<Group>){
        this.groups.clear()
        this.groups.addAll(groups)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.groups.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = HomeV2GroupItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.binding.layout.setOnClickListener {view ->
            AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Group_Detail)
            group.unreadPostsCount = 0
            holder.binding.cardNewsGroup.visibility = View.INVISIBLE
            (view.context as? Activity)?.startActivityForResult(
                Intent(view.context, GroupFeedActivity::class.java).putExtra(
                    Const.GROUP_ID,
                    group.id
                ), 0
            )
        }

        group.unreadPostsCount.let {
            if (it > 0) {
                var numberOfPost = ""
                if (it > 9) {
                    numberOfPost = "9+"
                }else {
                    numberOfPost = it.toString()
                }
                holder.binding.tvNewsGroup.text = numberOfPost
                holder.binding.cardNewsGroup.visibility = android.view.View.VISIBLE
            } else {
                holder.binding.cardNewsGroup.visibility = android.view.View.GONE
            }
        }

        group.imageUrl?.let {
            Glide.with(holder.binding.root.context)
                .load(Uri.parse(it))
                .placeholder(R.drawable.ic_event_placeholder)
                .transform(CenterCrop(), GranularRoundedCorners(15F, 15F, 0F, 0F))
                .error(R.drawable.ic_event_placeholder)
                .into(holder.binding.ivGroupItem)
        } ?: run {
            Glide.with(holder.binding.root.context)
                .load(R.drawable.ic_event_placeholder)
                .transform(CenterCrop(), GranularRoundedCorners(15F, 0F, 0F, 15F))
                .into(holder.binding.ivGroupItem)
        }
        group.name.let {
            holder.binding.tvGroupItem.text = it
        }
    }
    class GroupViewHolder(val binding: HomeV2GroupItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)


}