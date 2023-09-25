package social.entourage.android.homev2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.new_contrib_item.view.distance
import kotlinx.android.synthetic.main.new_contrib_item.view.layout_contrib
import social.entourage.android.R
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.Events
import social.entourage.android.databinding.HomeV2ActionItemLayoutBinding
import social.entourage.android.tools.displayDistance
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px

class HomeActionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var actions:MutableList<Action> = mutableListOf()


    fun resetData(actions:MutableList<Action>){
        this.actions.clear()
        this.actions.addAll(actions)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.actions.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = HomeV2ActionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ActionViewHolder) {
            val action = actions[position]
            holder.binding.layout.setOnClickListener { view ->
                (view.context as? Activity)?.startActivityForResult(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,true)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                    0
                )
            }
            action.author?.avatarURLAsString?.let {
                Glide.with(holder.binding.root.context)
                    .load(Uri.parse(it))
                    .placeholder(R.drawable.placeholder_my_event)
                    .transform(CenterCrop(), GranularRoundedCorners(15F, 0F, 0F, 15F))
                    .error(R.drawable.placeholder_my_event)
                    .into(holder.binding.ivActionItem)
            } ?: run {
                Glide.with(holder.binding.root.context)
                    .load(R.drawable.placeholder_my_event)
                    .placeholder(R.drawable.placeholder_my_event)
                    .transform(CenterCrop(), GranularRoundedCorners(15F, 0F, 0F, 15F))
                    .into(holder.binding.ivActionItem)
            }
            action.title.let {
                holder.binding.tvActionItemTitle.text = it
            }
            action.sectionName.let {
                val context = holder.binding.root.context
                val itemDrawable = ActionSection.getIconFromId(it)
                holder.binding.ivActionItemEquipment.setImageDrawable(context.getDrawable(itemDrawable))
                holder.binding.tvActionItemSubtitle.text = MetaDataRepository.getActionSectionNameFromId(it)

            }
            action.distance.let {
                val context = holder.binding.root.context
                holder.binding.tvActionItemDistance.text = action.displayDistance(context)
            }
        }
    }

    class ActionViewHolder(val binding: HomeV2ActionItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}