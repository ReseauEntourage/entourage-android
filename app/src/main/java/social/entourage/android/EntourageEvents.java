package social.entourage.android;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;
import java.util.Objects;

import social.entourage.android.api.model.User;
import social.entourage.android.location.LocationUtils;

/**
 * Wrapper for sending events to different aggregators
 * Created by Mihai Ionescu on 03/10/2017.
 */

public class EntourageEvents {

    // Analytics events
    public static final String EVENT_OPEN_ENCOUNTER_FROM_MAP = "Open_Encounter_From_Map";
    public static final String EVENT_OPEN_POI_FROM_MAP = "Open_POI_From_Map";


    //----------------------------//
    // PREONBOARDING EVENTS
    //----------------------------//

    public static final String EVENT_VIEW_START_CARROUSEL1 = "View__Start__Carrousel1";
    public static final String EVENT_VIEW_START_CARROUSEL2 = "View__Start__Carrousel2";
    public static final String EVENT_VIEW_START_CARROUSEL3 = "View__Start__Carrousel3";
    public static final String EVENT_VIEW_START_CARROUSEL4 = "View__Start__Carrousel4";
    public static final String EVENT_VIEW_START_SIGNUPLOGIN = "View__Start__SignUpOrLogin";
    public static final String EVENT_ACTION_START_LOGINSTART = "Action__Start__LoginStart";
    public static final String EVENT_ACTION_START_SIGNUPSTART = "Action__Start__SignUpStart";

    //----------------------------//
    // LOGIN EVENTS
    //----------------------------//
    public static final String EVENT_VIEW_LOGIN_LOGIN = "View__Login__Login";
    public static final String EVENT_ACTION_LOGIN_SMS = "Action__Login__SMSCodeRequest";
    public static final String EVENT_ACTION_LOGIN_SUBMIT = "Action__Login__LoginSubmit";
    public static final String EVENT_ACTION_LOGIN_SUCCESS = "Action__Login__LoginSuccess";
    public static final String EVENT_ERROR_LOGIN_ERROR = "Error__Login__LoginError";
    public static final String EVENT_ERROR_LOGIN_FAIL = "Error__Login__LoginFail";
    public static final String EVENT_ERROR_LOGIN_PHONE = "Error__Login__TelephoneSubmitError";

    //----------------------------//
    // ONBOARDING - START
    //-------

    //------INPUT NAMES
    public static final String EVENT_VIEW_ONBOARDING_NAMES = "View__Onboarding__InputNames";
    public static final String EVENT_ACTION_ONBOARDING_NAMES = "Action__Onboarding__NameSubmit";

    //------PHONE
    public static final String EVENT_VIEW_ONBOARDING_PHONE = "View__Onboarding__InputPhone";
    public static final String EVENT_ACTION_ONBOARDING_PHONE_SUBMIT = "Action__Onboarding__PhoneSubmit";
    public static final String EVENT_ACTION_ONBOARDING_PHONE_SUBMIT_SUCCESS = "Action__Onboarding__PhoneSubmitSuccess";
    public static final String EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_ERROR = "Error__Onboarding__PhoneSubmitError";
    public static final String EVENT_ERROR_ONBOARDING_PHONE_SUBMIT_EXIST = "Error__Onboarding__PhoneAlreadyExistErro";
    //------CODE
    public static final String EVENT_VIEW_ONBOARDING__PASSCODE = "View__Onboarding__InputPasscode";
    public static final String EVENT_ACTION_ONBOARDING_SMS = "Action__Onboarding__SMSCodeRequest";
    public static final String EVENT_ACTION_ONBOARDING_SIGNUP_SUBMIT = "Action__Onboarding__SignUpSubmit";
    public static final String EVENT_ACTION_ONBOARDING_SIGNUP_SUCCESS = "Action__Onboarding__SignUpSuccess";
    //------public static final String EVENT_ERROR_ONBOARDING_SIGNUP_ERROR = "Error__Onboarding__SignUpError";
    public static final String EVENT_ERROR_ONBOARDING_SINGUP_FAIL = "Error__Onboarding__SignUpFail";

