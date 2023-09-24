package social.entourage.android.homev2

import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Help
import social.entourage.android.databinding.HomeV2HelpItemLayoutBinding
import social.entourage.android.tools.utils.px


class HomeHelpAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var helps:MutableList<Help> = mutableListOf()


    fun resetData(helps:MutableList<Help>){
        this.helps.clear()
        this.helps.addAll(helps)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.helps.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = HomeV2HelpItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HelpViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return helps.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HelpViewHolder) {
            val help = helps[position]
            help.title.let {
                val context = holder.binding.root.context
                holder.binding.tvHomeV2HelpItem.text = it
                if(it.contains("question")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.binding.homeV2PedagoItemMainLayout.setBackgroundColor(context.getColor(R.color.orange))
                        holder.binding.tvHomeV2HelpItem.setTextColor(context.getColor(R.color.white))
                        holder.binding.ivArrowRightHomeV2HelpItem.setColorFilter(context.getColor(R.color.white))

                    }
                }
            }
            if(help.ressourceId != 0){
                val context = holder.binding.root.context
                holder.binding.ivHomeV2HelpItem.setImageDrawable(context.getDrawable(help.ressourceId))
            }

        }
    }

    class HelpViewHolder(val binding: HomeV2HelpItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}