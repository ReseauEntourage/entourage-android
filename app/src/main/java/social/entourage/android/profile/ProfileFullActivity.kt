package social.entourage.android.profile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.ActionUtils
import social.entourage.android.api.model.EventUtils
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserBlockedUser
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutProfileBinding
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.home.HomePresenter
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.editProfile.EditPhotoActivity
import social.entourage.android.profile.editProfile.EditProfileFragment
import social.entourage.android.profile.settings.ProfilFullViewModel
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.user.UserPresenter
import timber.log.Timber
import java.text.SimpleDateFormat
import kotlin.random.Random



class ProfileFullActivity : BaseActivity()  {

    private lateinit var binding: ActivityLayoutProfileBinding
    private lateinit var user: User
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    private lateinit var profilFullViewModel:ProfilFullViewModel
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    var notifSubTitle = ""
    var notifBlocked = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutProfileBinding.inflate(layoutInflater)
        binding.containerProfile.visibility = View.GONE
        profilFullViewModel = ViewModelProvider(this).get(ProfilFullViewModel::class.java)
        user = EntourageApplication.me(this) ?: return
        userPresenter.user.observe(this, ::updateUser)
        homePresenter.notificationsPermission.observe(this, ::updateNotifParam)
        discussionsPresenter.getBlockedUsers.observe(this,::handleResponseBlocked)
        profilFullViewModel.hasToUpdate.observe(this, :: updateProfile)
        discussionsPresenter.getBlockedUsers()
        binding.progressBar.visibility = View.VISIBLE
        initializeStats()
        updateUserView()
        setButtonListeners()
        setModifyButton()
        setSignalButton()
        setScrollEffects()
        setBackButton()
        setConfettiView()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        if(isMe){
            userPresenter.getUser(user.id)
        }else{
            userPresenter.getUser(userId)
        }
        discussionsPresenter.getBlockedUsers()
        EnhancedOnboarding.isFromSettingsWishes = false
        EnhancedOnboarding.isFromSettingsDisponibility = false
        EnhancedOnboarding.isFromSettingsinterest = false
        EnhancedOnboarding.isFromSettingsActionCategorie = false

    }
    private fun handleResponseBlocked(blockedUsers:MutableList<UserBlockedUser>?) {
        if(blockedUsers.isNullOrEmpty()){
            notifBlocked = getString(R.string.settings_unblock_contacts_subtitle)
        }else{
            notifBlocked = getString(R.string.settings_number_blocked_contacts_subtitle) + blockedUsers.size
        }
        homePresenter.getNotificationsPermissions()
    }

    private fun updateNotifParam(notifsPermissions: InAppNotificationPermission?) {
        notifsPermissions?.let {
            notifSubTitle = ""
            if(it.action){
                notifSubTitle += getString(R.string.notifications_actions) + ", "
            }
            if(it.outing){
                notifSubTitle += getString(R.string.notifications_events) + ", "
            }
            if(it.neighborhood){
                notifSubTitle += getString(R.string.notifications_groups) + ", "
            }
            if(it.chat_message){
                notifSubTitle += getString(R.string.notifications_messages) + ", "
            }
        }
        if (notifSubTitle.isNotEmpty()) {
            notifSubTitle = notifSubTitle.substring(0, notifSubTitle.length - 2)
            notifSubTitle = getString(R.string.settings_notifications_subtitle) + notifSubTitle
        }else{
            notifSubTitle = getString(R.string.no_notifications_active)
        }
        setupRecyclerView()
        updateUserView()
        initializeStats()
        binding.containerProfile.visibility = View.VISIBLE
    }

    private fun setConfettiView() {
        binding.layoutAchievement.setOnClickListener { view ->
            //VibrationUtil.vibrate(this)
            //showConfetti(view)
        }
    }

    private fun updateUser(user:User){
        notifSubTitle = ""
        notifBlocked = ""
        this.user = user
        discussionsPresenter.getBlockedUsers()

    }

    private fun setButtonListeners() {
        binding.buttonModify.setOnClickListener {
            VibrationUtil.vibrate(this)
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
        if(isMe){
            binding.buttonModify.visibility = View.VISIBLE
        }else{
            binding.buttonModify.visibility = View.GONE
        }
    }

    private fun setModifyButton() {
        binding.btnModifyPhotoProfile.setOnClickListener {
            VibrationUtil.vibrate(this)
            val intent = Intent(this, EditPhotoActivity::class.java)
            startActivity(intent)

            
        }
        if(isMe){
            binding.btnModifyPhotoProfile.visibility = View.VISIBLE
        }else{
            binding.btnModifyPhotoProfile.visibility = View.GONE
        }
    }

    private fun setSignalButton(){
        if(isMe){
            binding.iconOption.visibility = View.GONE
        }else{
            binding.iconOption.visibility = View.VISIBLE
            binding.iconOption.setOnClickListener {
                VibrationUtil.vibrate(this)
                val bottomSheet = UserOptionsBottomSheet()
                UserOptionsBottomSheet.user = user
                bottomSheet.show(supportFragmentManager, "UserOptionsBottomSheet")
            }
        }

    }

    private fun setScrollEffects() {
        binding.profileNestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val minScale = 0.3f // Reduced minimum scale
            val scale = (1f - scrollY / 500f).coerceIn(minScale, 1f)
            binding.ivProfile.scaleX = scale
            binding.ivProfile.scaleY = scale

            // Hide ivProfile and btnModifyPhotoProfile when scaled down to minimum
            if (scale == minScale) {
                binding.ivProfile.visibility = View.GONE
                binding.btnModifyPhotoProfile.visibility = View.GONE
            } else {
                binding.ivProfile.visibility = View.VISIBLE
                binding.btnModifyPhotoProfile.visibility = View.VISIBLE
            }
        }
    }
    private fun setBackButton(){

        binding.iconBack.setOnClickListener{
            VibrationUtil.vibrate(this)
            this.finish()
        }
    }

    private fun setupRecyclerView() {
        val items = mutableListOf<ProfileSectionItem>()

        // -- 1) Titre de la section préférences : "Mes préférences" ou "Ses préférences"
        val preferencesTitleRes = if (isMe) {
            R.string.preferences_section_title
        } else {
            R.string.preferences_section_title_others
        }
        items.add(ProfileSectionItem.Separator(getString(preferencesTitleRes)))

        // -- 2) Intérêts : "Mes centres d'intérêt" ou "Ses centres d'intérêt"
        val interestsTitleRes = if (isMe) {
            R.string.preferences_interest_title
        } else {
            R.string.preferences_interest_title_others
        }
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
                title = getString(interestsTitleRes),
                subtitle = interestsText
            )
        )

        // -- 3) Envies d'agir : "Mes envies d'agir" ou "Ses envies d'agir"
        val actionTitleRes = if (isMe) {
            R.string.preferences_action_title
        } else {
            R.string.preferences_action_title_others
        }
        val involvementsText = if (user.involvements.isNotEmpty()) {
            user.involvements.joinToString(", ") { involvement ->
                when (involvement.lowercase()) {
                    "outings"       -> getString(R.string.onboarding_action_wish_event)
                    "both_actions"  -> getString(R.string.onboarding_action_wish_services)
                    "neighborhoods" -> getString(R.string.onboarding_action_wish_network)
                    "resources"     -> getString(R.string.onboarding_action_wish_pedago)
                    else            -> getString(R.string.interest_other)
                }
            }
        } else {
            getString(R.string.no_data_available)
        }
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_action,
                title = getString(actionTitleRes),
                subtitle = involvementsText
            )
        )

        // -- 4) Catégories d'entraide : "Mes catégories d'entraide" ou "Ses catégories d'entraide"
        val categoriesTitleRes = if (isMe) {
            R.string.preferences_action_categories_title
        } else {
            R.string.preferences_action_categories_title_others
        }
        val categoriesMap = mapOf(
            "sharing_time"       to getString(R.string.onboarding_category_sharing_time),
            "material_donations" to getString(R.string.onboarding_category_donation),
            "services"           to getString(R.string.onboarding_category_services)
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
                title = getString(categoriesTitleRes),
                subtitle = categoriesText
            )
        )

        // -- 5) Disponibilités : "Mes disponibilités" ou "Ses disponibilités"
        val availabilityTitleRes = if (isMe) {
            R.string.preferences_availability_title
        } else {
            R.string.preferences_availability_title_others
        }
        // Disponibilité
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
                title = getString(availabilityTitleRes),
                subtitle = availabilityText
            )
        )

        // -- 6) Section Paramètres (seulement si c'est mon profil)
        if (isMe) {
            items.add(ProfileSectionItem.Separator(getString(R.string.settings_section_title)))

            // Langue
            val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(this)
            val currentLanguageName = LanguageManager.languageMap.entries.firstOrNull {
                it.value == currentLanguageCode
            }?.key ?: getString(R.string.unknown_language)

            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_language,
                    title = getString(R.string.settings_language_title),
                    subtitle = currentLanguageName
                )
            )

            // Vérifier si la traduction automatique est activée
            val sharedPrefs = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
            val translationSubtitle = if (isTranslatedByDefault) {
                getString(R.string.translation_auto_enabled)
            } else {
                getString(R.string.translation_auto_disabled)
            }
            // (Si tu l’utilises quelque part, tu peux l’ajouter à la liste...)

            // Notifications
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_notifications,
                    title = getString(R.string.settings_notifications_title),
                    subtitle = notifSubTitle
                )
            )

            // Aide
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_help,
                    title = getString(R.string.settings_help_title),
                    subtitle = getString(R.string.settings_help_subtitle)
                )
            )

            // Déblocage
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_unblock_contacts,
                    title = getString(R.string.settings_unblock_contacts_title),
                    subtitle = notifBlocked
                )
            )

            // Feedback
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_feedback,
                    title = getString(R.string.settings_feedback_title),
                    subtitle = ""
                )
            )

            // Partage
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_share,
                    title = getString(R.string.settings_share_title),
                    subtitle = ""
                )
            )

            // Changer mot de passe
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_change_password,
                    title = getString(R.string.settings_password_title),
                    subtitle = ""
                )
            )

            // Déconnexion
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_logout,
                    title = getString(R.string.logout_button),
                    subtitle = ""
                )
            )

            // Suppression de compte
            items.add(
                ProfileSectionItem.Item(
                    iconRes = R.drawable.ic_profile_delete_account,
                    title = getString(R.string.delete_account_button),
                    subtitle = ""
                )
            )
        }

        // -- 7) Initialisation de l'adapter
        val adapter = SettingProfileFullAdapter(items, this, this.supportFragmentManager)
        binding.rvSectionProfile.layoutManager = LinearLayoutManager(this)
        binding.rvSectionProfile.adapter = adapter
    }




    private fun initializeStats() {
        if(user == null){

            return
        }

        if(isMe){
            binding.myActivityTv.text = getString(R.string.my_activity)
        }else{
            binding.myActivityTv.text = getString(R.string.his_activity)
        }
        user?.stats?.let { stats ->
            // Contributions
            if (stats.neighborhoodsCount > 0) {
                binding.contribContent.text = stats.neighborhoodsCount.toString()
                binding.titleContrib.text = getString(R.string.contributions_group)
                binding.contribContent.visibility = View.VISIBLE
                binding.titleContrib.visibility = View.VISIBLE
            } else {

            }
            // Événements
            if (stats.outingsCount > 0) {
                binding.eventContent.text = stats.outingsCount.toString()
                binding.titleEvent.text = getString(R.string.contributions_event)
                binding.eventContent.visibility = View.VISIBLE
                binding.titleEvent.visibility = View.VISIBLE
            } else {

            }
            // Icônes (toujours visibles dans cet exemple)
            binding.iconContrib.setImageResource(R.drawable.icon_navbar_groupe_inactif)
            binding.iconEvent.setImageResource(R.drawable.icon_navbar_calendrier_inactif)
        }

        // Rôles
        user?.roles?.let { roles ->
            Timber.wtf("wtf roles $roles")
            binding.tagUser.visibility = if (roles.contains("Ambassadeur") || roles.contains("Équipe Entourage") || roles.contains("Association")) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.ivAssoBadge.visibility = if (roles.contains("Équipe Entourage") || roles.contains("Association")) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if(roles.contains("Ambassadeur")){
                binding.tvTagHomeV2EventItem.text = getString(R.string.ambassador)
                binding.ivAssoBadge.visibility = View.GONE
            }else if(roles.contains("Équipe Entourage")){
                binding.tvTagHomeV2EventItem.text = user?.partner?.name
                binding.ivAssoBadge.visibility = View.VISIBLE
            }else if(roles.contains("Association")){
                binding.tvTagHomeV2EventItem.text = user.partner?.name
                binding.ivAssoBadge.visibility = View.VISIBLE
            }
        }
        // Date d'inscription
        user?.createdAt?.let { createdAt ->
            val locale = LanguageManager.getLocaleFromPreferences(this)
            binding.joined.date.text = SimpleDateFormat(
                this.getString(R.string.profile_date_format),
                locale
            ).format(createdAt)
            binding.joined.date.visibility = View.VISIBLE
        } ?: run {
            binding.joined.date.visibility = View.GONE
        }

        // Email
        if(isMe){
            user?.email?.let { email ->
                if (email.isNotBlank()) {
                    binding.tvMail.text = email
                    binding.tvMail.visibility = View.VISIBLE
                } else {
                    binding.tvMail.visibility = View.GONE
                }
            } ?: run {
                binding.tvMail.visibility = View.GONE
            }


            user?.phone.let { phone ->
                if (phone?.isNotBlank()!!) {
                    binding.tvPhone.text = phone
                    binding.tvPhone.visibility = View.VISIBLE
                } else {
                    binding.tvPhone.visibility = View.GONE
                }
            } ?: run {
                binding.tvPhone.visibility = View.GONE
            }

            // Adresse et distance
            user?.address?.let { address ->
                if (address.displayAddress.isNotBlank() && user.travelDistance != null) {
                    binding.tvZone.text = "${address.displayAddress} - Rayon de ${user.travelDistance} km"
                    binding.tvZone.visibility = View.VISIBLE
                } else {
                    binding.tvZone.visibility = View.GONE
                }
            } ?: run {
                binding.tvZone.visibility = View.GONE
            }
        }else{
            binding.tvMail.visibility = View.GONE
            binding.tvPhone.visibility = View.GONE
            user?.address?.let { address ->
                binding.tvZone.text = address.displayAddress
            }
            if(user.address == null) {
                binding.tvZone.visibility = View.GONE
            }
        }


        // À propos
        user?.about?.let { about ->
            if (about.isNotBlank()) {
                binding.tvDescription.text = about
                binding.tvDescription.setTextColor(ContextCompat.getColor(this, R.color.black)) // Couleur normale
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.text = this.getString(R.string.placeholder_description_profile)
                binding.tvDescription.setTextColor(ContextCompat.getColor(this, R.color.grey)) // Placeholder en gris
                binding.tvDescription.visibility = View.VISIBLE
            }
        } ?: run {
            binding.tvDescription.text = this.getString(R.string.placeholder_description_profile)
            binding.tvDescription.setTextColor(ContextCompat.getColor(this, R.color.grey)) // Placeholder en gris
            binding.tvDescription.visibility = View.VISIBLE
        }
        binding.appVersion.text =
            getString(R.string.about_version_format, BuildConfig.VERSION_FULL_NAME)
        if (!BuildConfig.DEBUG) {
            binding.appDebugInfo.visibility = View.INVISIBLE
        } else {
            binding.appDebugInfo.visibility = View.VISIBLE
            binding.appDebugInfo.text = getString(
                R.string.about_debug_info_format, BuildConfig.VERSION_DISPLAY_BRANCH_NAME,
                EntourageApplication.get().sharedPreferences.getString(
                    EntourageApplication.KEY_REGISTRATION_ID,
                    null
                )
            )
        }
        binding.progressBar.visibility = View.GONE

    }


    private fun updateUserView() {
        with(binding) {
            tvName.text = user.displayName
            ivProfile.let { photoView ->
                user.avatarURL?.let { avatarURL ->
                    Glide.with(binding.ivProfile)
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
                val imgUrl = user.partner?.smallLogoUrl
                Glide.with(binding.ivProfile)
                    .load(imgUrl)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(photoView)
            } ?: run {
                binding.ivProfile.setImageResource(R.drawable.placeholder_user)
            }
        }
    }

    private fun showConfetti(view: View) {
        // Récupérer le parent pour ajouter les confettis
        val parentView = view.rootView as ViewGroup

        // Définir la position initiale des confettis (autour du clic)
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0] + view.width / 2
        val y = location[1] + view.height / 2

        // Générer plusieurs petites vues
        for (i in 0..100) {
            val confetti = createConfettiView(view)
            confetti.translationX = x.toFloat() + Random.nextInt(-50, 50)
            confetti.translationY = y.toFloat() + Random.nextInt(-50, 50)
            parentView.addView(confetti)

            // Animer chaque confetti
            animateConfetti(confetti, parentView)
        }
    }

    private fun createConfettiView(view: View): View {
        // Créer une petite vue colorée
        val confetti = View(view.context)
        confetti.layoutParams = FrameLayout.LayoutParams(20, 20)
        confetti.setBackgroundColor(generateRandomColor())
        return confetti
    }

    private fun generateRandomColor(): Int {
        // Générer une couleur aléatoire
        val colors = listOf(
            Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.CYAN
        )
        return colors.random()
    }

    private fun animateConfetti(confetti: View, parentView: ViewGroup) {
        // Initial position
        val startY = confetti.translationY
        val startX = confetti.translationX

        // Generate random explosion height
        val peakY = startY - Random.nextInt(200, 500) // Upward burst
        val peakX = startX + Random.nextInt(-200, 200) // Random horizontal spread

        // Phase 1: Explosion upwards
        val explosionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 400L // Explosion duration
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                confetti.translationY = startY + (peakY - startY) * progress
                confetti.translationX = startX + (peakX - startX) * progress
            }
        }

        // Phase 2: Free fall
        val endY = startY + Random.nextInt(300, 800) // Random fall
        val endX = peakX + Random.nextInt(-200, 200) // Final spread

        val fallAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Random.nextLong(1000, 2000) // Fall duration
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                confetti.translationY = peakY + (endY - peakY) * progress
                confetti.translationX = peakX + (endX - peakX) * progress
                confetti.alpha = 1 - progress // Fade out confetti
            }
        }

        // Smooth transition: Explosion to fall
        explosionAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                fallAnimator.start() // Start fall after explosion
            }
        })

        // Remove confetti after fall
        fallAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                parentView.removeView(confetti) // Remove view
            }
        })

        // Start explosion
        explosionAnimator.start()
    }

    fun updateProfile(hasToUpdate:Boolean){
        if(hasToUpdate){
            userPresenter.getUser(user.id)
        }
    }

    companion object {
        var isMe:Boolean = false
        var userId:Int = 0
    }

}
