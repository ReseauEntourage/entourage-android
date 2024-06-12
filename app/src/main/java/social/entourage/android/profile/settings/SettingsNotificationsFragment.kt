package social.entourage.android.profile.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.databinding.NewFragmentSettingsNotifsBinding
import social.entourage.android.home.HomePresenter
import social.entourage.android.api.model.notification.InAppNotificationPermission

class SettingsNotificationsFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsNotifsBinding? = null
    val binding: NewFragmentSettingsNotifsBinding get() = _binding!!

    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    var notificationsPermission = MutableLiveData<InAppNotificationPermission?>()
    private var areNotificationsEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsNotifsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        areNotificationsEnabled = areNotificationsEnabled(requireContext())
        populate()
        handleCloseButton()
        homePresenter.notificationsPermission.observe(requireActivity(), ::updateSwitch)
        homePresenter.getNotificationsPermissions()
    }

    private fun updateSwitch(notifsPermissions: InAppNotificationPermission?) {
        notifsPermissions?.let {
            if (!it.isAllChecked() || !areNotificationsEnabled) {
                // Si les notifications du serveur ou du téléphone sont désactivées, désactivez tous les switches
                resetAllSwitches()
            } else {
                // Sinon, mettez à jour les switches en fonction des permissions du serveur
                binding.uiSwitchNotifsActions.isChecked = it.action
                binding.uiSwitchNotifsEvents.isChecked = it.outing
                binding.uiSwitchNotifsGroups.isChecked = it.neighborhood
                binding.uiSwitchNotifsMessages.isChecked = it.chat_message
                binding.uiSwitchNotifsAll.isChecked = it.isAllChecked()
            }
        }
    }

    private fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun redirectToNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
        }
        startActivity(intent)
        dismiss()
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

        binding.uiSwitchNotifsActions.setOnCheckedChangeListener { compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsEvents.setOnCheckedChangeListener { compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsGroups.setOnCheckedChangeListener { compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsMessages.setOnCheckedChangeListener { compoundButton, b ->
            checkSwitchs()
        }

        binding.validate.button.setOnClickListener {
            homePresenter.notificationsPermission.value?.action = binding.uiSwitchNotifsActions.isChecked
            homePresenter.notificationsPermission.value?.neighborhood = binding.uiSwitchNotifsGroups.isChecked
            homePresenter.notificationsPermission.value?.outing = binding.uiSwitchNotifsEvents.isChecked
            homePresenter.notificationsPermission.value?.chat_message = binding.uiSwitchNotifsMessages.isChecked
            homePresenter.updateNotificationsPermissions()
            dismiss()
        }
    }

    private fun checkSwitchs() {
        val isAllOn = binding.uiSwitchNotifsActions.isChecked ||
                binding.uiSwitchNotifsEvents.isChecked ||
                binding.uiSwitchNotifsGroups.isChecked ||
                binding.uiSwitchNotifsMessages.isChecked
        binding.uiSwitchNotifsAll.isChecked = isAllOn
    }

    private fun handleCloseButton() {
        binding.header.iconCross.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "SettingsNotifsFragment"
        fun newInstance(): SettingsNotificationsFragment {
            return SettingsNotificationsFragment()
        }
    }
}
