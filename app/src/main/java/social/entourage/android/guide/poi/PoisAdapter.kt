package social.entourage.android.guide.poi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.databinding.LayoutFeedFullMapCardBinding
import social.entourage.android.databinding.LayoutPoiCardBinding
import timber.log.Timber

/**
 * Point of interest adapter
 *
 * Created by mihaiionescu on 26/04/2017.
 */
class PoisAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: ArrayList<TimestampedObject> = ArrayList()

    override fun getItemCount(): Int {
        return items.size + positionOffset
    }

    val dataItemCount: Int
        get() = items.size

    //private val mDaySections = HashMap<Date, Int>() //Move from discussionAdapter . use here to clear array before refresh - To fix date separator
    // visibily error
    fun addItems(addItems: List<TimestampedObject>) {
        val positionStart = (if (items.size == 0) 0 else items.size - 1) + positionOffset
        for (to in addItems) {
            addCardInfo(to, false)
        }
        notifyItemRangeInserted(positionStart, addItems.size)
    }

    private fun addCardInfo(cardInfo: TimestampedObject, notifyView: Boolean) {
        items.add(cardInfo)
        if (notifyView) {
            notifyItemInserted(items.size - 1 + positionOffset)
        }
    }

    fun removeAll() {
        val oldCount = dataItemCount
        if (oldCount == 0) return
        items.clear()
        notifyItemRangeRemoved(positionOffset, oldCount)
    }

    private var mapViewHolder: MapViewHolder? = null
    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var onFollowButtonClickListener: View.OnClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TimestampedObject.TOP_VIEW) {
            (holder as? MapViewHolder)?.populate()
        }
        else {
            (holder as? PoiViewHolder)?.populate(items[position - positionOffset])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TimestampedObject.TOP_VIEW
        } else items[position - positionOffset].type
    }

    fun setMapHeight(height: Int) {
        mapViewHolder?.setHeight(height)
    }

    fun setGeolocStatusIcon(visible: Boolean) {
        mapViewHolder?.setGeolocStatusIcon(visible)
    }

    private val positionOffset: Int
        get() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TimestampedObject.TOP_VIEW) {
            val binding = LayoutFeedFullMapCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            if(mapViewHolder!=null) {
                Timber.e("MapViewHolder already created")
            }
            MapViewHolder(binding).apply {
                mapViewHolder = this
                onMapReadyCallback?.let {this.setMapReadyCallback(it)}
                this.setFollowButtonOnClickListener(onFollowButtonClickListener)
                this.setGeolocStatusIcon(LocationUtils.isLocationPermissionGranted())
            }
        }
        else {
            val binding = LayoutPoiCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PoiViewHolder(binding)
        }
    }

    fun setOnMapReadyCallback(onMapReadyCallback: OnMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback
    }

    fun setOnFollowButtonClickListener(onFollowButtonClickListener: View.OnClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener
    }

    init {
        setHasStableIds(false)
    }
}