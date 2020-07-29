package social.entourage.android.mainprofile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_mainprofile.*
import kotlinx.android.synthetic.main.layout_mainprofile_appversion.*
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.tools.view.EntourageSnackbar

/**
 * Side menu fragment
 */
class MainProfileFragment  : Fragment(R.layout.layout_mainprofile) {

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

    override fun onResume() {
        super.onResume()
        EntourageEvents.logEvent (EntourageEvents.VIEW_PROFILE_MENU)
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
        mainprofile_app_version?.text = getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME)
        mainprofile_app_debug_info?.text = getString(R.string.about_debug_info_format, BuildConfig.VERSION_DISPLAY_BRANCH_NAME, FirebaseInstanceId.getInstance().id)
        mainprofile_app_version?.setOnLongClickListener { handleLongPress() }
        mainprofile_app_debug_info?.setOnLongClickListener { handleLongPress() }

        //add listener to user photo and name, that opens the user profile screen
        drawer_header_user_photo?.setOnClickListener { selectMenuAction(R.id.action_user) }
        drawer_header_user_name?.setOnClickListener { selectMenuAction(R.id.action_user) }
        //add listener to modify profile text view
        action_edit_profile?.setOnClickListener { selectMenuAction(R.id.action_edit_profile) }

        //add listeners to side menu items
        if (mainprofile_items_layout != null) {
            val itemsCount = mainprofile_items_layout.childCount
            for (j in 0 until itemsCount) {
                (mainprofile_items_layout.getChildAt(j) as? MainProfileItemView)?.setOnClickListener { v: View -> selectMenuAction(v.id) }
            }
        }

        ui_layout_show_events?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_SHOWEVENTS)
            showEvents()
        }
        ui_layout_show_actions?.setOnClickListener {
            EntourageEvents.logEvent(EntourageEvents.ACTION_PROFILE_SHOWACTIONS)
            showActions()
        }
    }

    private fun updateUserView() {
        val user = EntourageApplication.me(activity) ?: return
        drawer_header_user_name?.text = user.displayName

        drawer_header_user_photo?.let { photoView ->
            user.avatarURL?.let {avatarURL ->
                Picasso.get()
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .transform(CropCircleTransformation())
                        .into(photoView)
            } ?: run {
                photoView.setImageResource(R.drawable.ic_user_photo_small)
            }
        }
        // Show partner logo
        drawer_header_user_partner_logo?.let {logoView->
            user.partner?.smallLogoUrl?.let { partnerURL ->
                Picasso.get()
                        .load(Uri.parse(partnerURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(CropCircleTransformation())
                        .into(logoView)
            } ?: run {
                logoView.setImageDrawable(null)
            }
        }

        // Changed the ethics charter text depending on signed/unsigned
        action_charte?.setTitle(if (user.hasSignedEthicsCharter()) R.string.action_charter_signed else R.string.action_charter_unsigned)

        //Show hide join Good waves
        if (user.stats?.isGoodWavesValidated == true) {
            action_good_waves?.visibility = View.GONE
        }
        else {
            action_good_waves?.visibility = View.VISIBLE
        }

        ui_tv_nb_actions?.text = if(user.stats?.actionsCount != null) "${user.stats!!.actionsCount}" else "0"
        ui_tv_nb_events?.text = if(user.stats?.eventsCount != null) "${user.stats!!.eventsCount}" else "0"

    }

    private fun selectMenuAction(action: Int) {
        (activity as? MainActivity)?.selectItem(action)
    }

    private fun showEvents() {
       (activity as? MainActivity)?.showEvents()
    }
    private fun showActions() {
        (activity as? MainActivity)?.showAllActions()
    }

    private fun handleLongPress(): Boolean {
        selectMenuAction(R.id.mainprofile_app_version)
        if(mainProfileCoordinatorLayout != null) {
            EntourageSnackbar.make(
                    mainProfileCoordinatorLayout,
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
        val TAG = MainProfileFragment::class.java.simpleName
    }
}