    //------PROFILE
    public static final String EVENT_VIEW_ONBOARDING_CHOOSE_PROFILE = "View__Onboarding__ChooseProfile";
    public static final String EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SIGNUP = "Action__Onboarding__ChooseProfile";
    public static final String EVENT_ACTION_ONBOARDING_CHOOSE_PROFILE_SKIP = "Action__Onboarding__ChooseProfileSkip";

    //------Riverain__Sdf
    public static final String EVENT_VIEW_ONBOARDING_ACTION_ZONE = "View__Onboarding__ActionZone";
    public static final String EVENT_ACTION_ONBOARDING_SETACTION_ZONE_GEOLOC = "Action__Onboarding__SetActionZoneGeoloc";
    public static final String EVENT_ACTION_ONBOARDING_SETACTION_ZONE_SEARCH = "Action__Onboarding__SetActionZoneSearch";
    public static final String EVENT_ACTION_ONBOARDING_ACTION_ZONE_SUBMIT = "Action__Onboarding__ActionZoneSubmit";
    public static final String EVENT_ACTION_ONBOARDING_ACTION_ZONE2_SUBMIT = "Action__Onboarding__ActionZone2Submit";
    //------Riverain__Sdf Mosaïc
    public static final String EVENT_VIEW_ONBOARDING_NEIGHBOR_MOSAIC = "View__Onboarding__NeighborMosaic";
    public static final String EVENT_VIEW_ONBOARDING_INNEED_MOSAIC = "View__Onboarding__InNeedMosaic";
    public static final String EVENT_ACTION_ONBOARDING_NEIGHBOR_MOSAIC = "Action__Onboarding__NeighborMosaic";
    public static final String EVENT_ACTION_ONBOARDING_INNEED_MOSAIC = "Action__Onboarding__InNeedMosaic";
    //------ASSO
    public static final String EVENT_VIEW_ONBOARDING_PRO_STORIES = "View__Onboarding__ProStoriesPage";
    public static final String EVENT_VIEW_ONBOARDING_PRO_FEATURES = "View__Onboarding__ProFeaturesPage";
    public static final String EVENT_VIEW_ONBOARDING_PRO_SIGNUP = "View__Onboarding__ProSignUp";
    public static final String EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUBMIT = "Action__Onboarding__ProSignUpSubmit";
    public static final String EVENT_ACTION_ONBOARDING_PRO_SIGNUP_SUCCESS = "Action__Onboarding__ProSignUpSuccess";
    public static final String EVENT_ERROR_ONBOARDING_PRO_SIGNUP_ERROR = "Error__Onboarding__ProSignUpError";
    //------Asso Mosaïc
    public static final String EVENT_VIEW_ONBOARDING_PRO_MOSAIC = "View__Onboarding__ProMosaic";
    public static final String EVENT_ACTION_ONBOARDING_PRO_MOSAIC = "Action__Onboarding__ProMosaic";
    //------EMAIL
    public static final String EVENT_VIEW_ONBOARDING_INPUT_EMAIL = "View__Onboarding__InputEmail";
    public static final String EVENT_ACTION_ONBOARDING_EMAIL_SUBMIT = "Action__Onboarding__EmailSubmit";
    public static final String EVENT_ERROR_ONBOARDING_EMAIL_SUBMIT_ERROR = "Error__Onboarding__EmailSubmitError";
    //------PHOTO
    public static final String EVENT_VIEW_ONBOARDING_CHOOSE_PHOTO = "View__Onboarding__ChoosePhoto";
    public static final String EVENT_ACTION_ONBOARDING_PHOTO_SUBMIT = "Action__Onboarding__PhotoSubmit";
    public static final String EVENT_ACTION_ONBOARDING_IGNORE_PHOTO = "Action__Onboarding__IgnorePhoto";
    public static final String EVENT_ACTION_ONBOARDING_UPLOAD_PHOTO = "Action__Onboarding__UploadPhoto";
    public static final String EVENT_ACTION_ONBOARDING_TAKE_PHOTO = "Action__Onboarding__TakePhoto";

    //-------
    // ONBOARDING - END
    //----------------------------//

    //----------------------------//
    // PROFILE EDIT - START
    //-------

