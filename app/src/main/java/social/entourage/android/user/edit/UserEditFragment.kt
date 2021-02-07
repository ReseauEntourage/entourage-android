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
import com.squareup.otto.Subscribe
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_edit.*
import kotlinx.android.synthetic.main.layout_view_title.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.*
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.BaseOrganization
import social.entourage.android.api.model.User
import social.entourage.android.api.tape.Events.OnPartnerViewRequestedEvent
import social.entourage.android.api.tape.Events.OnUserInfoUpdatedEvent
import social.entourage.android.base.BaseActivity
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.user.partner.PartnerFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.UserFragment
import social.entourage.android.user.UserOrganizationsAdapter
import social.entourage.android.user.edit.UserEditActionZoneFragment.FragmentListener
import social.entourage.android.user.edit.partner.UserEditPartnerFragment
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.photo.PhotoChooseSourceFragmentCompat
import timber.log.Timber
import java.util.*
import javax.inject.Inject

private const val ARG_IS_SHOW_ACTION = "isShowAction"
open class UserEditFragment  : BaseDialogFragment(), FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var presenter: UserEditPresenter? = null

    private var scrollViewY = 0

    private var organizationsAdapter: UserOrganizationsAdapter? = null
    var editedUser: User? = null
        private set

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
        setupComponent(EntourageApplication.get(activity).entourageComponent)
        title_close_button?.setOnClickListener { onCloseButtonClicked() }
        user_firstname_layout?.setOnClickListener { onEditFirstname() }
        user_lastname_layout?.setOnClickListener { onEditFirstname() }
        user_email_layout?.setOnClickListener { onEditEmail() }
        user_password_layout?.setOnClickListener { onEditPassword() }
        user_about_edit_button?.setOnClickListener { onEditAboutClicked() }
        user_delete_account_button?.setOnClickListener { onDeleteAccountClicked() }
        title_action_button?.setOnClickListener { onSaveButtonClicked()  }
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
            onDeleteSecondaryAddress()
        }

        ui_profile_add_zone?.setOnClickListener {
            if(editedUser?.address!=null) {
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

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerUserEditComponent.builder()
                .entourageComponent(entourageComponent)
                .userEditModule(UserEditModule(this))
                .build()
                .inject(this)
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
        if (editedUser == null) {
            editedUser = EntourageApplication.me(activity)?.clone() ?: return
        }
        editedUser?.let {user->
            user_photo?.let { userPhoto ->
                user.avatarURL?.let { avatarURL->
                    Picasso.get().load(Uri.parse(avatarURL))
                            .placeholder(R.drawable.ic_user_photo)
                            .transform(CropCircleTransformation())
                            .into(userPhoto)
                } ?: run {
                    Picasso.get().load(R.drawable.ic_user_photo)
                            .transform(CropCircleTransformation())
                            .into(userPhoto)
                }
            }
            user_edit_firstname?.text = user.firstName
            user_edit_lastname?.text = user.lastName
            user_email?.text = user.email
            user_phone?.text = user.phone
            user_about?.text = user.about

            if (user.about.isNotEmpty()) {
                user_about_edit_button?.text = getString(R.string.user_about_mod_button)
            }
            else {
                user_about_edit_button?.text = getString(R.string.user_about_edit_button)
            }

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
                User.USER_GOAL_NEIGHBOUR -> getString(R.string.onboard_type_choice1)
                User.USER_GOAL_ALONE -> getString(R.string.onboard_type_choice2)
                User.USER_GOAL_ASSO -> getString(R.string.onboard_type_choice3)
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
        presenter?.saveNewPassword(newPassword ?: "").also { user_edit_progressBar?.visibility = View.VISIBLE }
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
                        presenter?.deleteAccount()
                    }
                    .setNegativeButton(R.string.no, null)
            builder.show()
        }
    }

    private fun onSaveButtonClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_SAVE)
        editedUser?.let { user->
            // If we have an user fragment in the stack, let it handle the update
            (parentFragmentManager.findFragmentByTag(UserFragment.TAG) as UserFragment?)?.saveAccount(user)
                ?: run {
                    presenter?.updateUser(user)
                }
        }
    }

    private fun onPhotoClicked() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            PhotoChooseSourceFragmentCompat().show(parentFragmentManager, PhotoChooseSourceFragmentCompat.TAG)
        } else {
            ChoosePhotoFragment.newInstance().show(parentFragmentManager, ChoosePhotoFragment.TAG)
        }
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
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", it.packageName)
                        intent.putExtra("app_uid", it.applicationInfo.uid)
                    }
                    else -> {
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + it.packageName)
                    }
                }
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Timber.e(ex, "Cannot open Notifications Settings page")
            } catch (ignored: Exception) {
            }
        }
    }

    private fun onDeleteSecondaryAddress() {
        val userRequest = EntourageApplication.get().entourageComponent.userRequest
        val call = userRequest.deleteSecondaryAddressLocation()

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
                    authenticationController.me?.let { me->
                        me.addressSecondary = null
                        authenticationController.saveUser(me)
                        initUserData()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun onActionSelectType() {
         val frag = UserEditProfileType()
         frag.show(parentFragmentManager, UserEditProfileType.TAG)
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
    fun userInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        activity?.let {
            if (it.isFinishing) {
                return
            }
            EntourageApplication.me(it)?.let { me ->
                editedUser?.let { user ->
                    user.avatarURL = me.avatarURL
                    user.partner = me.partner
                    user.address = me.address
                    user.addressSecondary = me.addressSecondary
                    user.interests.clear()
                    user.interests.addAll(me.interests)
                    user.goal = me.goal
                    initUserData()
                }
            }
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
        if (activity?.isFinishing == true) {
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

    fun onSaveNewPassword(newPassword: String) {
        activity?.let {
            if (it.isFinishing) {
                return
            }
            editedUser?.smsCode = newPassword
            displayToast(getString(R.string.user_text_update_ok))
            user_edit_progressBar?.visibility = View.GONE
        }
    }

    fun onDeletedAccount(success: Boolean) {
        activity?.let {
            val hasActivity = !it.isFinishing && it is BaseActivity
            if (hasActivity) {
                (it as BaseActivity?)?.dismissProgressDialog()
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
        editedUser?.let { user ->
            //ATTENTION both addresses can be null when they are not set yet !
            val frag = UserEditActionZoneFragment.newInstance(
                    if (isSecondary) user.addressSecondary else user.address,
                    isSecondary
            )
            frag.setupListener(this)
            frag.show(parentFragmentManager, UserEditActionZoneFragment.TAG)
        }
    }

    override fun onUserEditActionZoneFragmentDismiss() {}
    override fun onUserEditActionZoneFragmentAddressSaved() {
        storeActionZone(false)
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        storeActionZone(true)
    }

    private fun storeActionZone(ignoreActionZone: Boolean) {
        EntourageApplication.get().entourageComponent.authenticationController.isIgnoringActionZone = ignoreActionZone

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            (parentFragmentManager.findFragmentByTag(UserEditActionZoneFragmentCompat.TAG) as UserEditActionZoneFragmentCompat?)?.let { userEditActionZoneFragmentCompat ->
                if (!userEditActionZoneFragmentCompat.isStateSaved) {
                    userEditActionZoneFragmentCompat.dismiss()
                }
            }
        } else {
            (parentFragmentManager.findFragmentByTag(UserEditActionZoneFragment.TAG) as UserEditActionZoneFragment?)?.let { userEditActionZoneFragment ->
                if (!userEditActionZoneFragment.isStateSaved) {
                    userEditActionZoneFragment.dismiss()
                }
            }
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "user_edit_fragment"
        fun newInstance(isShowAction:Boolean) =
                UserEditFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(ARG_IS_SHOW_ACTION,isShowAction)
                    }
                }
    }
}