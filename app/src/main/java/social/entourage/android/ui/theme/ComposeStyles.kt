package social.entourage.android.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import social.entourage.android.R

// Font Families
val NunitoSansRegular = FontFamily(Font(R.font.nunitosans_regular, FontWeight.Normal))
val NunitoSansSemiBold = FontFamily(Font(R.font.nunitosans_semibold, FontWeight.SemiBold))
val NunitoSansItalic = FontFamily(Font(R.font.nunitosans_italic, FontWeight.Normal, FontStyle.Italic))
val NunitoSansBold = FontFamily(Font(R.font.nunitosans_bold, FontWeight.Bold))
val NunitoSansLight = FontFamily(Font(R.font.nunitosans_light, FontWeight.Light))
val NunitoSansExtraLight = FontFamily(Font(R.font.nunitosans_extralight, FontWeight.ExtraLight))
val QuicksandBold = FontFamily(Font(R.font.quicksand_bold, FontWeight.Bold))

// Colors (mapped from colors.xml or literal values from styles.xml)
val ColorPrimary = Color(0xFFFFFFFF)
val ColorPrimaryDark = Color(0xFF717171)
val ColorAccent = Color(0xFFFF9739)
val ColorOrange = Color(0xFFFF9739)
val ColorLightOrange = Color(0xFFFF9C5D)
// Orange foncé pour du texte sur fond clair, utilisé seulement en mode "contraste amélioré" (contraste >= 4.5:1)
val ColorOrangeText = Color(0xFFA8530A)

/** Texte orange sur fond clair : couleur d'origine, ou orange foncé en mode contraste amélioré. */
fun accessibleOrange(original: Color): Color =
    if (social.entourage.android.accessibility.AccessibilitySettings.highContrast) ColorOrangeText else original
val ColorBlack = Color(0xFF000000)
val ColorWhite = Color(0xFFFFFFFF)
val ColorGrey = Color(0xFF707070)
val ColorNewLightGrey = Color(0xFFCDCDCD)
val ColorDeletedGrey = Color(0xFFA8A8A8)
val ColorRed = Color(0xFFFF0000)
val ColorGreyishBrown = Color(0xFF4A4A4A)
val ColorPreOnboardBlack = Color(0xFF1E1E1E)
val ColorOnboardBlack36 = Color(0xFF363636)
val ColorLightGrey = Color(0xFFEDEDED)

/**
 * EntourageComposeStyles maps the existing XML styles from styles.xml
 * to Compose TextStyle objects for consistent UI design.
 */
object EntourageComposeStyles {

    // --- Base Application Styles ---
    
    val entourageTitle = TextStyle(
        fontSize = 18.sp, // @dimen/entourage_font_large
        color = ColorGreyishBrown
    )

    val userProfileTextViewStyle = TextStyle(
        fontSize = 18.sp, // @dimen/entourage_font_large
        color = ColorGreyishBrown
    )

    val entourageDisclaimerSectionTitle = TextStyle(
        fontSize = 20.sp,
        color = ColorGreyishBrown
    )

    val entourageDisclaimerSectionDetails = TextStyle(
        fontSize = 18.sp, // @dimen/entourage_font_large
        color = ColorGreyishBrown
    )

    val mapActionSubMenu = TextStyle(
        fontSize = 14.sp, // @dimen/entourage_font_small
        color = ColorGreyishBrown
    )

    val mapActionMenu: TextStyle get() = TextStyle(
        fontSize = 16.sp, // @dimen/entourage_font_medium
        fontWeight = FontWeight.Bold,
        color = accessibleOrange(ColorAccent)
    )

    val plusImageSubtitle = TextStyle(
        fontSize = 17.sp,
        color = ColorGreyishBrown
    )

    val userProfileTextViewStylePwdEditText = TextStyle(
        fontSize = 16.sp, // @dimen/entourage_font_medium
        color = ColorBlack
    )

    val customEditTextThemePwdEditView = TextStyle(
        fontSize = 16.sp, // @dimen/entourage_font_medium
        color = ColorBlack
    )

    // --- Profile & Header Styles ---

    val profileName = TextStyle(
        fontFamily = NunitoSansSemiBold,
        fontSize = 20.sp, // @dimen/profile_name
        color = ColorBlack
    )

