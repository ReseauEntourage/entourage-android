package social.entourage.android.homev2

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import social.entourage.android.R
import social.entourage.android.api.model.Help
import social.entourage.android.api.model.Summary
import social.entourage.android.databinding.HomeV2HelpItemLayoutBinding

interface OnHomeV2HelpItemClickListener{
    fun onItemClick(position:Int, moderatorId:Int)
}

class HomeHelpAdapter(val callback:OnHomeV2HelpItemClickListener): RecyclerView.Adapter<HomeHelpAdapter.HelpViewHolder>() {

    private var helps:MutableList<Help> = mutableListOf()
    private var summary:Summary? = null
    fun resetData(helps:MutableList<Help>,summary: Summary){
        this.helps.clear()
        this.helps.addAll(helps)
        this.summary = summary
        notifyDataSetChanged()

    }

    fun clearList(){
        this.helps.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val binding = HomeV2HelpItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return helps.size
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val help = helps[position]
        holder.binding.root.setOnClickListener {
            callback.onItemClick(position, summary!!.moderator?.id!!)
        }



        if(help.ressourceId != 0){
            val context = holder.binding.root.context
            holder.binding.ivHomeV2HelpItem.setImageDrawable(context.getDrawable(help.ressourceId))
        }
        help.title.let {
            val context = holder.binding.root.context
            holder.binding.tvHomeV2HelpItem.text = it
            val isRTL = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
            holder.binding.ivArrowRightHomeV2HelpItem.scaleX = if (isRTL) -1f else 1f

            if(position == 0){
                holder.binding.homeV2PedagoItemMainLayout.background = context.getDrawable(R.drawable.home_version_two_large_button_gradient_shape)
                holder.binding.tvHomeV2HelpItem.setTextColor(context.getColor(R.color.white))
                holder.binding.ivArrowRightHomeV2HelpItem.setColorFilter(context.getColor(R.color.white))
                summary?.moderator?.imageURL?.let { imageUrl->
                    Glide.with(context)
                        .load(Uri.parse(imageUrl))
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(holder.binding.ivHomeV2HelpItem)
                } ?: run {
                    Glide.with(holder.binding.root.context)
                        .load(R.drawable.placeholder_user)
                        .transform(CenterCrop(), GranularRoundedCorners(45F, 45F, 0F, 0F))
                        .into(holder.binding.ivHomeV2HelpItem)
                }
            }
        }
    }

    class HelpViewHolder(val binding: HomeV2HelpItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}