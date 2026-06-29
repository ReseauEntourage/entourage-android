package social.entourage.android.profile

import social.entourage.android.badges.loadBadgeSvg
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserBlockedUser
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.databinding.ActivityLayoutProfileBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.home.HomePresenter
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.association.AssociationProfileActivity
import social.entourage.android.profile.settings.ProfilFullViewModel
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.UserPresenter
import timber.log.Timber
import java.text.SimpleDateFormat
import kotlin.random.Random

class ProfileFullActivity : BaseSecuredActivity() {

    private lateinit var binding: ActivityLayoutProfileBinding
    private var user: User? = null
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    private lateinit var profilFullViewModel: ProfilFullViewModel
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private var notifSubTitle = ""
    private var notifBlocked = ""
    private var id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutProfileBinding.inflate(layoutInflater)
        binding.containerProfile.visibility = View.GONE
        profilFullViewModel = ViewModelProvider(this).get(ProfilFullViewModel::class.java)
        userPresenter.user.observe(this, ::updateUser)
        homePresenter.notificationsPermission.observe(this, ::updateNotifParam)
        discussionsPresenter.getBlockedUsers.observe(this, ::handleResponseBlocked)
        profilFullViewModel.hasToUpdate.observe(this, ::updateProfile)
        discussionsPresenter.newConversation.observe(this, ::handleGetConversation)
        binding.progressBar.visibility = View.VISIBLE

        id = intent.getIntExtra(Const.USER_ID, 0)

