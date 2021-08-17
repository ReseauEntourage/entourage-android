package social.entourage.android.onboarding.pre_onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_cell_pre_onboarding.view.*
import social.entourage.android.R

/**
 * Created by Jr on 15/04/2020.
 */
class PreOnboardingRVAdapter(val context:Context,private val myDataset: ArrayList<Int>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

   inner class ImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(res: Int) {
            itemView.ui_iv_cell_preonboard?.setImageResource(res)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
        val v = LayoutInflater.from(context)
                .inflate(R.layout.layout_cell_pre_onboarding,parent, false)
        return ImageVH(v)
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ImageVH
        holder.bind(myDataset[position])
    }
}