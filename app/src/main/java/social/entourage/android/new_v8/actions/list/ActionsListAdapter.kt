package social.entourage.android.new_v8.actions.list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.new_contrib_item.view.*
import kotlinx.android.synthetic.main.new_demand_item.view.*
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.new_v8.actions.detail.ActionDetailActivity
import social.entourage.android.new_v8.models.*
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px

class ActionsListAdapter(
    var groupsList: List<Action>,
    var userId: Int?,
    private val isContrib:Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val TYPE_CONTRIB = 0
    val TYPE_DEMAND = 1

    override fun getItemViewType(position: Int): Int {
        return  if (isContrib) TYPE_CONTRIB else TYPE_DEMAND
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_CONTRIB) {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.new_contrib_item, parent, false)
            return ViewHolderContrib(view)
        }
        else {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.new_demand_item, parent, false)
            return ViewHolderDemand(view)
        }
    }

    inner class ViewHolderContrib(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(action: Action) {
            binding.layout_contrib.setOnClickListener { view ->
                view.context.startActivity(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,false)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine())
                )
            }

            binding.name.text = action.title
            binding.distance.text = "À xx km de moi"
            binding.location.text = action.metadata?.displayAddress
            binding.date.text = action.dateFormattedString(binding.context)
            action.imageUrl?.let {
                Glide.with(binding.image.context)
                    .load(it)
                    .error(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            } ?: run {
                Glide.with(binding.image.context)
                    .load(R.drawable.ic_placeholder_action)
                    .apply(RequestOptions().override(90.px, 90.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            }
        }
    }

    inner class ViewHolderDemand(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(action: Action) {
            binding.layout_demand.setOnClickListener {
                binding.layout_demand.context.startActivity(
                    Intent(binding.layout_demand.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,true)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine())
                )
            }

            binding.demand_title.text = action.title
            binding.demand_section_name.text = MetaDataRepository.getActionSectionNameFromId(action.sectionName)
            binding.demand_section_pic.setImageDrawable(binding.context.getDrawable(ActionSection.getIconFromId(action.sectionName)))
            binding.demand_username.text = action.author?.userName
            binding.demand_location.text = action.metadata?.displayAddress
            binding.demand_date.text = action.dateFormattedString(binding.context)

            action.author?.avatarURLAsString?.let { avatarURL ->
                Glide.with(binding.demand_pict.context)
                    .load(avatarURL)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demand_pict)
            } ?: run {
                Glide.with(binding.demand_pict.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demand_pict)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_CONTRIB) {
            (holder as? ViewHolderContrib)?.bind(groupsList[position])
        }
        else {
            (holder as? ViewHolderDemand)?.bind(groupsList[position])
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}
