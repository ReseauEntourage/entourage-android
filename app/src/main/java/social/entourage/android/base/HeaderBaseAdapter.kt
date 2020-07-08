package social.entourage.android.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.map.MapViewHolder

open class HeaderBaseAdapter : EntourageBaseAdapter() {
    protected var mapViewHolder: MapViewHolder? = null
    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var onFollowButtonClickListener: View.OnClickListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) { //header position
            if (mapViewHolder == null && holder is MapViewHolder) {
                mapViewHolder = holder
            }
            //we populate with  no data
            mapViewHolder?.populate()
            return
        }
        super.onBindViewHolder(holder, position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TimestampedObject.TOP_VIEW
        } else super.getItemViewType(position)
    }

    fun setMapHeight(height: Int) {
        mapViewHolder?.setHeight(height)
    }

    fun setGeolocStatusIcon(visible: Boolean) {
        mapViewHolder?.setGeolocStatusIcon(visible)
    }

    fun displayGeolocStatusIcon(active: Boolean) {
        mapViewHolder?.displayGeolocStatusIcon(active)
    }

    override val positionOffset: Int
        get() = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val cardViewHolder = super.onCreateViewHolder(parent, viewType)
        if (cardViewHolder is MapViewHolder && viewType == TimestampedObject.TOP_VIEW) {
            mapViewHolder = cardViewHolder
            cardViewHolder.setMapReadyCallback(onMapReadyCallback)
            cardViewHolder.setFollowButtonOnClickListener(onFollowButtonClickListener)
            cardViewHolder.setGeolocStatusIcon(isLocationEnabled() && isLocationPermissionGranted())
        }
        return cardViewHolder
    }

    fun setOnMapReadyCallback(onMapReadyCallback: OnMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback
    }

    fun setOnFollowButtonClickListener(onFollowButtonClickListener: View.OnClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener
    }
}