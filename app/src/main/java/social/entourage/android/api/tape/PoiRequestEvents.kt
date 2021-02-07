package social.entourage.android.api.tape

import social.entourage.android.api.model.guide.Poi

/**
 * Events specific to Entourage app
 * Created by Mihai Ionescu on 16/04/2018.
 */
class PoiRequestEvents : Events() {
    /**
     * Event signaling that poi view is requested
     */
    class OnPoiViewRequestedEvent(val poi: Poi)
}