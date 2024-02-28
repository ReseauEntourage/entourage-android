package social.entourage.android.guide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.databinding.LayoutPoiCardBinding
import social.entourage.android.databinding.LayoutSearchPoiEmptyBinding
import social.entourage.android.guide.poi.PoiViewHolder

/**
 * Created by Jr (MJ-DEVS) on 24/11/2020.
 */
class GDSSearchAdapter(var items: ArrayList<Poi>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isAlreadySend = false
    fun updateAdapter(items: ArrayList<Poi>) {
        this.items = items
        notifyDataSetChanged()
        isAlreadySend = true
    }

    override fun getItemViewType(position: Int): Int {
        if (items.size == 0) return TYPE_EMPTY
        return TYPE_POI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EMPTY -> {
                // Utilisation de ViewBinding pour le type vide
                val binding = LayoutSearchPoiEmptyBinding.inflate(layoutInflater, parent, false)
                VHEmpty(binding.root)
            }
            else -> {
                // Utilisation de ViewBinding pour le PoiViewHolder
                val binding = LayoutPoiCardBinding.inflate(layoutInflater, parent, false)
                PoiViewHolder(binding).apply { showCallButton = false }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? PoiViewHolder)?.populate(items[position])
    }

    override fun getItemCount(): Int {
        if (items.size == 0 && isAlreadySend) return 1
        return items.size
    }

    inner class VHEmpty(view: View) : RecyclerView.ViewHolder(view)
    companion object {
        const val TYPE_POI = 1
        const val TYPE_EMPTY = 0
    }
}