    //------PHOTO
    public static final String EVENT_VIEW_PROFILE_CHOOSE_PHOTO = "View__Profile__ChoosePhoto";
    public static final String EVENT_ACTION_PROFILE_PHOTO_SUBMIT = "Action__Profile__PhotoSubmit";
    public static final String EVENT_ACTION_PROFILE_UPLOAD_PHOTO = "Action__Profile__UploadPhoto";
    public static final String EVENT_ACTION_PROFILE_TAKE_PHOTO = "Action__Profile__TakePhoto";
    //------ZONE
    public static final String EVENT_VIEW_PROFILE_ACTION_ZONE = "View__Profile__ActionZone";
    public static final String EVENT_ACTION_PROFILE_SETACTION_ZONE_GEOLOC = "Action__Profile__SetActionZoneGeoloc";
    public static final String EVENT_ACTION_PROFILE_SETACTION_ZONE_SEARCH = "Action__Profile__SetActionZoneSearch";
    public static final String EVENT_ACTION_PROFILE_ACTION_ZONE_SUBMIT = "Action__Profile__ActionZoneSubmit";

    //-------
    // PROFILE EDIT - END
    //----------------------------//


    //LOG IN Events OLD VALUES
    public static final String EVENT_LOGOUT = "Logout";
    public static final String EVENT_LOGIN_START = "Login_Start";
    public static final String EVENT_LOGIN_OK = "Login_Success";
    public static final String EVENT_LOGIN_FAILED = "Login_Failed";
    public static final String EVENT_LOGIN_ERROR = "Login_Error";
    public static final String EVENT_TUTORIAL_START = "Log_Tutorial_Start";
    public static final String EVENT_TUTORIAL_END = "Log_Tutorial_End";
    public static final String EVENT_LOGIN_SEND_NEW_CODE = "Login_Send_New_Code";
    public static final String EVENT_LOGIN_SLIDESHOW = "SwipeTutorial";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_OK = "Newsletter_Inscription_OK";
    public static final String EVENT_NEWSLETTER_INSCRIPTION_FAILED = "Newsletter_Inscription_Failed";
    public static final String EVENT_SPLASH_SIGNUP = "SplashSignUp";
    public static final String EVENT_SPLASH_LOGIN = "SplashLogIn";
    public static final String EVENT_PHONE_SUBMIT = "TelephoneSubmit";
    public static final String EVENT_PHONE_SUBMIT_FAIL = "TelephoneSubmitFail";
    public static final String EVENT_PHONE_SUBMIT_ERROR = "TelephoneSubmitError";
    public static final String EVENT_SMS_CODE_REQUEST = "SMSCodeRequest";
    public static final String EVENT_EMAIL_SUBMIT = "EmailSubmit";
    public static final String EVENT_EMAIL_SUBMIT_ERROR = "EmailSubmitError";
    public static final String EVENT_EMAIL_IGNORE = "IgnoreEmail";
    public static final String EVENT_NAME_SUBMIT = "NameSubmit";
    public static final String EVENT_NAME_SUBMIT_ERROR = "NameSubmitError";
    public static final String EVENT_NAME_TYPE = "NameType";
    public static final String EVENT_PHOTO_UPLOAD_SUBMIT = "PhotoUploadSubmit";
    public static final String EVENT_PHOTO_TAKE_SUBMIT = "PhotoTakeSubmit";
    public static final String EVENT_PHOTO_IGNORE = "IgnorePhoto";
    public static final String EVENT_PHOTO_BACK = "BackFromPhoto1";
    public static final String EVENT_PHOTO_SUBMIT = "SubmitInstantPhoto";
    public static final String EVENT_NOTIFICATIONS_ACCEPT = "AcceptNotifications";
    public static final String EVENT_NOTIFICATIONS_POPUP_ACCEPT = "AcceptNotificationsFromPopup";
    public static final String EVENT_NOTIFICATIONS_POPUP_REFUSE = "RefuseNotificationsFromPopup";
    public static final String EVENT_GEOLOCATION_ACCEPT = "AcceptGeoloc";
    public static final String EVENT_GEOLOCATION_POPUP_ACCEPT = "AcceptGeolocFromPopup";
    public static final String EVENT_GEOLOCATION_POPUP_REFUSE = "RefuseGeolocFromPopup";
    public static final String EVENT_GEOLOCATION_ACTIVATE_04_4A = "ActivateGeolocFromScreen04_4UserBlocked";
    public static final String EVENT_GEOLOCATION_ACTION_ZONE_04_4A = "ActionZoneFromScreen04_4UserBlocked";
    public static final String EVENT_WELCOME_CONTINUE = "WelcomeScreenContinue";

