package social.entourage.android.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {
    public static final String KEY_ENCOUNTER = "keyEncounter";
    public static final String KEY_POI = "keyPoi";
    public static final String KEY_LATITUDE = "keyLatitude";
    public static final String KEY_LONGITUDE = "keyLongitude";

    public static final DateFormat FORMATER_DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");

    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;
    public static final int REQUEST_CREATE_ENCOUNTER = 1;

    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";
}
