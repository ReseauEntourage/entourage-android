package social.entourage.android.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home_neo_help.ui_bt_back
import kotlinx.android.synthetic.main.fragment_home_neo_tour_list.*
import kotlinx.android.synthetic.main.layout_cell_home_neo_tour.view.*
import social.entourage.android.R
import social.entourage.android.api.HomeTourArea
import social.entourage.android.api.TourAreaApi
import social.entourage.android.tools.log.AnalyticsEvents


class HomeNeoTourListFragment : Fragment() {

    var adapter: HomeNeoTourListAdapter? = null
    var datas:ArrayList<HomeTourArea> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_neo_tour_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            (parentFragment as? HomeFragment)?.onBackPressed()
        }

        adapter = HomeNeoTourListAdapter(datas) { position ->
            val tourArea = datas[position]
            (parentFragment as? HomeFragment)?.goTourSend(tourArea)

            val tagAnalytic = String.format(AnalyticsEvents.ACTION_NEOFEEDFIRST_TourCity,tourArea.postalCode)
            AnalyticsEvents.logEvent(tagAnalytic)
        }

        ui_recyclerview?.adapter = adapter
        ui_recyclerview?.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        getTourList()
    }

    fun getTourList() {
        TourAreaApi.getInstance().getTourAreas { tourAreas, error ->
            datas = ArrayList()

            if (tourAreas != null) {
                for (tourArea in tourAreas) {
                    if (tourArea.isActive()) {
                        datas.add(tourArea)
                    }
                }
                adapter?.updateDatas(datas)
            }
        }
    }

    companion object {
        const val TAG = "social.entourage.android.home.neo.tour.list"
    }
}

/***
 * RecyclerView Adapter
 */

class HomeNeoTourListAdapter(var arrayItems:ArrayList<HomeTourArea>,
                             val listener:(position:Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateDatas(datas:ArrayList<HomeTourArea>) {
        arrayItems = ArrayList()
        arrayItems.addAll(datas)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view = inflater.inflate(R.layout.layout_cell_home_neo_tour, parent, false)
        return CellVH(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CellVH).bind(position)
    }

    override fun getItemCount(): Int {
        return arrayItems.size
    }

    inner class CellVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind( position: Int) {
            itemView.ui_tv_title.text = arrayItems[position].areaName

            itemView.setOnClickListener {
                listener(position)
            }
        }
    }
}