    // SCREEN Events
    public static final String EVENT_SCREEN_01 = "Screen01SplashView";
    public static final String EVENT_SCREEN_02_1 = "Screen02OnboardingLoginView";
    public static final String EVENT_SCREEN_03_1 = "Screen03_1OnboardingCodeResendView";
    public static final String EVENT_SCREEN_03_2 = "Screen03_2OnboardingPhoneNotFoundView";
    public static final String EVENT_SCREEN_04 = "Screen04_GoEnableGeolocView"; //Not implemented on Android
    public static final String EVENT_SCREEN_04_3 = "Screen04_3OnboardingNotificationsView";
    public static final String EVENT_SCREEN_30_1 = "Screen30_1WelcomeView";
    public static final String EVENT_SCREEN_30_2 = "Screen30_2InputPhoneView";
    public static final String EVENT_SCREEN_30_2_E = "Screen30_2PhoneAlreadyExistsError";
    public static final String EVENT_SCREEN_30_3 = "Screen30_3InputPasscodeView";
    public static final String EVENT_SCREEN_30_4 = "Screen30_4InputEmailView";
    public static final String EVENT_SCREEN_30_5 = "Screen30_5InputNameView";
    public static final String EVENT_SCREEN_09_1_ME = "Screen09_1MyProfileViewAsPublicView";
    public static final String EVENT_SCREEN_09_1_OTHER = "Screen09_1OtherUserProfileView";
    public static final String EVENT_SCREEN_09_2 = "Screen09_2EditMyProfileView";
    public static final String EVENT_SCREEN_09_4 = "Screen09_4EditPasswordView";
    public static final String EVENT_SCREEN_09_4_SUBMIT = "Screen09_4ChangePasswordSubmit";
    public static final String EVENT_SCREEN_09_5 = "Screen09_5EditNameView";
    public static final String EVENT_SCREEN_09_6 = "Screen09_6ChoosePhotoView";
    public static final String EVENT_SCREEN_09_7 = "Screen09_7TakePhotoView";
    public static final String EVENT_SCREEN_09_8 = "Screen09_8ConfirmPhotoView";
    public static final String EVENT_SCREEN_09_9 = "Screen09_9MovePhotoView";
    public static final String EVENT_SCREEN_06_1 = "Screen06_1FeedView";
    public static final String EVENT_SCREEN_06_2 = "Screen06_2MapView";
    public static final String EVENT_SCREEN_17_2 = "Screen17_2MyMessagesView";

    //MENU Events
    public static final String EVENT_PROFILE_FROM_MENU = "Open_Profile_From_Menu";
    public static final String EVENT_OPEN_GUIDE_FROM_TAB = "Open_Guide_From_Tab";
    public static final String EVENT_OPEN_TOURS_FROM_MENU = "Open_Tours_From_Menu";
    public static final String EVENT_OPEN_FEED_FROM_TAB = "Open_NewsFeed_From_Tab";
    public static final String EVENT_MENU_TAP_MY_PROFILE = "TapMyProfilePhoto";
    public static final String EVENT_MENU_LOGOUT = "LogOut";
    public static final String EVENT_MENU_ABOUT = "AboutClick";
    public static final String EVENT_MENU_GOAL = "WhatActionsClick";
    public static final String EVENT_ABOUT_FAQ = "AppFAQClick";
    public static final String EVENT_MENU_BLOG = "SimpleCommeBonjourClick";
    public static final String EVENT_MENU_CHART = "ViewEthicsChartClick";
    public static final String EVENT_MENU_ATD = "ATDPartnershipView";
    public static final String EVENT_MENU_DONATION = "DonationView";
    public static final String EVENT_MENU_AMBASSADOR = "AmbassadorProgramClick";

