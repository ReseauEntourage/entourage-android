package social.entourage.android.user

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import com.bumptech.glide.Glide
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.user_photo
import kotlinx.android.synthetic.main.fragment_user_edit.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.R
import social.entourage.android.api.model.EntourageConversation
import social.entourage.android.api.model.User
import social.entourage.android.api.tape.Events.OnPartnerViewRequestedEvent
import social.entourage.android.api.tape.Events.OnUserInfoUpdatedEvent
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.UserEditAboutFragment
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.photo.ChoosePhotoFragment.Companion.newInstance
import social.entourage.android.user.partner.PartnerFragment
import social.entourage.android.user.report.UserReportFragment
import timber.log.Timber
import javax.inject.Inject

class UserFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var toReturn: View? = null

    @Inject lateinit var presenter: UserPresenter

    private var user: User? = null
    private var isMyProfile = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_user, container, false)
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_PROFILE_FROM_MENU)
        return toReturn
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get(activity).components)
        val requestedUserId = arguments?.getInt(User.KEY_USER_ID) ?: return
        val authenticatedUser = presenter.authenticatedUser
        if (authenticatedUser != null && requestedUserId == authenticatedUser.id) {
            isMyProfile = true
            user = authenticatedUser
            configureView()
        } else {
            user_profile_progressBar?.visibility = View.VISIBLE
            presenter.getUser(requestedUserId)
        }
        title_close_button?.setOnClickListener { onCloseButtonClicked()}
        user_profile_edit_button?.setOnClickListener { onEditButtonClicked() }
        user_photo?.setOnClickListener { onUserPhotoClicked() }
        user_name?.setOnClickListener { onEditProfileClicked() }
        user_identification_email_layout?.setOnClickListener { onEditProfileClicked() }
        user_identification_phone_layout?.setOnClickListener { onEditProfileClicked() }
        user_number_of_entourages_layout?.setOnClickListener { onEditProfileClicked() }
        user_profile_report_button?.setOnClickListener { onReportUserClicked() }
        user_message_button?.setOnClickListener { onMessageUserClicked() }
        user_photo_button?.setOnClickListener { onPhotoEditClicked() }
        user_about_edit_button?.setOnClickListener { onAboutEditClicked() }
    }

    private fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerUserComponent.builder()
                .entourageComponent(entourageComponent)
                .userModule(UserModule(this))
                .build()
                .inject(this)
    }

    override fun onStart() {
        super.onStart()
        EntBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        EntBus.unregister(this)
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun configureView() {
        if (activity?.isFinishing==true) return
        AnalyticsEvents.logEvent(if (isMyProfile) AnalyticsEvents.EVENT_SCREEN_09_1_ME else AnalyticsEvents.EVENT_SCREEN_09_1_OTHER)

        user_profile_edit_button?.visibility = if (isMyProfile) View.VISIBLE else View.GONE
        user_profile_report_button?.visibility = if (isMyProfile) View.GONE else View.VISIBLE
        user_about_edit_button?.visibility = if (isMyProfile) View.VISIBLE else View.GONE
        user_photo?.let { userPhoto ->
            user?.avatarURL?.let { avatarURL ->
                Glide.with(this)
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo)
                        .circleCrop()
                        .into(userPhoto)
            } ?: run  {
                Glide.with(this)
                        .load(R.drawable.ic_user_photo)
                        .circleCrop()
                        .into(userPhoto)
            }
        }
        // Show the partner logo, if available
        user_partner_logo?.let { logoView ->
            user?.partner?.smallLogoUrl?.let {partnerURL->
                Glide.with(this)
                        .load(Uri.parse(partnerURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .circleCrop()
                        .into(logoView)
            } ?: run {
                logoView.setImageDrawable(null)
            }
        }
        user_photo_button?.visibility = if (isMyProfile) View.VISIBLE else View.GONE
        user?.let { u ->
            user_name?.setText(u.displayName)
            user_name?.setRoles(u.roles)
            user_tours_count?.text = getString(R.string.user_entourage_count_format, u.stats?.getActionCount() ?: 0)
            val userAbout = u.about ?: ""
            user_profile_about_layout?.visibility = if (userAbout.isNotBlank()) View.VISIBLE else View.GONE
            ui_tv_user_description?.text = userAbout

            ui_tv_nb_actions?.text = u.stats?.actionsCount?.let { "$it" } ?: "0"
            ui_tv_nb_events?.text = u.stats?.eventsCount?.let { "$it" } ?: "0"

            ui_tv_good_waves?.visibility = if (u.stats?.isGoodWavesValidated == true) View.VISIBLE else View.GONE

            user_identification_phone_check?.setImageResource(R.drawable.verified)
            user_identification_email_check?.setImageResource(if (u.email != null) R.drawable.verified else R.drawable.not_verified)
            user_profile_associations?.initUserAssociations(u, this)

            //User message layout is available only for the other users, if the conversation field is set
            user_message_layout?.visibility = if (isMyProfile || u.conversation == null) View.GONE else View.VISIBLE
        }
    }

    private fun showUserEditFragment() {
        // Allow editing only of the logged user and if enabled in configuration
        if (!(isMyProfile && Configuration.showUserEditProfile())) return
        // Show the edit profile screen
        try {
            UserEditFragment().show(parentFragmentManager, UserEditFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    private fun displayToast(@StringRes messageResIs: Int) {
        activity?.let {Toast.makeText(it, messageResIs, Toast.LENGTH_SHORT).show() }
    }

    fun saveAccount(user: User) {
        presenter.updateUser(user)
    }

    val editedUser: User?
        get() = user?.clone()

    // ----------------------------------
    // Presenter Callbacks
    // ----------------------------------
    fun onUserReceived(user: User) {
        if (activity?.isFinishing == true) {
            return
        }
        user_profile_progressBar?.visibility = View.GONE
        this.user = user
        configureView()
    }

    fun onUserReceivedError() {
        if (activity?.isFinishing == true) {
            return
        }
        user_profile_progressBar?.visibility = View.GONE
        displayToast(R.string.user_retrieval_error)
    }

    fun onUserUpdatedError() {
        if (activity?.isFinishing == true) {
            return
        }
        displayToast(R.string.user_text_update_ko)
    }

    fun onUserUpdated(user: User) {
        if (activity?.isFinishing == true) {
            return
        }
        //update the current view
        this.user = user
        configureView()
        //update the edit view, if available
        (parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) as UserEditFragment?)?.dismiss()
        displayToast(R.string.user_text_update_ok)
    }

    fun onConversationFound(entourage: EntourageConversation) {
        try {
            val entourageInformationFragment = FeedItemInformationFragment.newInstance(entourage, 0, 0)
            entourageInformationFragment.hideInfoButton()
            entourageInformationFragment.show(parentFragmentManager, FeedItemInformationFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.e(e)
        }
    }

    fun onConversationNotFound() {}

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------
    private fun onCloseButtonClicked() {
        dismiss()
    }

    private fun onEditButtonClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_EDIT_PROFILE)
        showUserEditFragment()
    }

    private fun onUserPhotoClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_USER_EDIT_PHOTO)
        showUserEditFragment()
    }

    fun onEditProfileClicked() {
        showUserEditFragment()
    }

    private fun onReportUserClicked() {
        user?.id?.let { UserReportFragment.newInstance(it).show(parentFragmentManager, UserReportFragment.TAG)}
    }

    private fun onMessageUserClicked() {
        //UserDiscussionFragment.newInstance(user, false).show(parentFragmentManager, UserDiscussionFragment.TAG);
        user?.conversation?.let { presenter.getConversation(it) }
    }

    private fun onPhotoEditClicked() {
        newInstance().show(parentFragmentManager, ChoosePhotoFragment.TAG)
    }

    private fun onAboutEditClicked() {
        UserEditAboutFragment().show(parentFragmentManager, UserEditAboutFragment.TAG)
    }

    // ----------------------------------
    // Events Handling
    // ----------------------------------
    @Subscribe
    fun userInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        if (!isAdded) {
            return
        }
        //update the current view
        user = EntourageApplication.me(activity)
        configureView()
    }

    @Subscribe
    fun onPartnerViewRequested(event: OnPartnerViewRequestedEvent?) {
        if (activity?.isFinishing == true) {
            return
        }
        if (event == null) {
            return
        }
        try {
            // Because we are handling this event in the user edit fragment too, we need to make sure that there is no active user edit fragment
            if (parentFragmentManager.findFragmentByTag(UserEditFragment.TAG) != null) {
                return
            }
            PartnerFragment.newInstance(event.partner).show(parentFragmentManager,PartnerFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "fragment_user"

        fun newInstance(userId: Int): UserFragment {
            val userFragment = UserFragment()
            val args = Bundle()
            args.putInt(User.KEY_USER_ID, userId)
            userFragment.arguments = args
            return userFragment
        }
    }
}