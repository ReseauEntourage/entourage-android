package social.entourage.android.common;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Constants {
    public static final String KEY_ENCOUNTER = "keyEncounter";

    public static final DateFormat FORMATER_DDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
    public static final int REQUEST_CREATE_ENCOUNTER = 1;
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;
    public static final String KEY_LATITUDE = "keyLatitude";
    public static final String KEY_LONGITUDE = "keyLongitude";
}
