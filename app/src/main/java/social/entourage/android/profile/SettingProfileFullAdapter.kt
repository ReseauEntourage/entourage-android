// SettingProfileFullAdapter.kt
package social.entourage.android.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.SettingItemSeparatorBinding
import social.entourage.android.databinding.SettingsItemUserSectionBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.language.LanguageBottomFragment
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.profile.editProfile.EditPasswordFragment
import social.entourage.android.profile.settings.HelpAboutFragment
import social.entourage.android.profile.settings.SettingsNotificationsFragment
import social.entourage.android.profile.settings.UnblockUsersFragment

class SettingProfileFullAdapter(
    private val items: List<ProfileSectionItem>,
    private val context: Context,
    private val parentFragmentManager: androidx.fragment.app.FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SEPARATOR = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProfileSectionItem.Separator -> VIEW_TYPE_SEPARATOR
            is ProfileSectionItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEPARATOR -> {
                val binding = SettingItemSeparatorBinding.inflate(inflater, parent, false)
                SeparatorViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = SettingsItemUserSectionBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProfileSectionItem.Separator -> (holder as SeparatorViewHolder).bind(item)
            is ProfileSectionItem.Item -> (holder as ItemViewHolder).bind(item)
        }
    }

    // ViewHolder for Separator
    inner class SeparatorViewHolder(
        private val binding: SettingItemSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(separator: ProfileSectionItem.Separator) {
            binding.tvTitleSeparator.text = separator.title
        }
    }

    // ViewHolder for Item
    inner class ItemViewHolder(
        private val binding: SettingsItemUserSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfileSectionItem.Item) {
            binding.ivUserSection.setImageResource(item.iconRes)
            binding.tvTitleUserSection.text = item.title
            binding.tvSubtitleUserSection.text = item.subtitle
            binding.ivArrowUserSection.setImageResource(R.drawable.arrow_right_orange)

            //Deconnexion et suppression en orange
            if (item.title == context.getString(R.string.delete_account_button) || item.title == context.getString(R.string.logout_button)) {
                binding.tvTitleUserSection.setTextColor(ContextCompat.getColor(context, R.color.orange))
                binding.tvSubtitleUserSection.setTextColor(ContextCompat.getColor(context, R.color.orange))
                binding.ivArrowUserSection.setColorFilter(ContextCompat.getColor(context, R.color.orange))
            }

            // Handle Clicks
            binding.root.setOnClickListener {
                when (item.title) {
                    context.getString(R.string.settings_language_title) -> {
                        LanguageBottomFragment.newInstance().show(parentFragmentManager, LanguageBottomFragment.TAG)
                    }
                    context.getString(R.string.settings_notifications_title) -> {
                        SettingsNotificationsFragment.newInstance()
                            .show(parentFragmentManager, SettingsNotificationsFragment.TAG)
                    }
                    context.getString(R.string.settings_feedback_title) -> {
                        WebViewFragment.newInstance(context.getString(R.string.url_app_suggest), 0, true)
                            .show(parentFragmentManager, WebViewFragment.TAG)
                    }
                    context.getString(R.string.settings_password_title) -> {
                        EditPasswordFragment().show(parentFragmentManager, EditPasswordFragment.TAG)
                    }
                    context.getString(R.string.settings_unblock_contacts_title) -> {
                        UnblockUsersFragment.newInstance()
                            .show(parentFragmentManager, UnblockUsersFragment.TAG)
                    }

                    context.getString(R.string.settings_help_title) -> {
                        HelpAboutFragment.newInstance().show(parentFragmentManager, HelpAboutFragment.TAG)
                    }
                    context.getString(R.string.delete_account_button) -> {
                        CustomAlertDialog.showWithCancelFirst(
                            context,
                            context.getString(R.string.delete_account_dialog_title),
                            context.getString(R.string.delete_account_dialog_content),
                            context.getString(R.string.delete)
                        ) {
                            EntourageApplication.get().logOut()
                            (context as Activity).finish()
                        }
                    }
                    context.getString(R.string.logout_button) -> {
                        CustomAlertDialog.showWithCancelFirst(
                            context,
                            context.getString(R.string.sign_out_dialog_title),
                            context.getString(R.string.sign_out_dialog_content),
                            context.getString(R.string.signing_out)
                        ) {
                            EntourageApplication.get().logOut()
                            (context as Activity).finish()
                        }
                    }
                    context.getString(R.string.preferences_interest_title) -> {
                        EnhancedOnboarding.isFromSettingsinterest = true
                        val intent = Intent(context, EnhancedOnboarding::class.java)
                        context.startActivity(intent)
                    }
                    context.getString(R.string.preferences_availability_title) -> {
                        EnhancedOnboarding.isFromSettingsDisponibility = true
                        val intent = Intent(context, EnhancedOnboarding::class.java)
                        context.startActivity(intent)

                    }
                    context.getString(R.string.preferences_action_title) -> {
                        EnhancedOnboarding.isFromSettingsWishes = true
                        val intent = Intent(context, EnhancedOnboarding::class.java)
                        context.startActivity(intent)

                    }

                    else -> {
                        Toast.makeText(context, "Unknown action", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
