package social.entourage.android.guide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_poi_card.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.guide.poi.PoiRenderer
import java.util.ArrayList

/**
 * Created by Jr (MJ-DEVS) on 24/11/2020.
 */
class GDSSearchAdapter(var items: ArrayList<Poi>,val listenerClick: (position:Int) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TYPE_EMPTY = 0
    val TYPE_POI = 1

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
        when(viewType) {
            TYPE_EMPTY -> return VHEmpty(layoutInflater.inflate(R.layout.layout_search_poi_empty, parent, false))
            else -> return VHPoi(layoutInflater.inflate(R.layout.layout_poi_card, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VHPoi) {
            holder.bind(position)
        }
    }

    override fun getItemCount(): Int {
        if (items.size == 0 && isAlreadySend) return 1
        return items.size
    }

    inner class VHPoi(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            val poi = items[position]
            itemView.poi_card_title?.text = poi.name ?: ""
            itemView.poi_card_address?.text = poi.address ?: ""
            itemView.poi_card_distance?.text = LocationPoint(poi.latitude, poi.longitude).distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
            itemView.poi_card_call_button?.visibility =  View.GONE

            itemView.setOnClickListener {
                listenerClick(position)
            }
        }
    }

    inner class VHEmpty(view: View) : RecyclerView.ViewHolder(view)
}