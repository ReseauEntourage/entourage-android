package social.entourage.android.guide.poi

import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.guide.poi.PoiViewHolder.Companion.layoutResource
import social.entourage.android.map.MapViewHolder

/**
 * Point of interest adapter
 *
 * Created by mihaiionescu on 26/04/2017.
 */
class PoisAdapter : HeaderBaseAdapter() {
    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                ViewHolderType(MapViewHolder::class.java, R.layout.layout_feed_full_map_card)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.GUIDE_POI,
                ViewHolderType(PoiViewHolder::class.java, layoutResource)
        )
        setHasStableIds(false)
    }
}