package social.entourage.android.profile.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.api.model.notification.InAppNotificationPermission
import social.entourage.android.databinding.NewFragmentSettingsNotifsBinding
import social.entourage.android.home.HomePresenter

class SettingsNotificationsActivity : AppCompatActivity() {

    private lateinit var binding: NewFragmentSettingsNotifsBinding
    private lateinit var profilFullViewModel: ProfilFullViewModel

    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    var notificationsPermission = MutableLiveData<InAppNotificationPermission?>()
    private var areNotificationsEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentSettingsNotifsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profilFullViewModel = ViewModelProvider(this).get(ProfilFullViewModel::class.java)
        areNotificationsEnabled = areNotificationsEnabled(this)
        populate()
        handleCloseButton()

        homePresenter.notificationsPermission.observe(this, ::updateSwitch)
        homePresenter.getNotificationsPermissions()
    }

    private fun updateSwitch(notifsPermissions: InAppNotificationPermission?) {
        notifsPermissions?.let {
            if (!areNotificationsEnabled) {
                resetAllSwitches()                      // pas de notif autorisée par Android
            } else {
                binding.uiSwitchNotifsActions.isChecked  = it.action
                binding.uiSwitchNotifsEvents.isChecked   = it.outing
                binding.uiSwitchNotifsGroups.isChecked   = it.neighborhood
                binding.uiSwitchNotifsMessages.isChecked = it.chat_message
                binding.uiSwitchNotifsAll.isChecked      = it.isAllChecked()
            }
        }
    }


    private fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun redirectToNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, this@SettingsNotificationsActivity.packageName)
        }
        startActivity(intent)
        finish() // A la place de dismiss()
    }

    private fun resetAllSwitches() {
        binding.uiSwitchNotifsActions.isChecked = false
        binding.uiSwitchNotifsEvents.isChecked = false
        binding.uiSwitchNotifsGroups.isChecked = false
        binding.uiSwitchNotifsMessages.isChecked = false
        binding.uiSwitchNotifsAll.isChecked = false
    }

    private fun populate() {
        binding.uiSwitchNotifsAll.setOnClickListener {
            val isOn = binding.uiSwitchNotifsAll.isChecked
            if (isOn && !areNotificationsEnabled) {
                redirectToNotificationSettings()
            } else {
                binding.uiSwitchNotifsActions.isChecked = isOn
                binding.uiSwitchNotifsEvents.isChecked = isOn
                binding.uiSwitchNotifsGroups.isChecked = isOn
                binding.uiSwitchNotifsMessages.isChecked = isOn
            }
        }

        binding.uiSwitchNotifsActions.setOnCheckedChangeListener { _, _ ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsEvents.setOnCheckedChangeListener { _, _ ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsGroups.setOnCheckedChangeListener { _, _ ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsMessages.setOnCheckedChangeListener { _, _ ->
            checkSwitchs()
        }

        binding.validate.button.setOnClickListener {
            homePresenter.notificationsPermission.value?.action = binding.uiSwitchNotifsActions.isChecked
            homePresenter.notificationsPermission.value?.neighborhood = binding.uiSwitchNotifsGroups.isChecked
            homePresenter.notificationsPermission.value?.outing = binding.uiSwitchNotifsEvents.isChecked
            homePresenter.notificationsPermission.value?.chat_message = binding.uiSwitchNotifsMessages.isChecked
            homePresenter.updateNotificationsPermissions()
            profilFullViewModel.updateProfile()
            finish() // A la place de dismiss()
        }
    }

    private fun checkSwitchs() {
        val isAllOn =
            binding.uiSwitchNotifsActions.isChecked  &&
                    binding.uiSwitchNotifsEvents.isChecked   &&
                    binding.uiSwitchNotifsGroups.isChecked   &&
                    binding.uiSwitchNotifsMessages.isChecked

        binding.uiSwitchNotifsAll.isChecked = isAllOn
    }

    private fun handleCloseButton() {
        binding.header.hbsIconCross.setOnClickListener {
            finish() // A la place de dismiss()
        }
    }

    companion object {
        const val TAG = "SettingsNotifsFragment"

        fun start(context: Context) {
            val intent = Intent(context, SettingsNotificationsActivity::class.java)
            context.startActivity(intent)
        }
    }
}
