package social.entourage.android;

public class Constants {

    // Announcements version
    public static final String ANNOUNCEMENTS_VERSION = "v1";

    // Filenames
    public static final String FILENAME_TAPE_QUEUE = "encounters_queue";

    // Request and result codes
    public static final int REQUEST_CREATE_ENCOUNTER = 1;
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;

    // Link IDs
    public static final String SCB_LINK_ID = "pedagogic-content";
    public static final String GOAL_LINK_ID= "action-examples";
    public static final String DONATE_LINK_ID = "donation";
    public static final String ATD_LINK_ID = "atd-partnership";
    public static final String CHARTE_LINK_ID = "ethics-charter";
    public static final String FAQ_LINK_ID = "faq";
    public static final String SUGGESTION_ID = "suggestion";
    public static final String FEEDBACK_ID = "feedback";
    public static final String VOLUNTEERING_ID = "volunteering";
    public static final String PROPOSE_POI_ID = "propose-poi";
    public static final String TERMS_LINK_ID = "terms";
    public static final String PRIVACY_LINK_ID = "privacy-policy";
    public static final String AMBASSADOR_ID = "devenir-ambassadeur";

    // Geolocation
    public static final long UPDATE_TIMER_MILLIS_OFF_TOUR = 20000;
    public static final long UPDATE_TIMER_MILLIS_ON_TOUR = 5000;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR = 0;//30;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR = 0;//5;

    //Time constants
    public static final long MILLIS_HOUR = 3600000; //1000 * 60 * 60

    // Items per pagination
    public static final int ITEMS_PER_PAGE = 10;

    //Invite success automatic hide delay
    public static final long INVITE_SUCCESS_HIDE_DELAY = 5000; //1000 * 5

    // Don't show the popup again within this distance
    public static final int EMPTY_POPUP_DISPLAY_LIMIT = 300; //meters

    // Carousel delay time
    public static final long CAROUSEL_DELAY_MILLIS = 15000; // 15 seconds
}