    //FEED Events
    public static final String EVENT_FEED_MESSAGES = "GoToMessages"; // No longer used
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
    public static final String EVENT_FEED_ACTION_CREATE_CLICK = "CreateActionClick";
    public static final String EVENT_FEED_PENDING_OVERLAY = "PendingRequestOverlay";
    public static final String EVENT_FEED_CANCEL_JOIN_REQUEST = "CancelJoinRequest";
    public static final String EVENT_FEED_OPEN_ACTIVE_OVERLAY = "OpenActiveOverlay";
    public static final String EVENT_FEED_QUIT_ENTOURAGE = "QuitFromFeed";
    public static final String EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR = "ActivateGeolocFromCreateTourPopup";
    public static final String EVENT_FEED_ACTIVATE_GEOLOC_RECENTER = "ActivateGeolocFromRecenterPopup";
    public static final String EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER = "ActivateGeolocFromGuideRecenterPopup";
    public static final String EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER = "ActivateGeolocFromBanner";
    public static final String EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER = "ActivateGeolocFromGuideBanner";
    public static final String EVENT_FEED_TAB_ALL = "ShowAllFeed";
    public static final String EVENT_FEED_TAB_EVENTS = "ShowEventFeed";
    public static final String TOUR_FEED_TAB_EVENTS = "ShowTourFeed";

    //MAP Events
    public static final String EVENT_MAP_MAPVIEW_CLICK = "MapViewClick";
    public static final String EVENT_MAP_LISTVIEW_CLICK = "ListViewClick";
    public static final String EVENT_MAP_PLUS_CLICK = "PlusFromMapClick";
    public static final String EVENT_MAP_LONGPRESS = "HiddenButtonsOverlayPress";
    public static final String EVENT_MAP_ZOOM_IN = "ZoomIn";
    public static final String EVENT_MAP_ZOOM_OUT = "ZoomOut";
    public static final String EVENT_MAP_SHIFT_CENTER = "MapShiftCenter"; //Not able to detect if it's an automatic or manual shift

    //GUIDE Events
    public static final String EVENT_GUIDE_POI_VIEW = "POIView";
    public static final String EVENT_GUIDE_PLUS_CLICK = "PlusFromGuideClick";
    public static final String EVENT_GUIDE_LONGPRESS = "LongPressFromGuideClick";
    public static final String EVENT_GUIDE_PROPOSE_POI = "ProposePOIView";
    public static final String EVENT_GUIDE_LIST_VIEW = "GDSListViewClick";
    public static final String EVENT_GUIDE_MAP_VIEW = "GDSMapViewClick";

    //SEND JOIN REQUEST Events
    public static final String EVENT_JOIN_REQUEST_START = "StartJoinMessage";
    public static final String EVENT_JOIN_REQUEST_SUBMIT = "SubmitJoinMessage";
    public static final String EVENT_JOIN_REQUEST_ACCEPT = "AcceptJoinRequest";
    public static final String EVENT_JOIN_REQUEST_REJECT = "RejectJoinRequest";

    //ENTOURAGE VIEW Events
    public static final String EVENT_ENTOURAGE_DISCUSSION_VIEW = "Screen14_1DiscussionView";
    public static final String EVENT_ENTOURAGE_PUBLIC_VIEW_MEMBER = "Screen14_2PublicPageViewAsMember";
    public static final String EVENT_ENTOURAGE_PUBLIC_VIEW_NONMEMBER = "Screen14_2PublicPageViewAsNonMember";
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
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_SUCCESS = "SuccessfulClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_FAILURE = "BlockedClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_HELP = "HelpRequestOnClosePopup";
    public static final String EVENT_ENTOURAGE_CLOSE_POPUP_CANCEL = "CancelClosePopup";
    public static final String EVENT_ENTOURAGE_SHARE_MEMBER = "ShareLinkAsMemberOrCreator";
    public static final String EVENT_ENTOURAGE_SHARE_NONMEMBER = "ShareLinkAsNonMember";

    //MY ENTOURAGES Events
    public static final String EVENT_MYENTOURAGES_BANNER_CLICK = "BannerMessageClick";
    public static final String EVENT_MYENTOURAGES_MESSAGE_OPEN = "MessageOpen";
    public static final String EVENT_MYENTOURAGES_FILTER_CLICK = "MessagesFilterClick";
    public static final String EVENT_MYENTOURAGES_BANNER_MOVE = "MoveBannerClick"; //A lot of code needs to be written to detect this
    public static final String EVENT_MYENTOURAGES_BACK_CLICK = "BackToFeedClick";

