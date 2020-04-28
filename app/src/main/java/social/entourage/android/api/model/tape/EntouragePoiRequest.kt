package social.entourage.android.api.model.tape

import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.tape.Events

/**
 * Events specific to Entourage app
 * Created by Mihai Ionescu on 16/04/2018.
 */
class EntouragePoiRequest : Events() {
    /**
     * Event signaling that poi view is requested
     */
    class OnPoiViewRequestedEvent(val poi: Poi)
}