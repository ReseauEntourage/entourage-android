package social.entourage.android.mainprofile

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_mainprofile.*
import kotlinx.android.synthetic.main.layout_mainprofile_appversion.*
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.tape.Events
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar

/**
 * Side menu fragment
 */
class MainProfileFragment  : Fragment(R.layout.layout_mainprofile) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EntBus.register(this)
    }

    override fun onDestroy() {
        EntBus.unregister(this)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseView()
        updateUserView()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent (AnalyticsEvents.VIEW_PROFILE_MENU)
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
        mainprofile_app_version?.setOnLongClickListener { handleLongPress() }
        if (!BuildConfig.DEBUG) {
            mainprofile_app_debug_info?.visibility=View.INVISIBLE
        } else {
            mainprofile_app_debug_info?.visibility=View.VISIBLE
            mainprofile_app_debug_info?.text = getString(R.string.about_debug_info_format, BuildConfig.VERSION_DISPLAY_BRANCH_NAME,
                EntourageApplication.get().sharedPreferences.getString(EntourageApplication.KEY_REGISTRATION_ID, null))
            mainprofile_app_debug_info?.setOnLongClickListener { handleLongPress() }
        }
        //add listener to user photo and name, that opens the user profile screen
        drawer_header_user_photo?.setOnClickListener { selectMenuProfile("user") }
        drawer_header_user_name?.setOnClickListener { selectMenuProfile("user") }
        //add listener to modify profile text view
        action_edit_profile?.setOnClickListener { selectMenuProfile("editProfile") }

        //add listeners to side menu items

        ui_layout_show_events?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PROFILE_SHOWEVENTS)
            showEvents()
        }
        ui_layout_show_actions?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PROFILE_SHOWACTIONS)
            showActions()
        }

        ui_layout_charte?.setOnClickListener {
            selectMenuProfile("charte")
        }
        ui_layout_guide?.setOnClickListener {
            selectMenuProfile("scb")
        }

        ui_layout_goodwaves?.setOnClickListener {
            selectMenuProfile("goodWaves")
        }
        ui_layout_ambassador?.setOnClickListener {
            selectMenuProfile("ambassador")
        }
        ui_layout_linkedout?.setOnClickListener {
            selectMenuProfile("linkedout")
        }
        ui_layout_donate?.setOnClickListener {
            selectMenuProfile("donation")
        }

        ui_layout_share?.setOnClickListener {
            selectMenuProfile("share")
        }
        ui_layout_blog?.setOnClickListener {
            selectMenuProfile("blog")
        }

        ui_layout_logout?.setOnClickListener {
            selectMenuProfile("logout")
        }

        ui_layout_help?.setOnClickListener {
            selectMenuProfile("help")
        }

        ui_iv_fb?.setOnClickListener {
            selectMenuProfile("fb")
        }
        ui_iv_insta?.setOnClickListener {
            selectMenuProfile("insta")
        }
        ui_iv_twit?.setOnClickListener {
            selectMenuProfile("twit")
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
        ui_tv_charte?.setText(if (user.hasSignedEthicsCharter()) R.string.action_charter_signed else R.string.action_charter_unsigned)

        //Show hide join Good waves
        if (user.stats?.isGoodWavesValidated == true) {
            ui_layout_goodwaves?.visibility = View.GONE
        }
        else {
            ui_layout_goodwaves?.visibility = View.VISIBLE
        }

        ui_tv_nb_actions?.text = user.stats?.actionsCount?.let { "$it" } ?: "0"
        ui_tv_nb_events?.text = user.stats?.eventsCount?.let { "$it" } ?: "0"

        val isExpertMode = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_HOME_IS_EXPERTMODE,false)
        ui_switch_change_mode?.isChecked = !isExpertMode

        if (user.isUserTypeNeighbour) {
            ui_layout_change_mode?.visibility = View.VISIBLE
            ui_switch_change_mode?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_SWITCH_ExpertToNeo)
                }
                else {
                    AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_SWITCH_NeoToExpert)
                }
                EntourageApplication.get().sharedPreferences.edit()
                        .putBoolean(EntourageApplication.KEY_HOME_IS_EXPERTMODE, !isChecked)
                        .remove("isNavNews")
                        .remove("navType")
                        .apply()
            }
        }
        else {
            ui_layout_change_mode?.visibility = View.GONE
        }
    }

    private fun selectMenuProfile(position: String) {
        (activity as? MainActivity)?.selectMenuProfileItem(position)
    }

    private fun showEvents() {
       (activity as? MainActivity)?.showEvents()
    }
    private fun showActions() {
        (activity as? MainActivity)?.showAllActions()
    }

    private fun handleLongPress(): Boolean {
        selectMenuProfile("appVersion")
        if(mainProfileCoordinatorLayout != null) {
            EntSnackbar.make(
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
        val TAG: String = MainProfileFragment::class.java.simpleName
    }
}