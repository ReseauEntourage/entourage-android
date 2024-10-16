package social.entourage.android.homev2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import social.entourage.android.R
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.ActionUtils
import social.entourage.android.databinding.HomeV2ActionItemLayoutBinding
import social.entourage.android.tools.displayDistance
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const

class HomeActionAdapter(private var isContrib:Boolean): RecyclerView.Adapter<HomeActionAdapter.ActionViewHolder>() {
    var actions:MutableList<Action> = mutableListOf()

    fun getIsContrib():Boolean{
        return isContrib
    }
    fun resetData(actions:MutableList<Action>){
        this.actions.clear()
        this.actions.addAll(actions)
        notifyDataSetChanged()

    }
    fun clearList(){
        this.actions.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val binding = HomeV2ActionItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val action = actions[position]
        // Vérification de la langue
        val isArabic = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.binding.root.resources.configuration.locales[0].language == "ar"
        } else {
            holder.binding.root.resources.configuration.locale.language == "ar"
        }

        // Appliquer les propriétés en fonction de la langue pour les TextView
        if (isArabic) {
            holder.binding.tvActionItemTitle.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvActionItemTitle.gravity = Gravity.END
            holder.binding.tvActionItemTitle.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemTitle.textDirection = View.TEXT_DIRECTION_RTL

            holder.binding.tvActionItemSubtitle.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvActionItemSubtitle.gravity = Gravity.END
            holder.binding.tvActionItemSubtitle.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemSubtitle.textDirection = View.TEXT_DIRECTION_RTL

            holder.binding.tvActionItemDistance.layoutDirection = View.LAYOUT_DIRECTION_RTL
            holder.binding.tvActionItemDistance.gravity = Gravity.END
            holder.binding.tvActionItemDistance.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemDistance.textDirection = View.TEXT_DIRECTION_RTL
        } else {
            holder.binding.tvActionItemTitle.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvActionItemTitle.gravity = Gravity.START
            holder.binding.tvActionItemTitle.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemTitle.textDirection = View.TEXT_DIRECTION_LTR

            holder.binding.tvActionItemSubtitle.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvActionItemSubtitle.gravity = Gravity.START
            holder.binding.tvActionItemSubtitle.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemSubtitle.textDirection = View.TEXT_DIRECTION_LTR

            holder.binding.tvActionItemDistance.layoutDirection = View.LAYOUT_DIRECTION_LTR
            holder.binding.tvActionItemDistance.gravity = Gravity.START
            holder.binding.tvActionItemDistance.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            holder.binding.tvActionItemDistance.textDirection = View.TEXT_DIRECTION_LTR
        }

        holder.binding.layout.setOnClickListener { view ->
            if(isContrib){
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Contrib_Detail)
                (view.context as? Activity)?.startActivityForResult(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,false)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                    0
                )
            }else{
                AnalyticsEvents.logEvent(AnalyticsEvents.Action_Home_Demand_Detail)
                (view.context as? Activity)?.startActivityForResult(
                    Intent(view.context, ActionDetailActivity::class.java)
                        .putExtra(Const.ACTION_ID, action.id)
                        .putExtra(Const.ACTION_TITLE,action.title)
                        .putExtra(Const.IS_ACTION_DEMAND,true)
                        .putExtra(Const.IS_ACTION_MINE, action.isMine()),
                    0
                )
            }
        }
        action.author?.avatarURLAsString?.let {
            Glide.with(holder.binding.root.context)
                .load(Uri.parse(it))
                .placeholder(R.drawable.placeholder_action)
                .transform(CenterCrop(), GranularRoundedCorners(15F, 0F, 0F, 15F))
                .error(R.drawable.placeholder_action)
                .into(holder.binding.ivActionItem)
        } ?: run {
            Glide.with(holder.binding.root.context)
                .load(R.drawable.placeholder_action)
                .placeholder(R.drawable.placeholder_action)
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
            if(it != null ){
                holder.binding.tvActionItemSubtitle.text = ActionUtils.showTagTranslated(context ,it)
            }
        }
        action.distance.let {
            val context = holder.binding.root.context
            holder.binding.tvActionItemDistance.text = action.displayDistance(context)
        }
    }

    class ActionViewHolder(val binding: HomeV2ActionItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}