    //MY ENTOURAGES FILTER Events
    public static final String EVENT_MYENTOURAGES_FILTER_EXIT = "ExitMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_SAVE = "SaveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ACTIVE = "ActiveMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_INVITATIONS = "InvitationsFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ORGANIZER = "OrganizerFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_UNREAD = "UnreadMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_PAST = "ExcludeClosedEntouragesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_ASK = "AskMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_OFFER = "OfferMessagesFilter";
    public static final String EVENT_MYENTOURAGES_FILTER_TOUR = "TourMessagesFilter";

    //ENTOURAGE DISCLAIMER Events
    public static final String EVENT_ENTOURAGE_DISCLAIMER_CLOSE = "CloseEthicsPopupClick";
    public static final String EVENT_ENTOURAGE_DISCLAIMER_ACCEPT = "AcceptEthicsChartClick";
    public static final String EVENT_ENTOURAGE_DISCLAIMER_LINK = "LinkToEthicsChartClick";

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
    public static final String EVENT_CREATE_ENCOUNTER_CLICK = "CreateEncounterClick";
    public static final String EVENT_CREATE_ENCOUNTER_START = "Open_Create_Encounter_From_Tour";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_OK = "Encounter_Voice_Message_Recorded_OK";
    public static final String EVENT_CREATE_ENCOUNTER_OK = "Encounter_Created";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_STARTED = "Encounter_VoiceMsgRecord_Started";
    public static final String EVENT_CREATE_ENCOUNTER_VOICE_MESSAGE_NOT_SUPPORTED = "Encounter_VoiceMsgRecord_Not_Supported";
    public static final String EVENT_CREATE_ENCOUNTER_FAILED = "Encounter_Create_Failed";

    //MAP Filter Events
    public static final String EVENT_MAP_FILTER_FILTER1 = "ClickFilter1Value";
    public static final String EVENT_MAP_FILTER_FILTER2 = "ClickFilter2Value";
    public static final String EVENT_MAP_FILTER_FILTER3 = "ClickFilter3Value";
    public static final String EVENT_MAP_FILTER_ONLY_MINE = "ShowOnlyMineFilter";
    public static final String EVENT_MAP_FILTER_ONLY_TOURS = "ShowToursOnlyFilterClick";
    public static final String EVENT_MAP_FILTER_ONLY_OFFERS = "ShowOnlyOffersClick";
    public static final String EVENT_MAP_FILTER_ONLY_ASK = "ShowOnlyAsksClick";
    public static final String EVENT_MAP_FILTER_ONLY_MEDICAL_TOURS = "ShowOnlyMedicalToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_SOCIAL_TOURS = "ShowOnlySocialToursClick";
    public static final String EVENT_MAP_FILTER_ONLY_DISTRIBUTION_TOURS = "ShowOnlyDistributionToursClick";
    public static final String EVENT_MAP_FILTER_ACTION_CATEGORY = "FilterActionSubtypeClick";
    public static final String EVENT_MAP_FILTER_SUBMIT = "SubmitFilterPreferences";
    public static final String EVENT_MAP_FILTER_CLOSE = "CloseFilter";

    // USER Events
    public static final String EVENT_USER_EDIT_PROFILE = "EditMyProfile";
    public static final String EVENT_USER_EDIT_PHOTO = "EditPhoto";
    public static final String EVENT_USER_ROTATE_PHOTO = "RotatePhoto";
    public static final String EVENT_USER_SAVE = "SaveProfileEdits";
    public static final String EVENT_USER_SAVE_FAILED = "SaveProfileEditFailed";
    public static final String EVENT_USER_TOBADGE = "ToBadgePageFromProfile";
    public static final String EVENT_USER_TONOTIFICATIONS = "ToNotifications";

