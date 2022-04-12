package social.entourage.android.user.edit

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_user_edit.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.BaseOrganization
import social.entourage.android.api.model.User
import social.entourage.android.api.tape.Events.OnPartnerViewRequestedEvent
import social.entourage.android.api.tape.Events.OnUserInfoUpdatedEvent
import social.entourage.android.base.BaseActivity
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.UserOrganizationsAdapter
import social.entourage.android.user.edit.partner.UserEditPartnerFragment
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment.FragmentListener
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject

open class UserEditFragment  : BaseDialogFragment(), FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    internal var presenter: UserEditPresenter = UserEditPresenter(this)

    private var scrollViewY = 0

    private var organizationsAdapter: UserOrganizationsAdapter? = null
    var isShowAction = false
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           isShowAction = it.getBoolean(ARG_IS_SHOW_ACTION,false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_09_2)
        return inflater.inflate(R.layout.fragment_user_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title_close_button?.setOnClickListener { onCloseButtonClicked() }
        user_firstname_layout?.setOnClickListener { onEditFirstname() }
        user_lastname_layout?.setOnClickListener { onEditFirstname() }
        user_email_layout?.setOnClickListener { onEditEmail() }
        user_password_layout?.setOnClickListener { onEditPassword() }
        user_about_edit_button?.setOnClickListener { onEditAboutClicked() }
        user_delete_account_button?.setOnClickListener { onDeleteAccountClicked() }
        title_action_button?.setOnClickListener { presenter.updateUser()  }
        user_photo_button?.setOnClickListener { onPhotoClicked() }
        user_add_association_button?.setOnClickListener { onAddAssociationClicked() }
        user_notifications_layout?.setOnClickListener { onShowNotificationsSettingsClicked() }

        ui_iv_action_zone1_mod?.setOnClickListener {
            onActionZoneEditClicked(false)
        }
        ui_iv_action_zone2_mod?.setOnClickListener {
            onActionZoneEditClicked(true)
        }

        ui_iv_action_zone2_delete?.setOnClickListener {
            presenter.deleteSecondaryAddress()
        }

        ui_profile_add_zone?.setOnClickListener {
            if(presenter.editedUser?.address!=null) {
                onActionAddSecondaryZone()
            } else { // if no first Zone is set, we set it first
                onActionZoneEditClicked(false)
            }
        }

        ui_iv_action_type_mod?.setOnClickListener {
           onActionSelectType()
        }

        initUserData()

        if (isShowAction) {
            onActionSelectType()
        }
    }

    override fun onStart() {
        super.onStart()
        configureNotifications()
        EntBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        EntBus.unregister(this)
    }

    fun initUserData() {
        presenter.editedUser?.let {user->
            user_photo?.let { userPhoto ->
                user.avatarURL?.let { avatarURL->
                    Glide.with(this)
                            .load(Uri.parse(avatarURL))
                            .placeholder(R.drawable.ic_user_photo)
                            .circleCrop()
                            .into(userPhoto)
                } ?: run {
                    Glide.with(this)
                            .load(R.drawable.ic_user_photo)
                            .circleCrop()
                            .into(userPhoto)
                }
            }
            user_edit_firstname?.text = user.firstName
            user_edit_lastname?.text = user.lastName
            user_email?.text = user.email
            user_phone?.text = user.phone
            user_about?.text = user.about

            user_about_edit_button?.text = if (!user.about.isNullOrBlank()) getString(R.string.user_about_mod_button) else  getString(R.string.user_about_edit_button)

            val organizationList: MutableList<BaseOrganization> = ArrayList()
            user.partner?.let { organizationList.add(it) }
            user.organization?.let { organizationList.add(it) }
            if (organizationList.size > 0) {
                organizationsAdapter?.setOrganizationList(organizationList)
                        ?: run {
                    user_associations_view?.layoutManager = LinearLayoutManager(context)
                    organizationsAdapter = UserOrganizationsAdapter(organizationList)
                    user_associations_view?.adapter = organizationsAdapter
                }
                user_associations_layout?.visibility = View.VISIBLE
            }
            user.address?.let { address->
                ui_tv_action_zone1_title?.text = address.displayAddress
            } ?: run {
                ui_tv_action_zone1_title?.text = getString(R.string.user_edit_action_zone_button_no_address)
            }

            user.addressSecondary?.let {addressSecondary ->
                ui_tv_action_zone2_title?.text = addressSecondary.displayAddress
                ui_layout_2nd_zone?.visibility = View.VISIBLE
                ui_layout_add_zone?.visibility = View.GONE
            } ?: run {
                ui_layout_2nd_zone?.visibility = View.GONE
                ui_layout_add_zone?.visibility = View.VISIBLE
            }

            //Type
            ui_tv_action_type_desc?.text = when(user.goal) {
                User.USER_GOAL_NEIGHBOUR -> getString(R.string.onboard_type_choice_neighbour)
                User.USER_GOAL_ALONE -> getString(R.string.onboard_type_choice_alone)
                User.USER_GOAL_ASSO -> getString(R.string.onboard_type_choice_asso)
                else -> {getString(R.string.profile_action_not_selected) }
            }

            val interests = user.getFormattedInterests(requireContext())
            ui_tv_action_type_title?.text = String.format(getString(R.string.profile_activities),interests)
        }
    }

    private fun displayToast(message: String?) {
        activity?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    fun saveNewPassword(newPassword: String?) {
        presenter.saveNewPassword(newPassword ?: "").also { user_edit_progressBar?.visibility = View.VISIBLE }
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------
    private fun onCloseButtonClicked() {
        dismiss()
    }

    private fun onEditFirstname() {
        showEditProfile(UserEditProfileFragment.EDIT_NAME)
    }

    private fun onEditEmail() {
        showEditProfile(UserEditProfileFragment.EDIT_EMAIL)
    }

    private fun onEditPassword() {
        UserEditPasswordFragment().show(parentFragmentManager, UserEditPasswordFragment.TAG)
    }

    private fun onEditAboutClicked() {
        scrollView?.let { scrollViewY = it.scrollY }
        UserEditAboutFragment().show(parentFragmentManager, UserEditAboutFragment.TAG)
    }

    private fun onDeleteAccountClicked() {
        activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.user_delete_account_dialog)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        (it as BaseActivity?)?.showProgressDialog(0)
                        presenter.deleteAccount()
                    }
                    .setNegativeButton(R.string.no, null)
            builder.show()
        }
    }

    private fun onPhotoClicked() {
        ChoosePhotoFragment.newInstance().show(parentFragmentManager, ChoosePhotoFragment.TAG)
    }

    fun onAddAssociationClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_TOBADGE)
        UserEditPartnerFragment().show(parentFragmentManager, UserEditPartnerFragment.TAG)
    }

    private fun onShowNotificationsSettingsClicked() {
        activity?.let {
            try {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_TONOTIFICATIONS)
                val intent = Intent()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, it.packageName)
                    }
                    else -> {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", it.packageName)
                        intent.putExtra("app_uid", it.applicationInfo.uid)
                    }
                }
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Timber.e(ex, "Cannot open Notifications Settings page")
            } catch (ignored: Exception) {
            }
        }
    }

    private fun onActionSelectType() {
         UserEditProfileType().show(parentFragmentManager, UserEditProfileType.TAG)
    }

    private fun onActionAddSecondaryZone() {
        val frag = UserEditActionZoneFragment.newInstance(null, true)
        frag.setupListener(this)
        frag.show(parentFragmentManager, UserEditActionZoneFragment.TAG)
    }

    // ----------------------------------
    // Protected methods
    // ----------------------------------
    fun scrollToOriginalPosition() {
        scrollView?.scrollTo(0, scrollViewY)
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun showEditProfile(editType: Int) {
        UserEditProfileFragment.newInstance(editType).show(parentFragmentManager, UserEditProfileFragment.TAG)
    }

    private fun configureNotifications() {
        val areNotificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        user_notifications_image?.setImageResource(if (areNotificationsEnabled) R.drawable.verified else R.drawable.not_verified)
    }

    // ----------------------------------
    // Events Handling
    // ----------------------------------
    @Subscribe
    fun userInfoUpdated(event: OnUserInfoUpdatedEvent) {
        activity?.let {
            if (it.isFinishing) {
                return
            }
            presenter.initEditedUser()
        }
    }

    @Subscribe
    fun onPartnerViewRequested(event: OnPartnerViewRequestedEvent?) {
        if (activity?.isFinishing == true) {
            return
        }
        if (event == null) {
            return
        }
        PartnerFragment.newInstance(event.partner).show(parentFragmentManager, PartnerFragment.TAG)
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------
    fun onUserUpdated(user: User) {
        if (activity?.isFinishing == true || this.isDetached) {
            return
        }
        try {
            displayToast(getString(R.string.user_text_update_ok))
            dismiss()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun onUserUpdateError() {
        if (activity?.isFinishing == true || isDetached) {
            return
        }
        displayToast(getString(R.string.user_text_update_ko))
    }

    fun onSavePasswordError() {
        activity?.let {
            if (it.isFinishing) {
                return
            }
            displayToast(getString(R.string.user_text_update_ko))
            user_edit_progressBar?.visibility = View.GONE
        }
    }

    fun onSaveNewPassword() {
        activity?.let {
            if (it.isFinishing) {
                return
            }
            displayToast(getString(R.string.user_text_update_ok))
            user_edit_progressBar?.visibility = View.GONE
        }
    }

    fun onDeletedAccount(success: Boolean) {
        (activity as? BaseActivity)?.let {
            val hasActivity = !it.isFinishing
            if (hasActivity) {
                it.dismissProgressDialog()
            }
            if (success) {
                //logout and go back to login screen
                if (it is MainActivity) {
                    it.selectMenuProfileItem("logout")
                }
            } else if (hasActivity) {
                Toast.makeText(it, R.string.user_delete_account_failure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------------
    // Edit Action Zone
    // ----------------------------------
    private fun onActionZoneEditClicked(isSecondary: Boolean) {
        //ATTENTION both addresses can be null when they are not set yet !
        val frag = UserEditActionZoneFragment.newInstance(
                if (isSecondary) presenter.editedUser?.addressSecondary else presenter.editedUser?.address,
                isSecondary
        )
        frag.setupListener(this)
        frag.show(parentFragmentManager, UserEditActionZoneFragment.TAG)
    }

    override fun onUserEditActionZoneFragmentDismiss() {}
    override fun onUserEditActionZoneFragmentAddressSaved() {
        presenter.storeActionZone(false)
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        presenter.storeActionZone(true)
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "user_edit_fragment"
        const val ARG_IS_SHOW_ACTION = "isShowAction"
        fun newInstance(isShowAction:Boolean) =
                UserEditFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_IS_SHOW_ACTION,isShowAction)
                    }
                }
    }
}