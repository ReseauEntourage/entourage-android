package social.entourage.android

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.about.EntourageAboutFragment
import social.entourage.android.api.AppRequest
import social.entourage.android.api.UserRequest
import javax.inject.Inject

/**
 * Presenter controlling the MainActivity
 * @see MainActivity
 */
class MainPresenter @Inject internal constructor(
        activity: MainActivity,
        appRequest: AppRequest,
        userRequest: UserRequest) : MainBasePresenter(activity, appRequest, userRequest) {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    public override fun handleMenu(menuId: Int) {
        when (menuId) {
            R.id.action_ambassador -> {
                if(activity==null) return
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_AMBASSADOR)
                val ambassadorIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getLink(Constants.AMBASSADOR_ID)))
                try {
                    activity.startActivity(ambassadorIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.mainprofile_app_version -> {
                val clipboardManager = get().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("FirebaseID", FirebaseInstanceId.getInstance().id)
                clipboardManager.setPrimaryClip(clipData)
            }
            R.id.action_about -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_ABOUT)
                if(activity==null) return
                EntourageAboutFragment().show(activity.supportFragmentManager, EntourageAboutFragment.TAG)
            }
            else -> super.handleMenu(menuId)
        }
    }
}