    val associationName: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val profileDescription = TextStyle(
        fontFamily = NunitoSansItalic,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val profileInformationTitle: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val profileInterest: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val orangeButton: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorOrange)
    )

    val profileSubtitles: TextStyle get() = TextStyle(
        fontFamily = NunitoSansLight,
        fontSize = 14.sp, // @dimen/profile_titles
        color = accessibleOrange(ColorOrange)
    )

    val profileActivityStatistics = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 16.sp, // @dimen/profile_activity_statistics
        color = ColorBlack
    )

    val input = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/profile_labels
        color = ColorBlack
    )

    val h4 = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val h1 = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 24.sp, // @dimen/header_title
        color = ColorWhite
    )

    val h1Collapsed = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 18.sp, // @dimen/button_create_group
        color = ColorWhite
    )

    val h1Black = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 24.sp, // @dimen/header_title
        color = ColorBlack
    )

    val miniHeaderBlack = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 18.sp, // @dimen/mini_header_title
        color = ColorBlack
    )

    val tabText = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 14.sp, // @dimen/profile_titles
        color = ColorNewLightGrey
    )

    val settingsItems = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 14.sp // @dimen/settings_titles
    )

    val h2 = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val leftH2 = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val leftH2Orange: TextStyle get() = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorOrange)
    )

    val buttonCreateGroup = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 18.sp, // @dimen/button_create_group
        color = ColorWhite
    )

    val h2White = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorWhite
    )

    val h2Orange: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorOrange)
    )

    val h3Orange = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val selectedFilter = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 13.sp, // @dimen/profile_labels
        color = ColorWhite
    )

    val selectedFilterOrange: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val unselectedFilter: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val h2SeekBarProgression: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 13.sp, // @dimen/profile_labels
        color = accessibleOrange(ColorOrange)
    )

    val editImage: TextStyle get() = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 14.sp, // @dimen/profile_titles
        color = accessibleOrange(ColorOrange)
    )

    val headerCreateGroup = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 24.sp, // @dimen/header_title
        color = ColorBlack
    )

    val headerCreateGroupOrange: TextStyle get() = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 24.sp, // @dimen/header_title
        color = accessibleOrange(ColorLightOrange)
    )

    val errorMsg = TextStyle(
        fontFamily = NunitoSansItalic,
        fontSize = 13.sp, // @dimen/error_msg
        color = ColorRed
    )

    val report: TextStyle get() = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorLightOrange)
    )

    val leftLegend = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/profile_legend
        color = ColorGrey
    )

    val leftLegendGrey = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/profile_labels
        color = ColorGrey
    )

    val centerLegendGrey = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 10.sp, // @dimen/contributions_empty_label
        color = ColorGrey
    )

    val leftCourantLightOrange: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorLightOrange)
    )

    val leftCourantOrange = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorGrey
    )

    val groupMemberSubtitle: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/group_member_subtitle
        color = accessibleOrange(ColorLightOrange)
    )

    val groupMemberSubtitleBlack = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/group_member_subtitle
        color = ColorBlack
    )

    val leftCourantBoldOrange: TextStyle get() = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorOrange)
    )

    val leftCourantBlack = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val leftCourantBoldBlack = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val leftLargeBoldBlack = TextStyle(
        fontFamily = NunitoSansBold,
        fontSize = 30.sp, // @dimen/contributions_value
        color = ColorBlack
    )

    val versionNumber = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 9.sp, // @dimen/version_number
        color = ColorBlack
    )

    val notificationsItems = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorBlack
    )

    val leftDefault = TextStyle(
        fontFamily = NunitoSansLight,
        fontSize = 13.sp, // @dimen/profile_labels
        color = ColorDeletedGrey
    )

    val leftDate = TextStyle(
        fontFamily = NunitoSansExtraLight,
        fontSize = 9.sp, // @dimen/version_number
        color = ColorBlack
    )

    val leftDateOrange: TextStyle get() = TextStyle(
        fontFamily = NunitoSansExtraLight,
        fontSize = 9.sp, // @dimen/version_number
        color = accessibleOrange(ColorOrange)
    )

    val readItem: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/error_msg
        color = accessibleOrange(ColorOrange)
    )

    val watchedPedagogical: TextStyle get() = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp, // @dimen/group_member_subtitle
        color = accessibleOrange(ColorOrange)
    )

    val leftCourantBoldLightOrange: TextStyle get() = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = accessibleOrange(ColorLightOrange)
    )

    val leftCourantBoldWhite = TextStyle(
        fontFamily = QuicksandBold,
        fontSize = 15.sp, // @dimen/profile_description
        color = ColorWhite
    )

    val bottomNavigationActiveItem = TextStyle(
        fontFamily = NunitoSansLight,
        fontSize = 13.sp
    )

    val bottomNavigationInactive = TextStyle(
        fontFamily = NunitoSansLight,
        fontSize = 13.sp
    )

    val customChip = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 13.sp,
        color = ColorBlack
    )

    val entTextField = TextStyle(
        fontFamily = NunitoSansRegular,
        fontSize = 15.sp,
        color = ColorPreOnboardBlack
    )
}
