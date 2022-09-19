package social.entourage.android.new_v8.groups.list

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.databinding.NewGroupItemBinding
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import social.entourage.android.tools.log.AnalyticsEvents

class GroupsListAdapter(
    var groupsList: List<Group>,
    var userId: Int?,
    var from: FromScreen?
) : RecyclerView.Adapter<GroupsListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewGroupItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(groupsList[position]) {
                binding.layout.setOnClickListener {
                    handleAnalytics()
                    with(binding.layout.context) {
                        startActivity(
                            Intent(this, FeedActivity::class.java).putExtra(
                                Const.GROUP_ID,
                                groupsList[position].id
                            )
                        )
                    }
                }
                binding.groupName.text = this.name
                this.members?.size?.let {
                    binding.members.text = String.format(
                        holder.itemView.context.getString(if (it > 1) R.string.members_number else R.string.member_number),
                        it
                    )
                }
                binding.futureOutgoingEvents.text = String.format(
                    holder.itemView.context.getString(R.string.future_outgoing_events_number),
                    this.futureOutingsCount
                )
                this.imageUrl?.let {
                    Glide.with(binding.image.context)
                        .load(Uri.parse(it))
                        .apply(RequestOptions().override(90.px, 90.px))
                        .placeholder(R.drawable.ic_group_placeholder)
                        .error(R.drawable.ic_group_placeholder)
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.image.context)
                        .load(R.drawable.ic_group_placeholder)
                        .apply(RequestOptions().override(90.px, 90.px))
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(binding.image)
                }


                val listAdapter = GroupsInterestsListAdapter(this.interests)
                if (userId == groupsList[position].admin?.id) {
                    binding.admin.visibility = View.VISIBLE
                    binding.star.visibility = View.VISIBLE
                } else {
                    binding.admin.visibility = View.GONE
                    binding.star.visibility = View.GONE
                }
                binding.recyclerView.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = listAdapter
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }

    private fun handleAnalytics() {
        when (from) {
            FromScreen.DISCOVER -> AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_DISCOVER_CARD
            )
            FromScreen.DISCOVER_SEARCH -> AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_SEARCH_SEE_RESULT
            )
            FromScreen.MY_GROUPS -> AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_MY_GROUP_CARD
            )
            else -> {}
        }
    }
}


enum class FromScreen {
    MY_GROUPS,
    DISCOVER_SEARCH,
    DISCOVER
}