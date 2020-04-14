package social.entourage.android.sidemenu

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.entourage.layout_side_menu.*
import kotlinx.android.synthetic.entourage.layout_side_menu_appversion.*
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.view.EntourageSnackbar

/**
 * Side menu fragment
 */
class SideMenuFragment  : Fragment(R.layout.layout_side_menu) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusProvider.instance.register(this)
    }

    override fun onDestroy() {
        BusProvider.instance.unregister(this)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseView()
        updateUserView()
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun userInfoUpdated(event: Events.OnUserInfoUpdatedEvent?) {
        updateUserView()
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun initialiseView() {
        sidemenu_app_version?.text = getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME)
        sidemenu_app_debug_info?.text = getString(R.string.about_debug_info_format, BuildConfig.VERSION_DISPLAY_BRANCH_NAME, FirebaseInstanceId.getInstance().id)
        sidemenu_app_version?.setOnLongClickListener { v: View? -> handleLongPress() }
        sidemenu_app_debug_info?.setOnLongClickListener { v: View? -> handleLongPress() }

        //add listener to user photo and name, that opens the user profile screen
        drawer_header_user_photo?.setOnClickListener { v: View? -> selectMenuAction(R.id.action_user) }
        drawer_header_user_name?.setOnClickListener { v: View? -> selectMenuAction(R.id.action_user) }
        //add listener to modify profile text view
        action_edit_profile?.setOnClickListener { v: View? -> selectMenuAction(R.id.action_edit_profile) }

        //add listeners to side menu items
        if (sidemenuitems_layout != null) {
            val itemsCount = sidemenuitems_layout.childCount
            for (j in 0 until itemsCount) {
                (sidemenuitems_layout.getChildAt(j) as? SideMenuItemView)?.setOnClickListener { v: View -> selectMenuAction(v.id) }
            }
        }
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
        drawer_header_user_name?.text = user.displayName
        val avatarURL = user.avatarURL
        if (avatarURL != null && drawer_header_user_photo!=null) {
            Picasso.get()
                    .load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(drawer_header_user_photo!!)
        } else {
            drawer_header_user_photo?.setImageResource(R.drawable.ic_user_photo_small)
        }
        // Show partner logo
        var partnerURL: String? = null
        val partner = user.partner
        if (partner != null) {
            partnerURL = partner.smallLogoUrl
        }
        if (partnerURL != null && drawer_header_user_partner_logo!=null) {
            Picasso.get()
                    .load(Uri.parse(partnerURL))
                    .placeholder(R.drawable.partner_placeholder)
                    .transform(CropCircleTransformation())
                    .into(drawer_header_user_partner_logo!!)
        } else {
            drawer_header_user_partner_logo!!.setImageDrawable(null)
        }
        // Show Update Private Circle item only if the user is member of any
        //TODO ADD THIS TO PFP : action_update_info?.visibility = if (user.getMemberships(Entourage.TYPE_NEIGHBORHOOD).size > 0) View.VISIBLE else View.GONE
        // Changed the ethics charter text depending on signed/unsigned
        action_charte?.setTitle(if (user.hasSignedEthicsCharter()) R.string.action_charter_signed else R.string.action_charter_unsigned)
    }

    private fun selectMenuAction(action: Int) {
        if (activity == null || activity !is MainActivity) return
        (activity as MainActivity).selectItem(action)
    }

    private fun handleLongPress(): Boolean {
        selectMenuAction(R.id.sidemenu_app_version)
        if(sideMenuCoordinatorLayout != null) {
            EntourageSnackbar.make(
                    sideMenuCoordinatorLayout,
                    R.string.debug_info_clipboard,
                    Snackbar.LENGTH_SHORT
            ).show()
        }
        return true
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = SideMenuFragment::class.java.simpleName
    }
}