package social.entourage.android.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

    // Tour
    public static final String KEY_TOUR_ID = "tourId";

    // API
    public static final String TOKEN = "0cb4507e970462ca0b11320131e96610";

    // Param keys
    public static final String KEY_ENCOUNTER = "keyEncounter";
    public static final String KEY_POI = "keyPoi";
    public static final String KEY_LATITUDE = "keyLatitude";
    public static final String KEY_LONGITUDE = "keyLongitude";

    // Twitter
    public static final String HASHTAG = "#entourage";
    public static final String TWITTER_ENTOURAGE_ACCOUNT_NAME = "@R_Entour";

    // Formatters
    public static final DateFormat FORMATER_DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    public static final DateFormat FORMATER_HHMM = new SimpleDateFormat("hh:mm", Locale.US);

    // Request and result codes
    public static final int REQUEST_CREATE_ENCOUNTER = 1;
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;

    // Flurry events
    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";
    public static final String EVENT_OPEN_POI_FROM_MAP = "Open_POI_From_Map";
    public static final String EVENT_OPEN_GUIDE_FROM_MENU = "Open_Guide_From_Menu";

    // GeoCoder (essai pour la récupération des adresses dans le Run-Tracking NTE) ---> à supprimer
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    private static final String PACKAGE_NAME = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    // Geolocation
    public static final long UPDATE_TIMER_MILLIS = 1000;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS = 10;



}
