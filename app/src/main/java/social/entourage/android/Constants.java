package social.entourage.android;

public class Constants {

    //TODO: should be changed after each release
    // API Key
    public static final String API_KEY = "f28b6ff3362be6dd408e4bae";

    // Filenames
    public static final String SHARED_PREFERENCES_FILE = "entourage_shared_preferences";
    public static final String FILENAME_TAPE_QUEUE = "encounters_queue";

    // Update Dialog
    public static final String UPDATE_DIALOG_DISPLAYED = "update_dialog_displayed";

    // Request and result codes
    public static final int REQUEST_CREATE_ENCOUNTER = 1;
    public static final int RESULT_CREATE_ENCOUNTER_OK = 2;

    // Flurry events
    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";
    public static final String EVENT_OPEN_POI_FROM_MAP = "Open_POI_From_Map";

    //LOG IN Events
    public static final String EVENT_LOGOUT = "Logout";
    public static final String EVENT_LOGIN_START = "Login_Start";
    public static final String EVENT_LOGIN_OK = "Login_Success";
    public static final String EVENT_LOGIN_FAILED = "Login_Failed";
    public static final String EVENT_TUTORIAL_START = "Log_Tutorial_Start";
    public static final String EVENT_TUTORIAL_END = "Log_Tutorial_End";
    public static final String EVENT_LOGIN_SEND_NEW_CODE = "Login_Send_New_Code";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_OK = "Newsletter_Inscription_OK";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_FAILED = "Newsletter_Inscription_Failed";
    public static final String EVENT_SPLASH_SIGNUP = "SplashSignUp";
    public static final String EVENT_SPLASH_LOGIN = "SplashLogIn";
    public static final String EVENT_PHONE_SUBMIT = "TelephoneSubmit";
    public static final String EVENT_PHONE_SUBMIT_FAIL = "TelephoneSubmitFail";
    public static final String EVENT_SMS_CODE_REQUEST = "SMSCodeRequest";
    public static final String EVENT_EMAIL_SUBMIT = "EmailSubmit";
    public static final String EVENT_NAME_SUBMIT = "NameSubmit";
    public static final String EVENT_NAME_SUBMIT_ERROR = "NameSubmitError";
    public static final String EVENT_NAME_TYPE = "NameType";
    public static final String EVENT_PHOTO_UPLOAD_SUBMIT = "PhotoUploadSubmit";
    public static final String EVENT_PHOTO_TAKE_SUBMIT = "PhotoTakeSubmit";
    public static final String EVENT_PHOTO_IGNORE = "IgnorePhoto";
    public static final String EVENT_PHOTO_BACK = "BackFromPhoto1";
    public static final String EVENT_PHOTO_SUBMIT = "SubmitInstantPhoto";

    //MENU Events
    public static final String EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_MENU = "Open_Guide_From_Menu";
    public static final String EVENT_OPEN_TOURS_FROM_MENU = "Open_Tours_From_Menu";

    //TOUR Events
    public static final String EVENT_START_TOUR = "Start_Tour";
    public static final String EVENT_STOP_TOUR = "Stop_Tour";
    public static final String EVENT_OPEN_TOUR_LAUNCHER_FROM_MAP = "Open_Tour_Launcher_From_Map";

    //TOUR ENCOUNTER Events
    public static final String EVENT_CREATE_ENCOUNTER_START = "Open_Create_Encounter_From_Tour";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK = "Encounter_Voice_Message_Recorded_OK";
    public static final String EVENT_CREATE_ENCOUNTER_OK = "Encounter_Created";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED = "Encounter_Voice_Message_Recording_Started";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED = "Encounter_Voice_Message_Recording_Not_Supported";
    public static final String EVENT_CREATE_ENCOUNTER_FAILED = "Encounter_Create_Failed";

    // Geolocation
    public static final long UPDATE_TIMER_MILLIS_OFF_TOUR = 20000;
    public static final long UPDATE_TIMER_MILLIS_ON_TOUR_FEET = 5000;
    public static final long UPDATE_TIMER_MILLIS_ON_TOUR_CAR = 3000;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR = 0;//30;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR_FEET = 0;//5;
    public static final float DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR_CAR = 0;//3;

    //Time constants
    public static final long MILLIS_HOUR = 3600000; //1000 * 60 * 60

    // Items per pagination
    public static final int ITEMS_PER_PAGE = 10;

    //Invite success automatic hide delay
    public static final long INVITE_SUCCESS_HIDE_DELAY = 5000; //1000 * 5
}