    // SHORTCUT Events
    public static final String EVENT_SHORTCUT_SHAREAPP = "ShareShortcut";
    public static final String EVENT_SHORTCUT_RATEAPP = "RateShortcut";
    // ABOUT Events
    public static final String EVENT_ABOUT_RATING = "RatingClick";
    public static final String EVENT_ABOUT_FACEBOOK = "FacebookPageClick";
    public static final String EVENT_ABOUT_WEBSITE = "WebsiteVisitClick";
    public static final String EVENT_ABOUT_CGU = "CGUClick";
    public static final String EVENT_ABOUT_TUTORIAL = "OpenTutorialFromMenu";
    public static final String EVENT_ABOUT_PRIVACY = "OpenPrivacyFromAbout";
    public static final String EVENT_ABOUT_SUGGESTION = "OpenSuggestionFromAbout";
    public static final String EVENT_ABOUT_FEEDBACK = "OpenFeedbackFromAbout";
    public static final String EVENT_ABOUT_OSS = "OpenOpenSourceLibrariesFromAbout";
    public static final String EVENT_ABOUT_EMAIL = "OpenEmailFromAbout";

    // Encounter Popup While Tour Events
    public static final String EVENT_ENCOUNTER_POPUP_SHOW = "SwitchToEncounterPopupView";
    public static final String EVENT_ENCOUNTER_POPUP_ENCOUNTER = "SwitchToCreateEncounter";
    public static final String EVENT_ENCOUNTER_POPUP_ENTOURAGE = "ContinueCreatePublicEntourage";

    //Notifications
    public static final String EVENT_NOTIFICATION_RECEIVED="NotificationReceived";
    public static final String EVENT_NOTIFICATION_FCM_RECEIVED="NotificationReceivedFromFCM";
    public static final String EVENT_NOTIFICATION_ENTOURAGE_RECEIVED="NotificationReceivedFromEntourage";

    //PLUS Screen
    public static final String EVENT_PLUS_NOT_READY = "NotReadyToHelpClick";

    public static String TAG = EntourageEvents.class.getSimpleName();

    public static void logEvent(String event) {
        EntourageApplication.get().getFirebase().logEvent(event, null);
    }

    static void onLocationPermissionGranted(boolean isPermissionGranted) {
        FirebaseAnalytics mFirebaseAnalytics = EntourageApplication.get().getFirebase();
        String geolocStatus = isPermissionGranted? "YES":"NO";
        mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", geolocStatus);
    }

    static void updateUserInfo(User user, Context context, boolean areNotificationsEnabled) {
        /* TODO: catch this event elsewhere
        if (areNotificationsEnabled) {
            logEvent(EntourageEvents.EVENT_GEOLOCATION_POPUP_REFUSE);
        } else {
            logEvent(EntourageEvents.EVENT_GEOLOCATION_POPUP_ACCEPT);
        }
         */
        FirebaseAnalytics mFirebaseAnalytics = EntourageApplication.get().getFirebase();
        mFirebaseAnalytics.setUserId(String.valueOf(user.getId()));

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setUserId(String.valueOf(user.getId()));
        //crashlytics.setUserName(user.getDisplayName());

        mFirebaseAnalytics.setUserProperty("EntourageUserType", user.isPro()?"Pro":"Public");

        mFirebaseAnalytics.setUserProperty("Language", Locale.getDefault().getLanguage());

        if(user.getPartner()!=null) {
            mFirebaseAnalytics.setUserProperty("EntouragePartner", user.getPartner().getName());
        }

        if(user.getFirebaseProperties()!=null) {
            mFirebaseAnalytics.setUserProperty(User.UserFirebaseProperties.actionZoneCPName, user.getFirebaseProperties().getActionZoneCP());
            mFirebaseAnalytics.setUserProperty(User.UserFirebaseProperties.actionZoneDepName, user.getFirebaseProperties().getActionZoneDep());
        }

        String geolocStatus="NO";
        if (LocationUtils.INSTANCE.isLocationPermissionGranted()) {
            geolocStatus = "YES";
        }
        mFirebaseAnalytics.setUserProperty("EntourageGeolocEnable", geolocStatus);

        final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
        boolean notificationsEnabled = sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, true);
        mFirebaseAnalytics.setUserProperty("EntourageNotifEnable", notificationsEnabled && areNotificationsEnabled ?"YES":"NO");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mFirebaseAnalytics.setUserProperty("BackgroundRestriction", ((ActivityManager) Objects.requireNonNull(context.getSystemService(Context.ACTIVITY_SERVICE))).isBackgroundRestricted()?"YES":"NO");
        }
    }
}
