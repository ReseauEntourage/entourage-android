package social.entourage.android.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.ActivityProfileSettingsBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding

    companion object {
        private const val EXTRA_NOTIF_SUBTITLE = "extra_notif_subtitle"
        private const val EXTRA_NOTIF_BLOCKED = "extra_notif_blocked"

        fun start(context: Context, notifSubTitle: String, notifBlocked: String) {
            val intent = Intent(context, ProfileSettingsActivity::class.java).apply {
                putExtra(EXTRA_NOTIF_SUBTITLE, notifSubTitle)
                putExtra(EXTRA_NOTIF_BLOCKED, notifBlocked)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updatePaddingTopForEdgeToEdge(binding.layoutSettings)

        val notifSubTitle = intent.getStringExtra(EXTRA_NOTIF_SUBTITLE) ?: ""
        val notifBlocked = intent.getStringExtra(EXTRA_NOTIF_BLOCKED) ?: ""

        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView(notifSubTitle, notifBlocked)
    }

    private fun setupRecyclerView(notifSubTitle: String, notifBlocked: String) {
        val items = mutableListOf<ProfileSectionItem>()

        val currentLanguageCode = LanguageManager.loadLanguageFromPreferences(this)
        val currentLanguageName = LanguageManager.languageMap.entries.firstOrNull {
            it.value == currentLanguageCode
        }?.key ?: getString(R.string.unknown_language)

        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_language,
            title = getString(R.string.settings_language_title),
            subtitle = currentLanguageName
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_notifications,
            title = getString(R.string.settings_notifications_title),
            subtitle = notifSubTitle
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_help,
            title = getString(R.string.settings_help_title),
            subtitle = getString(R.string.settings_help_subtitle)
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_unblock_contacts,
            title = getString(R.string.settings_unblock_contacts_title),
            subtitle = notifBlocked
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_feedback,
            title = getString(R.string.settings_feedback_title),
            subtitle = ""
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_share,
            title = getString(R.string.settings_share_title),
            subtitle = ""
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_change_password,
            title = getString(R.string.settings_password_title),
            subtitle = ""
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_logout,
            title = getString(R.string.logout_button),
            subtitle = ""
        ))
        items.add(ProfileSectionItem.Item(
            iconRes = R.drawable.ic_profile_delete_account,
            title = getString(R.string.delete_account_button),
            subtitle = ""
        ))

        val adapter = SettingProfileFullAdapter(items, this, supportFragmentManager, isMe = true)
        binding.rvSettings.layoutManager = LinearLayoutManager(this)
        binding.rvSettings.adapter = adapter
    }
}
