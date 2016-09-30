package social.entourage.android;

import java.lang.ref.SoftReference;

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
    public static final String EVENT_NOTIFICATIONS_ACCEPT = "AcceptNotificationsFromPopup";
    public static final String EVENT_NOTIFICATIONS_REFUSE = "RefuseNotificationsFromPopup";
    public static final String EVENT_GEOLOCATION_ACCEPT = "AcceptGeolocFromPopup";
    public static final String EVENT_GEOLOCATION_REFUSE = "RefuseGeolocFromPopup";

    //MENU Events
    public static final String EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_MENU = "Open_Guide_From_Menu";
    public static final String EVENT_OPEN_TOURS_FROM_MENU = "Open_Tours_From_Menu";
    public static final String EVENT_MENU_TAP_MY_PROFILE = "TapMyProfilePhoto";
    public static final String EVENT_MENU_LOGOUT = "LogOut";
    public static final String EVENT_MENU_ABOUT = "AboutClick";

    //FEED Events
    public static final String EVENT_FEED_MESSAGES = "GoToMessages";
    public static final String EVENT_FEED_MENU = "OpenMenu";
    public static final String EVENT_FEED_USERPROFILE = "UserProfileClick";
    public static final String EVENT_FEED_ACTIVE_CLOSE_OVERLAY = "OpenActiveCloseOverlay";
    public static final String EVENT_FEED_MAPCLICK = "MapClick";
    public static final String EVENT_FEED_HEATZONECLICK = "HeatzoneMapClick";
    public static final String EVENT_FEED_TOURLINECLICK = "TourMapClick"; //Not possible
    public static final String EVENT_FEED_OPEN_ENTOURAGE = "OpenEntouragePublicPage";
    public static final String EVENT_FEED_OPEN_CONTACT = "OpenEnterInContact";
    public static final String EVENT_FEED_RECENTERCLICK = "RecenterMapClick";
    public static final String EVENT_FEED_FILTERSCLICK = "FeedFiltersPress";
    public static final String EVENT_FEED_REFRESH_LIST = "RefreshListPage";
    public static final String EVENT_FEED_SCROLL_LIST = "ScrollListPage";
    public static final String EVENT_FEED_PLUS_CLICK = "PlusFromFeedClick";
    public static final String EVENT_FEED_TOUR_CREATE_CLICK = "TourCreateClick";
    public static final String EVENT_FEED_ASK_CREATE_CLICK = "AskCreateClick";
    public static final String EVENT_FEED_OFFER_CREATE_CLICK = "OfferCreateClick";
    public static final String EVENT_FEED_GUIDE_SHOW_CLICK = "GDSViewClick";
    public static final String EVENT_FEED_PENDING_OVERLAY = "PendingRequestOverlay";
    public static final String EVENT_FEED_CANCEL_JOIN_REQUEST = "CancelJoinRequest";
    public static final String EVENT_FEED_OPEN_ACTIVE_OVERLAY = "OpenActiveOverlay";
    public static final String EVENT_FEED_QUIT_ENTOURAGE = "QuitFromFeed";

    //MAP Events
    public static final String EVENT_MAP_MAPVIEW_CLICK = "MapViewClick"; //Not used
    public static final String EVENT_MAP_LISTVIEW_CLICK = "ListViewClick";
    public static final String EVENT_MAP_LONGPRESS = "HiddenButtonsOverlayPress";
    public static final String EVENT_MAP_ZOOM_IN = "ZoomIn";
    public static final String EVENT_MAP_ZOOM_OUT = "ZoomOut";
    public static final String EVENT_MAP_SHIFT_CENTER = "MapShiftCenter"; //Not able to detect if it's an automatic or manual shift

    //GUIDE Events
    public static final String EVENT_GUIDE_POI_VIEW = "POIView";
    public static final String EVENT_GUIDE_PLUS_CLICK = "PlusFromGDSClick";
    public static final String EVENT_GUIDE_MASK_CLICK = "MaskGDSClick";

    //SEND JOIN REQUEST Events
    public static final String EVENT_JOIN_REQUEST_START = "StartJoinMessage";
    public static final String EVENT_JOIN_REQUEST_SUBMIT = "SubmitJoinMessage";
    public static final String EVENT_JOIN_REQUEST_ACCEPT = "AcceptJoinRequest";
    public static final String EVENT_JOIN_REQUEST_REJECT = "RejectJoinRequest";

    //ENTOURAGE VIEW Events
    public static final String EVENT_ENTOURAGE_VIEW_WRITE_MESSAGE = "WriteMessage";
    public static final String EVENT_ENTOURAGE_VIEW_SPEECH = "SpeechRecognitionMessage";
    public static final String EVENT_ENTOURAGE_VIEW_ADD_MESSAGE = "AddContentToMessage";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_OVERLAY = "OpenEntourageOptionsOverlay";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE = "CloseEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_QUIT = "ExitEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_OPTIONS_EDIT = "EditEntourageConfirm";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_FRIENDS = "InviteFriendsClick";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_CONTACTS = "InviteContacts";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_PHONE = "InviteByPhoneNumber";
    public static final String EVENT_ENTOURAGE_VIEW_INVITE_CLOSE = "InviteFriendsClose";
    public static final String EVENT_ENTOURAGE_VIEW_SWITCH_PUBLIC = "EntouragePublicPageFromMessages";
    public static final String EVENT_ENTOURAGE_VIEW_ASK_JOIN = "AskJoinFromPublicPage";
    public static final String EVENT_ENTOURAGE_CREATE_CHANGE_LOCATION = "ChangeLocationClick";

    //MY ENTOURAGES Events
    public static final String EVENT_MYENTOURAGES_BANNER_CLICK = "BannerMessageClick";
    public static final String EVENT_MYENTOURAGES_MESSAGE_OPEN = "MessageOpen"; //The entourage card is reused in multiple screens
    public static final String EVENT_MYENTOURAGES_PLUS_CLICK = "PlusOnMessagesPageClick";
    public static final String EVENT_MYENTOURAGES_FILTER_CLICK = "MessagesFilterClick";
    public static final String EVENT_MYENTOURAGES_BANNER_MOVE = "MoveBannerClick"; //A lot of code needs to be written to detect this
    public static final String EVENT_MYENTOURAGES_BACK_CLICK = "BackToFeedClick";

    //MY ENTOURAGES FILTER Events
    public static final String EVENT_MYENTOURAGES_FILTER_EXIT = "ExitMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_SAVE = "SaveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ACTIVE = "ActiveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_INVITATIONS = "InvitationsFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ORGANIZER = "OrganizerFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_PAST = "PastFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ASK = "AskMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_OFFER = "OfferMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_TOUR = "TourMessagesFilter";

    //TOUR Events
    public static final String EVENT_START_TOUR = "StartTourClick";
    public static final String EVENT_STOP_TOUR = "TourStop";
    public static final String EVENT_RESTART_TOUR = "TourRestart";
    public static final String EVENT_OPEN_TOUR_LAUNCHER_FROM_MAP = "Open_Tour_Launcher_From_Map";
    public static final String EVENT_TOUR_MEDICAL = "MedicalTourChoose";
    public static final String EVENT_TOUR_SOCIAL = "SocialTourChoose";
    public static final String EVENT_TOUR_DISTRIBUTION = "DistributionTourChoose";
    public static final String EVENT_TOUR_SUSPEND = "SuspendTourClick";
    public static final String EVENT_TOUR_PLUS_CLICK = "PlusOnTourClick";

    //TOUR ENCOUNTER Events
    public static final String EVENT_CREATE_ENCOUNTER_START = "Open_Create_Encounter_From_Tour";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK = "Encounter_Voice_Message_Recorded_OK";
    public static final String EVENT_CREATE_ENCOUNTER_OK = "Encounter_Created";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED = "Encounter_Voice_Message_Recording_Started";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED = "Encounter_Voice_Message_Recording_Not_Supported";
    public static final String EVENT_CREATE_ENCOUNTER_FAILED = "Encounter_Create_Failed";

    //MAP Filter Events
    public static final String EVENT_MAP_FILTER_FILTER1 = "ClickFilter1Value";
    public static final String EVENT_MAP_FILTER_FILTER2 = "ClickFilter2Value";
    public static final String EVENT_MAP_FILTER_FILTER3 = "ClickFilter3Value";
    public static final String EVENT_MAP_FILTER_ONLY_MINE = "ShowOnlyMineClick";
    public static final String EVENT_MAP_FILTER_ONLY_TOURS = "ShowOnlyToursFilterClick";
    public static final String EVENT_MAP_FILTER_ONLY_OFFERS = "ShowOnlyOffersClick";
    public static final String EVENT_MAP_FILTER_ONLY_ASK = "ShowOnlyAsksClick";
    public static final String EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS = "ShowOnlyMedicalToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS = "ShowOnlySocialToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS = "ShowOnlyDistributionToursClick";
    public static final String EVENT_MAP_FILTER_SUBMIT = "SubmitFilterPrefferences";
    public static final String EVENT_MAP_FILTER_CLOSE = "CloseFilter";

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
