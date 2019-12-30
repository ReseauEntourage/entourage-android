package social.entourage.android.api.model.tape;

import social.entourage.android.api.model.guide.Poi;
import social.entourage.android.api.tape.Events;

/**
 * Events specific to Entourage app
 * Created by Mihai Ionescu on 16/04/2018.
 */
public class EntouragePoiRequest extends Events {

    /**
     * Event signaling that poi view is requested
     */

    public static class OnPoiViewRequestedEvent {

        private Poi poi;

        public OnPoiViewRequestedEvent(Poi poi) {
            this.poi = poi;
        }

        public Poi getPoi() {
            return poi;
        }
    }

}
