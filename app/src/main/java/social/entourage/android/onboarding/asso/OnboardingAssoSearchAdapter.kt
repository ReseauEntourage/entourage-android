package social.entourage.android.onboarding.asso

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_cell_onboarding_asso_search.view.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.tools.hideKeyboard

/**
 * Created by Jr (MJ-DEVS) on 04/06/2020.
 */
class OnboardingAssoSearchAdapter(val context: Context, var arrayAsso: ArrayList<Partner>,val clickListener:(position:Int) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var rowSelectedPosition = -1
    var isFiltered = false
    var arrayFiltered = ArrayList<Partner>()

    fun reloadDatas(arrayAssos:ArrayList<Partner>) {
        isFiltered = false
        rowSelectedPosition = -1
        this.arrayAsso = arrayAssos
        notifyDataSetChanged()
    }

    fun updateDatas(isFiltered:Boolean,arrayFiltered:ArrayList<Partner>?) {
        this.isFiltered = isFiltered
        rowSelectedPosition = -1
        if (isFiltered && arrayFiltered != null) {
            this.arrayFiltered = arrayFiltered
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssociationVH {
        val v = LayoutInflater.from(context)
                .inflate(R.layout.layout_cell_onboarding_asso_search,parent, false)
        return AssociationVH(v)
    }

    override fun getItemCount(): Int {
        return if (isFiltered) arrayFiltered.size else arrayAsso.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as AssociationVH
        holder.bind(position)
    }

    /********************************
     * Inner class Viewholder
     ********************************/
    inner class AssociationVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val asso = if (isFiltered) arrayFiltered[position] else arrayAsso[position]

            itemView.ui_tv_title.text = asso.name

            val isSelected = rowSelectedPosition == position
            if (isSelected) {
                itemView.ui_iv_select.setImageResource(R.drawable.contact_selected)
            }
            else {
                itemView.ui_iv_select.setImageResource(R.drawable.ic_filter_rb_bg_active)
            }

            itemView.setOnClickListener {
                clickListener(position)
                rowSelectedPosition = position
                itemView.hideKeyboard()
                notifyDataSetChanged()
            }
        }
    }
}