package social.entourage.android.base

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.deeplinks.UniversalLinkManager
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.WebViewFragment
import timber.log.Timber
import java.net.URL
import social.entourage.android.report.DataLanguageStock

/**
 * Base activity which set up a scoped graph and inject it
 */
abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    val entApp: EntourageApplication?
        get()= (application as? EntourageApplication)
    private val universalLinkManager = UniversalLinkManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        entApp?.onActivityCreated(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        entApp?.onActivityDestroyed(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        fromNotifLogFirebaseEvent()

    }

    fun showWebView(url: String, shareMessageRes: Int = 0) {
        if((url.contains("www.entourage.social") || url.contains("preprod.entourage.social")) && !url.contains("propose-poi")) {
            val uri = Uri.parse(url)
            universalLinkManager.handleUniversalLink(uri)
            return
        }
        if(shareMessageRes!=0 || !WebViewFragment.launchURL(this, url, shareMessageRes)) {
            WebViewFragment.newInstance(url, shareMessageRes, false)
                .show(supportFragmentManager, WebViewFragment.TAG)
        }
    }

    fun showWebViewForLinkId(linkId: String, shareMessageRes: Int = 0) {
        val link = getLink(linkId)
        showWebView(link, shareMessageRes)
    }

    open fun getLink(linkId: String): String {
        return getString(R.string.redirect_link_no_token_format, BuildConfig.ENTOURAGE_URL, linkId)
    }

     fun updateLanguage(){
        DataLanguageStock.updateUserLanguage(LanguageManager.loadLanguageFromPreferences(this))
        val savedLanguage = LanguageManager.loadLanguageFromPreferences(this)
        LanguageManager.setLocale(this, savedLanguage)
    }

    //TODO REFACTOR THIS
    fun fromNotifLogFirebaseEvent(){
        try {
            val notificationContent = Gson().fromJson(intent.getStringExtra("notification_content"), PushNotificationContent::class.java)
            val notificationBoolean= intent.getBooleanExtra("notification_content_boolean", false)
            val stage = notificationContent.extra?.stage
            if(stage.equals("h1")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay1)}
            if(stage.equals("j2")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay2)}
            if(stage.equals("j5")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay5)}
            if(stage.equals("j8")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay8)}
            if(stage.equals("j11")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay11)}
            val tracking = notificationContent.extra?.tracking
            if(tracking != null) {
                if(tracking.equals("join_request_on_create")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)}
                if(tracking.equals("outing_on_update")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__ModifiedEvent)}
                if(tracking.equals("outing_on_create")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostEvent)}
                if(tracking.equals("post_on_create_to_neighborhood")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostGroup)}
                if(tracking.equals("comment_on_create_to_neighborhood")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentGroup)}
                if(tracking.equals("comment_on_create_to_outing")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentEvent)}
                if(tracking.equals("outing_on_add_to_neighborhood")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__EventInGroup)}
                if(tracking.equals("contribution_on_create")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Contribution)}
                if(tracking.equals("solicitation_on_create")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Demand)}
                if(tracking.equals("private_chat_message_on_create")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PrivateMessage)}
                if(tracking.equals("join_request_on_create_to_neighborhood")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberGroup)}
                if(tracking.equals("join_request_on_create_to_outing")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)}
                if(tracking.equals("outing_on_cancel")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CanceledEvent)}
                if(tracking.equals("post_on_create_to_outing")){
                    AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PostEvent)}
                if(tracking.equals("public_chat_message_on_create")){
                    AnalyticsEvents.logEvent("UNDEFINED_PUSH_TRACKING")}
            }

        }catch (e:Exception){
            Timber.e("failed parse notif")
        }
    }
}