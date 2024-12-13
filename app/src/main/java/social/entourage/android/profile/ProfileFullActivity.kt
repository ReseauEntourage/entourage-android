package social.entourage.android.profile

import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutProfileBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.editProfile.EditPhotoActivity
import social.entourage.android.profile.editProfile.EditProfileFragment
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.user.UserPresenter
import timber.log.Timber
import java.text.SimpleDateFormat

class ProfileFullActivity : BaseActivity() {

    private lateinit var binding: ActivityLayoutProfileBinding
    private lateinit var user: User
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutProfileBinding.inflate(layoutInflater)
        user = EntourageApplication.me(this) ?: return
        setupRecyclerView()
        initializeStats()
        updateUserView()
        setButtonListeners()
        setModifyButton()
        setScrollEffects()
        setBackButton()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        userPresenter.user.observe(this, ::updateUser)
        updateUserView()

    }

    private fun updateUser(user:User){
        this.user = user
        updateUserView()
    }

    private fun setButtonListeners() {
        binding.buttonModify.setOnClickListener {
            VibrationUtil.vibrate(this)
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setModifyButton() {
        binding.btnModifyPhotoProfile.setOnClickListener {
            VibrationUtil.vibrate(this)
            val intent = Intent(this, EditPhotoActivity::class.java)
            startActivity(intent)
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

        // Section: Mes préférences
        items.add(ProfileSectionItem.Separator(getString(R.string.preferences_section_title)))

        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_interests,
                title = getString(R.string.preferences_interest_title),
                subtitle = getString(R.string.preferences_interest_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_action,
                title = getString(R.string.preferences_action_title),
                subtitle = getString(R.string.preferences_action_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_availability,
                title = getString(R.string.preferences_availability_title),
                subtitle = getString(R.string.preferences_availability_subtitle)
            )
        )

        // Section: Paramètres
        items.add(ProfileSectionItem.Separator(getString(R.string.settings_section_title)))

        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_language,
                title = getString(R.string.settings_language_title),
                subtitle = getString(R.string.settings_language_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_notifications,
                title = getString(R.string.settings_notifications_title),
                subtitle = getString(R.string.settings_notifications_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_help,
                title = getString(R.string.settings_help_title),
                subtitle = getString(R.string.settings_help_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_unblock_contacts,
                title = getString(R.string.settings_unblock_contacts_title),
                subtitle = getString(R.string.settings_unblock_contacts_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_feedback,
                title = getString(R.string.settings_feedback_title),
                subtitle = ""
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_share,
                title = getString(R.string.settings_share_title),
                subtitle = ""
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_change_password,
                title = getString(R.string.settings_password_title),
                subtitle = ""
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_logout,
                title = getString(R.string.logout_button),
                subtitle = ""
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_delete_account,
                title = getString(R.string.delete_account_button),
                subtitle = ""
            )
        )

        // Initialize Adapter
        val adapter = SettingProfileFullAdapter(items, this, this.supportFragmentManager)
        binding.rvSectionProfile.layoutManager = LinearLayoutManager(this)
        binding.rvSectionProfile.adapter = adapter
    }

    private fun initializeStats() {
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
            binding.tvTagHomeV2EventItem.visibility = if (roles.contains("ambassador")) {
                View.VISIBLE
            } else {
                View.GONE
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

        // Adresse et distance
        user?.address?.let { address ->
            if (address.displayAddress.isNotBlank() && user.travelDistance != null) {
                binding.tvZone.text = "${address.displayAddress} - ${user.travelDistance} km"
                binding.tvZone.visibility = View.VISIBLE
            } else {
                binding.tvZone.visibility = View.GONE
            }
        } ?: run {
            binding.tvZone.visibility = View.GONE
        }

        // À propos
        user?.about?.let { about ->
            if (about.isNotBlank()) {
                binding.tvDescription.text = about
                binding.tvDescription.visibility = View.VISIBLE
            } else {
                binding.tvDescription.visibility = View.GONE
            }
        } ?: run {
            binding.tvDescription.visibility = View.GONE
        }
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
        }
    }
}
