package social.entourage.android

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.IdRes
import social.entourage.android.about.VoisinageAboutFragment
import social.entourage.android.api.AppRequest
import social.entourage.android.api.UserRequest
import social.entourage.android.privateCircle.PrivateCircleChooseFragment
import javax.inject.Inject

/**
 * Presenter controlling the MainActivity
 * @see MainActivity
 */
class MainPresenter  @Inject internal constructor(activity: MainActivity?, appRequest: AppRequest?, userRequest: UserRequest?) : MainBasePresenter(activity, appRequest, userRequest) {
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    // ----------------------------------
    // MENU HANDLING
    // ----------------------------------
    public override fun handleMenu(@IdRes menuId: Int) {
        if (activity == null) return
        when (menuId) {
            R.id.action_update_info -> {
                val privateCircleChooseFragment = PrivateCircleChooseFragment()
                privateCircleChooseFragment.show(activity.supportFragmentManager, PrivateCircleChooseFragment.TAG)
            }
            R.id.action_contact -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:")
                val addresses = arrayOf(activity.getString(R.string.contact_email))
                intent.putExtra(Intent.EXTRA_EMAIL, addresses)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    Toast.makeText(activity, R.string.error_no_email, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_feedback -> activity.showWebViewForLinkId(Constants.FEEDBACK_ID)
            R.id.action_propose -> Toast.makeText(activity, R.string.error_not_yet_implemented, Toast.LENGTH_SHORT).show()
            R.id.action_how_to -> {
                val howToIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.how_to_url)))
                try {
                    activity.startActivity(howToIntent)
                } catch (ex: Exception) {
                    Toast.makeText(activity, R.string.no_browser_error, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_about -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_MENU_ABOUT)
                val aboutFragment = VoisinageAboutFragment()
                aboutFragment.show(activity.supportFragmentManager, VoisinageAboutFragment.TAG)
            }
            else -> super.handleMenu(menuId)
        }
    }
}