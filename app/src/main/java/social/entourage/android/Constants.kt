package social.entourage.android

object Constants {
    // Announcements version
    const val ANNOUNCEMENTS_VERSION = "v1"

    // Filenames
    const val FILENAME_TAPE_QUEUE = "encounters_queue"

    // Request and result codes
    const val REQUEST_CREATE_ENCOUNTER = 1
    const val RESULT_CREATE_ENCOUNTER_OK = 2

    // Link IDs
    const val SCB_LINK_ID = "pedagogic-content"
    const val AGIR_FAQ_ID = "action_faq"
    const val GOAL_LINK_ID = "action-examples"
    const val DONATE_LINK_ID = "donation"
    const val ATD_LINK_ID = "atd-partnership"
    const val CHARTE_LINK_ID = "ethics-charter"
    const val FAQ_LINK_ID = "faq"
    const val SUGGESTION_ID = "suggestion"
    const val FEEDBACK_ID = "feedback"
    const val VOLUNTEERING_ID = "volunteering"
    const val PROPOSE_POI_ID = "propose-poi"
    const val TERMS_LINK_ID = "terms"
    const val PRIVACY_LINK_ID = "privacy-policy"
    const val AMBASSADOR_ID = "devenir-ambassadeur"
    const val EVENTS_GUIDE_ID = "events-guide"
    const val GOOD_WAVES_ID = "good_waves"
    const val BLOG_LINK_ID = "how-to-present"

    const val ASSO_AGIR_LINK_ID = "partner_action_faq"

    // Geolocation
    const val UPDATE_TIMER_MILLIS_OFF_TOUR: Long = 20000
    const val UPDATE_TIMER_MILLIS_ON_TOUR: Long = 5000
    const val DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR = 0f //30;
    const val DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR = 0f //5;

    //Time constants
    const val MILLIS_HOUR: Long = 3600000 //1000 * 60 * 60

    // Items per pagination
    const val ITEMS_PER_PAGE = 10

    //Invite success automatic hide delay
    const val INVITE_SUCCESS_HIDE_DELAY: Long = 5000 //1000 * 5

    // Don't show the popup again within this distance
    const val EMPTY_POPUP_DISPLAY_LIMIT = 300 //meters

    // Carousel delay time
    const val CAROUSEL_DELAY_MILLIS: Long = 15000 // 15 seconds
    const val DISTANCE_MAX_DISPLAY = 100000.0f //100km

    const val SLUG_HUB_LINK_1 = "hub_1"
    const val SLUG_HUB_LINK_2 = "hub_2"
    const val SLUG_HUB_LINK_3 = "hub_3"
    const val SLUG_HUB_LINK_FAQ = "hub_faq"
}