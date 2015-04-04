package social.entourage.android.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {

    // Param keys
    public static final String KEY_ENCOUNTER = "keyEncounter";

    public static final String KEY_POI = "keyPoi";

    public static final String KEY_LATITUDE = "keyLatitude";

    public static final String KEY_LONGITUDE = "keyLongitude";

    // Formatters
    public static final DateFormat FORMATER_DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");

    public static final DateFormat FORMATER_HHMM = new SimpleDateFormat("hh:mm");

    // Request and result codes
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;

    public static final int REQUEST_CREATE_ENCOUNTER = 1;

    // Flurry events
    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";



}
