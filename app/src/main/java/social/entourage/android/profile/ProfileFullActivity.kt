// ProfileFullActivity.kt
package social.entourage.android.profile

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutProfileBinding
import social.entourage.android.language.LanguageManager
import java.text.SimpleDateFormat

class ProfileFullActivity : BaseActivity() {

    private lateinit var binding: ActivityLayoutProfileBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutProfileBinding.inflate(layoutInflater)
        user = EntourageApplication.me(this) ?: return
        setupRecyclerView()
        initializeStats()
        setContentView(binding.root)
        updateUserView()

    }

    private fun setupRecyclerView() {
        val items = mutableListOf<ProfileSectionItem>()

        // Section: Mes préférences
        items.add(ProfileSectionItem.Separator(getString(R.string.preferences_section_title)))

        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_interests, // Replace with actual icon resource
                title = getString(R.string.preferences_interest_title),
                subtitle = getString(R.string.preferences_interest_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_action, // Replace with actual icon resource
                title = getString(R.string.preferences_action_title),
                subtitle = getString(R.string.preferences_action_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_availability, // Replace with actual icon resource
                title = getString(R.string.preferences_availability_title),
                subtitle = getString(R.string.preferences_availability_subtitle)
            )
        )

        // Section: Paramètres
        items.add(ProfileSectionItem.Separator(getString(R.string.settings_section_title)))

        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_language, // Replace with actual icon resource
                title = getString(R.string.settings_language_title),
                subtitle = getString(R.string.settings_language_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_notifications, // Replace with actual icon resource
                title = getString(R.string.settings_notifications_title),
                subtitle = getString(R.string.settings_notifications_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_help, // Replace with actual icon resource
                title = getString(R.string.settings_help_title),
                subtitle = getString(R.string.settings_help_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_unblock_contacts, // Replace with actual icon resource
                title = getString(R.string.settings_unblock_contacts_title),
                subtitle = getString(R.string.settings_unblock_contacts_subtitle)
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_feedback, // Replace with actual icon resource
                title = getString(R.string.settings_feedback_title),
                subtitle = "" // Assuming no subtitle
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_share, // Replace with actual icon resource
                title = getString(R.string.settings_share_title),
                subtitle = "" // Assuming no subtitle
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_change_password, // Replace with actual icon resource
                title = getString(R.string.settings_password_title),
                subtitle = "" // Assuming no subtitle
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_logout, // Replace with actual icon resource
                title = getString(R.string.logout_button),
                subtitle = "" // Assuming no subtitle
            )
        )
        items.add(
            ProfileSectionItem.Item(
                iconRes = R.drawable.ic_profile_delete_account, // Replace with actual icon resource
                title = getString(R.string.delete_account_button),
                subtitle = "" // Assuming no subtitle
            )
        )

        // Initialize Adapter
        val adapter = SettingProfileFullAdapter(items)
        binding.rvSectionProfile.layoutManager = LinearLayoutManager(this)
        binding.rvSectionProfile.adapter = adapter
    }

    private fun initializeStats() {
        // Exemple pour les statistiques (événements, contributions, etc.)
        user?.stats?.let { stats ->
            // Contributions
            binding.contribContent.text = stats.neighborhoodsCount.toString()
            binding.titleContrib.text = getString(R.string.contributions_group)

            // Événements
            binding.eventContent.text = stats.outingsCount.toString()
            binding.titleEvent.text = getString(R.string.contributions_event)

            // Icônes (si nécessaires)
            binding.iconContrib.setImageResource(R.drawable.icon_navbar_groupe_inactif)
            binding.iconEvent.setImageResource(R.drawable.icon_navbar_calendrier_inactif)
        }

        // Afficher ou masquer le badge ambassadeur
        user?.roles?.let { roles ->
            if (roles.contains("ambassador")) {
                binding.tvTagHomeV2EventItem.visibility = View.VISIBLE
            } else {
                binding.tvTagHomeV2EventItem.visibility = View.GONE
            }
        }
        user.createdAt?.let {createdAt ->
            val locale = LanguageManager.getLocaleFromPreferences(this)
            binding.joined.date.text = SimpleDateFormat(
                this.getString(R.string.profile_date_format),
                locale
            ).format(
                createdAt
            )
        }
        user.email?.let { email ->
            binding.tvMail.text = email
        }
        user.address?.let { address ->
            binding.tvZone.text = address.googlePlaceId + " " + user.travelDistance + " km"
        }
        user.about?.let { about ->
            binding.tvDescription.text = about
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
