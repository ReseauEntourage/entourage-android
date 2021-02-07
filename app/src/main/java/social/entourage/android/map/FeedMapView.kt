package social.entourage.android.map

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView

/**
 * Created by mihaiionescu on 28/06/2017.
 */
class FeedMapView : MapView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context?, attributeSet: AttributeSet?, i: Int) : super(context, attributeSet, i)
    constructor(context: Context?, googleMapOptions: GoogleMapOptions?) : super(context, googleMapOptions)

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> parent.requestDisallowInterceptTouchEvent(false)
        }

        // Process event as normal. If parent has disallowed touch events, the map will process the event.
        return super.dispatchTouchEvent(motionEvent)
    }
}