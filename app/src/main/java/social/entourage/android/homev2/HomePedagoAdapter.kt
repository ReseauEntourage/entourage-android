package social.entourage.android.homev2

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Pedago
import social.entourage.android.databinding.HomeV2PedagoItemLayoutBinding
import social.entourage.android.tools.utils.px

class HomePedagoAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pedagos:MutableList<Pedago> = mutableListOf()


    fun resetData(pedagos:MutableList<Pedago>){
        this.pedagos.clear()
        this.pedagos.addAll(pedagos)
        notifyDataSetChanged()

    }

    fun clearList(){
        this.pedagos.clear()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = HomeV2PedagoItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedagoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pedagos.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PedagoViewHolder) {

            val pedago = pedagos[position]
            Log.wtf("wtf" , "pedago name " + pedago.name)
            Log.wtf("wtf" , "pedago imageUrl " + pedago.imageUrl)
            Log.wtf("wtf" , "pedago duration " + pedago.duration)
            Log.wtf("wtf" , "pedago category " + pedago.category)

            pedago.imageUrl?.let {
                Glide.with(holder.binding.root.context)
                    .load(Uri.parse(it))
                    .placeholder(R.drawable.ic_event_placeholder)
                    .transform(CenterCrop(), GranularRoundedCorners(16F, 16F, 16F, 16F))
                    .error(R.drawable.ic_event_placeholder)
                    .into(holder.binding.ivPedagoItem)
            }?: run {
                Glide.with(holder.binding.root.context)
                    .load(R.drawable.ic_placeholder_action)
                    .into(holder.binding.ivPedagoItem)
            }
            pedago.name.let {
                holder.binding.tvTitleItemPedago.text = it
            }
            pedago.duration.let {
                val context = holder.binding.root.context
                val formattedString = context.getString(R.string.home_v2_pedag_item_lenght_title, it)
               holder.binding.tvLenghtPedagoItem.text = formattedString
            }
            pedago.category.let {
                holder.binding.tvTagPedagoItem.text = "Comprendre"

            }
        }
    }

    class PedagoViewHolder(val binding: HomeV2PedagoItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

}