        initUserInfo()
        setModifyButton()
        setScrollEffects()
        setBackButton()
        setConfettiView()

        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        updatePaddingTopForEdgeToEdge(binding.profileContent)
    }

    private fun initUserInfo() {
        discussionsPresenter.getBlockedUsers()
        initializeStats()
        updateUserView()
        setButtonListeners()
        setPartnerClickListener()
        setSignalButton()
    }

    override fun onResume() {
        super.onResume()
        userPresenter.getUser(id)
        if (user == null) {
            binding.progressBar.visibility = View.VISIBLE
            Timber.e("user is null in resume Profile Screen")
        }
        EnhancedOnboarding.isFromSettingsWishes = false
        EnhancedOnboarding.isFromSettingsDisponibility = false
        EnhancedOnboarding.isFromSettingsinterest = false
        EnhancedOnboarding.isFromSettingsActionCategorie = false
    }

    private fun handleResponseBlocked(blockedUsers: MutableList<UserBlockedUser>?) {
        if (blockedUsers.isNullOrEmpty()) {
            notifBlocked = getString(R.string.settings_unblock_contacts_subtitle)
        } else {
            notifBlocked = getString(R.string.settings_number_blocked_contacts_subtitle) + blockedUsers.size
        }
        homePresenter.getNotificationsPermissions()
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            DetailConversationActivity.isSmallTalkMode = false
            startActivity(
                Intent(this, DetailConversationActivity::class.java)
                    .putExtras(
                        bundleOf(
                            Const.ID to conversation.id,
                            Const.POST_AUTHOR_ID to conversation.user?.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.NAME to conversation.title,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true,
                            Const.HAS_TO_SHOW_MESSAGE to conversation.hasToShowFirstMessage()
                        )
                    )
            )
        }
    }

    private fun updateNotifParam(notifsPermissions: InAppNotificationPermission?) {
        notifsPermissions?.let {
            notifSubTitle = ""
            if (it.action) {
                notifSubTitle += getString(R.string.notifications_actions) + ", "
            }
            if (it.outing) {
                notifSubTitle += getString(R.string.notifications_events) + ", "
            }
            if (it.neighborhood) {
                notifSubTitle += getString(R.string.notifications_groups) + ", "
            }
            if (it.chat_message) {
                notifSubTitle += getString(R.string.notifications_messages) + ", "
            }
        }
        if (notifSubTitle.isNotEmpty()) {
            notifSubTitle = notifSubTitle.substring(0, notifSubTitle.length - 2)
            notifSubTitle = getString(R.string.settings_notifications_subtitle) + notifSubTitle
        } else {
            notifSubTitle = getString(R.string.no_notifications_active)
        }
        setupRecyclerView()
        binding.containerProfile.visibility = View.VISIBLE
    }

    private fun setConfettiView() {
        binding.layoutAchievement.setOnClickListener { _ ->
            //VibrationUtil.vibrate(this)
            //showConfetti(view)
        }
    }

    private fun updateUser(user: User) {
        notifSubTitle = ""
        notifBlocked = ""
        this.user = user
        initUserInfo()
        updateBadgesSection(user.badges ?: emptyList())
    }

    private fun updateBadgesSection(apiBadges: List<social.entourage.android.badges.ApiBadge>) {
        val obtained = social.entourage.android.badges.buildProgressFromApi(apiBadges).filter { it.isObtained }
        if (obtained.isEmpty()) {
            binding.sectionBadges.visibility = View.GONE
            return
        }

        binding.sectionBadges.visibility = View.VISIBLE
        binding.btnVoirBadges.visibility = View.GONE
        binding.badgesRow.removeAllViews()

        obtained.forEach { progress ->
            val cardView = layoutInflater.inflate(
                R.layout.item_badge_profile_card,
                binding.badgesRow,
                false
            )
            cardView.findViewById<android.widget.ImageView>(R.id.iv_badge_icon).loadBadgeSvg(progress.definition.svgRes)
            cardView.findViewById<android.widget.TextView>(R.id.tv_card_label).text =
                getString(progress.definition.titleRes)
            val tvProgress = cardView.findViewById<android.widget.TextView>(R.id.tv_card_progress)
            tvProgress.text = getString(R.string.badge_status_obtained)
            tvProgress.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.green))
            tvProgress.visibility = View.VISIBLE
            cardView.isClickable = false
            binding.badgesRow.addView(cardView)
        }
    }

    private fun setScrollEffects() {
        binding.profileNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val minScale = 0.3f
            val scale = (1f - scrollY / 500f).coerceIn(minScale, 1f)
            binding.ivProfile.scaleX = scale
            binding.ivProfile.scaleY = scale

            if (scale == minScale) {
                binding.ivProfile.visibility = View.GONE
            } else {
                binding.ivProfile.visibility = View.VISIBLE
            }
        }
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener {
            VibrationUtil.vibrate(this)
            this.finish()
        }
    }

    private fun setupRecyclerView() {
        user?.let { user ->
            val items = mutableListOf<ProfileSectionItem>()

            items.add(ProfileSectionItem.Separator(getString(R.string.preferences_section_title_others)))

            val interestsText = if (user.interests.isNotEmpty()) {
                user.interests.joinToString(", ") { interest ->
                    EventUtils.showTagTranslated(this, interest)
                }
            } else {
                getString(R.string.no_data_available)
            }
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_interests,
                    title = getString(R.string.preferences_interest_title_others),
                    subtitle = interestsText
                )
            )

            val isAsso = user.partner != null && (user.roles?.contains("Association") == true || user.roles?.contains("Équipe Entourage") == true)

            if (isAsso) {
                val orientationsText = if (user.orientations.isNotEmpty()) {
                    user.orientations.joinToString(", ") { orientation ->
                        when (orientation) {
                            "share" -> getString(R.string.enhanced_onboarding_asso_wish_outings)
                            "guide" -> getString(R.string.enhanced_onboarding_asso_wish_neighborhoods)
                            "help" -> getString(R.string.enhanced_onboarding_asso_wish_both_actions)
                            else -> getString(R.string.interest_other)
                        }
                    }
                } else {
                    getString(R.string.no_data_available)
                }
                items.add(
                    ProfileSectionItem.Item(
                        iconRes = R.drawable.ic_profile_action,
                        title = getString(R.string.preferences_action_title_others),
                        subtitle = orientationsText
                    )
                )
            } else {
                val involvementsText = if (user.involvements.isNotEmpty()) {
                    user.involvements.joinToString(", ") { involvement ->
                        when (involvement.lowercase()) {
                            "outings" -> getString(R.string.onboarding_action_wish_event)
                            "both_actions" -> getString(R.string.onboarding_action_wish_services)
                            "neighborhoods" -> getString(R.string.onboarding_action_wish_network)
                            "resources" -> getString(R.string.onboarding_action_wish_pedago)
                            else -> getString(R.string.interest_other)
                        }
                    }
                } else {
                    getString(R.string.no_data_available)
                }
                items.add(
                    ProfileSectionItem.Item(
                        iconRes = R.drawable.ic_profile_action,
                        title = getString(R.string.preferences_action_title_others),
                        subtitle = involvementsText
                    )
                )

                val categoriesMap = mapOf(
                    "sharing_time" to getString(R.string.onboarding_category_sharing_time),
                    "material_donations" to getString(R.string.onboarding_category_donation),
                    "services" to getString(R.string.onboarding_category_services)
                )
                val categoriesText = if (user.concerns.isNotEmpty()) {
                    user.concerns.joinToString(", ") { concern ->
                        categoriesMap[concern] ?: getString(R.string.interest_other)
                    }
                } else {
                    getString(R.string.no_data_available)
                }
                items.add(
                    ProfileSectionItem.Item(
                        iconRes = R.drawable.ic_name_don_materiel,
                        title = getString(R.string.preferences_action_categories_title_others),
                        subtitle = categoriesText
                    )
                )

                val daysMap = mapOf(
                    "1" to getString(R.string.enhanced_onboarding_time_disponibility_day_monday),
                    "2" to getString(R.string.enhanced_onboarding_time_disponibility_day_tuesday),
                    "3" to getString(R.string.enhanced_onboarding_time_disponibility_day_wednesday),
                    "4" to getString(R.string.enhanced_onboarding_time_disponibility_day_thursday),
                    "5" to getString(R.string.enhanced_onboarding_time_disponibility_day_friday),
                    "6" to getString(R.string.enhanced_onboarding_time_disponibility_day_saturday),
                    "7" to getString(R.string.enhanced_onboarding_time_disponibility_day_sunday)
                )
                val timeSlotsMap = mapOf(
                    "09:00-12:00" to getString(R.string.enhanced_onboarding_time_disponibility_time_morning),
                    "14:00-18:00" to getString(R.string.enhanced_onboarding_time_disponibility_time_afternoon),
                    "18:00-21:00" to getString(R.string.enhanced_onboarding_time_disponibility_time_evening)
                )
                val availabilityText = if (user.availability.isNotEmpty()) {
                    user.availability.entries.joinToString(" ; ") { (day, times) ->
                        val dayName = daysMap[day] ?: day
                        val timeSlots = times.joinToString(", ") { time ->
                            timeSlotsMap[time] ?: time
                        }
                        "$dayName : $timeSlots"
                    }
                } else {
                    getString(R.string.no_data_available)
                }
                items.add(
                    ProfileSectionItem.Item(
                        iconRes = R.drawable.ic_profile_availability,
                        title = getString(R.string.preferences_availability_title_others),
                        subtitle = availabilityText
                    )
                )
            }

            val adapter = SettingProfileFullAdapter(items, this, this.supportFragmentManager, false)
            binding.rvSectionProfile.layoutManager = LinearLayoutManager(this)
            binding.rvSectionProfile.adapter = adapter
        }
    }

    private fun initializeStats() {
        user?.let { user ->
            user.stats?.let { stats ->
                if (stats.neighborhoodsCount > 0) {
                    binding.contribContent.text = stats.neighborhoodsCount.toString()
                    binding.titleContrib.text = getString(R.string.contributions_group)
                    binding.contribContent.visibility = View.VISIBLE
                    binding.titleContrib.visibility = View.VISIBLE
                }
                if (stats.outingsCount > 0) {
                    binding.eventContent.text = stats.outingsCount.toString()
                    binding.titleEvent.text = getString(R.string.contributions_event)
                    binding.eventContent.visibility = View.VISIBLE
                    binding.titleEvent.visibility = View.VISIBLE
                }
                binding.iconContrib.setImageResource(R.drawable.icon_navbar_groupe_inactif)
                binding.iconEvent.setImageResource(R.drawable.icon_navbar_calendrier_inactif)
            }

            user.roles?.let { roles ->
                binding.tagUser.visibility =
                    if (roles.contains("Animateur Entourage") || roles.contains("Équipe Entourage") || roles.contains(
                            "Association"
                        )
                    ) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                binding.ivAssoBadge.visibility =
                    if (roles.contains("Équipe Entourage") || roles.contains("Association")) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                if (roles.contains("Animateur Entourage")) {
                    binding.tvTagHomeV2EventItem.text = getString(R.string.ambassador)
                    binding.ivAssoBadge.visibility = View.GONE
                } else if (roles.contains("Équipe Entourage")) {
                    binding.tvTagHomeV2EventItem.text = user.partner?.name
                    binding.ivAssoBadge.visibility = View.VISIBLE
                } else if (roles.contains("Association")) {
                    binding.tvTagHomeV2EventItem.text = user.partner?.name
                    binding.ivAssoBadge.visibility = View.VISIBLE
                }
            }
            user.createdAt?.let { createdAt ->
                val locale = LanguageManager.getLocaleFromPreferences(this)
                binding.joined.profileJoinedDate.text = SimpleDateFormat(
                    this.getString(R.string.profile_date_format),
                    locale
                ).format(createdAt)
                binding.joined.profileJoinedDate.visibility = View.VISIBLE
            } ?: run {
                binding.joined.profileJoinedDate.visibility = View.GONE
            }

            user.about?.let { about ->
                if (about.isNotBlank()) {
                    binding.tvDescription.text = about
                    binding.tvDescription.setTextColor(
                        ContextCompat.getColor(this, R.color.black)
                    )
                    binding.tvDescription.visibility = View.VISIBLE
                } else {
                    binding.tvDescription.text = this.getString(R.string.placeholder_description_profile)
                    binding.tvDescription.setTextColor(
                        ContextCompat.getColor(this, R.color.grey)
                    )
                    binding.tvDescription.visibility = View.VISIBLE
                }
            } ?: run {
                binding.tvDescription.text = this.getString(R.string.placeholder_description_profile)
                binding.tvDescription.setTextColor(
                    ContextCompat.getColor(this, R.color.grey)
                )
                binding.tvDescription.visibility = View.VISIBLE
            }
        }
        binding.appVersion.text =
            getString(
                R.string.about_version_format,
                getString(R.string.app_name),
                BuildConfig.VERSION_FULL_NAME
            )
        binding.appVersion.setOnLongClickListener {
            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(
                "FIId", EntourageApplication.get().sharedPreferences.getString(
                    EntourageApplication.KEY_REGISTRATION_ID,
                    null
                )
            )
            clipboard.setPrimaryClip(clip)

            val snackbar = EntSnackbar.make(
                binding.root,
                R.string.copied_text,
                Snackbar.LENGTH_SHORT
            )
            snackbar.show()
            true
        }
        if (!BuildConfig.DEBUG) {
            binding.appDebugInfo.visibility = View.INVISIBLE
        } else {
            binding.appDebugInfo.visibility = View.VISIBLE
            binding.appDebugInfo.text = getString(
                R.string.about_debug_info_format,
                BuildConfig.VERSION_DISPLAY_BRANCH_NAME,
                EntourageApplication.get().sharedPreferences.getString(
                    EntourageApplication.KEY_REGISTRATION_ID,
                    null
                )
            )
        }
        binding.progressBar.visibility = View.GONE

        binding.myActivityTv.text = getString(R.string.his_activity)
        binding.tvMail.visibility = View.GONE
        binding.tvPhone.visibility = View.GONE
        user?.address?.let { address ->
            binding.tvZone.text = address.displayAddress
        } ?: run {
            binding.tvZone.visibility = View.GONE
        }
    }

    private fun updateUserView() {
        user?.let { user ->
            with(binding) {
                tvName.text = user.displayName
                ivProfile.let { photoView ->
                    user.avatarURL?.let { avatarURL ->
                        Timber.e("avatarURL: $avatarURL")
                        Glide.with(photoView)
                            .load(avatarURL)
                            .placeholder(R.drawable.placeholder_user)
                            .error(R.drawable.placeholder_user)
                            .circleCrop()
                            .into(photoView)
                    } ?: run {
                        photoView.setImageResource(R.drawable.placeholder_user)
                    }
                }
                ivAssoBadge.let { photoView ->
                    user.partner?.smallLogoUrl?.let { imgUrl ->
                        Glide.with(photoView)
                            .load(imgUrl)
                            .placeholder(R.drawable.placeholder_user)
                            .error(R.drawable.placeholder_user)
                            .circleCrop()
                            .into(photoView)
                    } ?: run {
                        photoView.setImageResource(R.drawable.placeholder_user)
                    }
                }
            }
        }
    }

    private fun setSignalButton() {
        binding.iconOption.visibility = View.VISIBLE
        binding.iconOption.setOnClickListener {
            VibrationUtil.vibrate(this)
            val bottomSheet = UserOptionsBottomSheet()
            UserOptionsBottomSheet.user = user
            bottomSheet.show(supportFragmentManager, "UserOptionsBottomSheet")
        }
    }

    private fun setModifyButton() {
        binding.btnModifyPhotoProfile.visibility = View.GONE
    }

    private fun setButtonListeners() {
        binding.buttonModify.setOnClickListener {
            VibrationUtil.vibrate(this)
            discussionsPresenter.createOrGetConversation(id.toString())
        }
        binding.buttonModify.visibility = View.VISIBLE
        binding.buttonModify.text = getString(R.string.profil_full_send_message)
    }

    private fun showConfetti(view: View) {
        val parentView = view.rootView as ViewGroup
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width / 2
        val y = location[1] + view.height / 2

        for (i in 0..100) {
            val confetti = createConfettiView(view)
            confetti.translationX = x.toFloat() + Random.nextInt(-50, 50)
            confetti.translationY = y.toFloat() + Random.nextInt(-50, 50)
            parentView.addView(confetti)
            animateConfetti(confetti, parentView)
        }
    }

    private fun setPartnerClickListener() {
        binding.ivAssoBadge.setOnClickListener {
            VibrationUtil.vibrate(this)
            user?.partner?.id?.let { partnerId ->
                val intent = Intent(this, AssociationProfileActivity::class.java).apply {
                    putExtra(Const.PARTNER_ID, partnerId.toInt())
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun createConfettiView(view: View): View {
        val confetti = View(view.context)
        confetti.layoutParams = FrameLayout.LayoutParams(20, 20)
        confetti.setBackgroundColor(generateRandomColor())
        return confetti
    }

    private fun generateRandomColor(): Int {
        val colors = listOf(
            Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN
        )
        return colors.random()
    }

    private fun animateConfetti(confetti: View, parentView: ViewGroup) {
        val startY = confetti.translationY
        val startX = confetti.translationX
        val peakY = startY - Random.nextInt(200, 500)
        val peakX = startX + Random.nextInt(-200, 200)

        val explosionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400L
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                confetti.translationY = startY + (peakY - startY) * progress
                confetti.translationX = startX + (peakX - startX) * progress
            }
        }

        val endY = startY + Random.nextInt(300, 800)
        val endX = peakX + Random.nextInt(-200, 200)

        val fallAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Random.nextLong(1000, 2000)
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                confetti.translationY = peakY + (endY - peakY) * progress
                confetti.translationX = peakX + (endX - peakX) * progress
                confetti.alpha = 1 - progress
            }
        }

        explosionAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                fallAnimator.start()
            }
        })

        fallAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                parentView.removeView(confetti)
            }
        })

        explosionAnimator.start()
    }

    private fun updateProfile(hasToUpdate: Boolean) {
        if (hasToUpdate) {
            userPresenter.getUser(id)
        }
    }
}
