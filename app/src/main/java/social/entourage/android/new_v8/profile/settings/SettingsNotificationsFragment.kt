package social.entourage.android.new_v8.profile.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.new_fragment_settings_notifs.*
import social.entourage.android.databinding.NewFragmentSettingsNotifsBinding
import social.entourage.android.new_v8.home.HomePresenter
import social.entourage.android.new_v8.models.NotifInAppPermission

class SettingsNotificationsFragment : BottomSheetDialogFragment() {

    private var _binding: NewFragmentSettingsNotifsBinding? = null
    val binding: NewFragmentSettingsNotifsBinding get() = _binding!!

    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsNotifsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populate()
        handleCloseButton()

        homePresenter.notificationsPermission.observe(requireActivity(), ::updateSwitch)
        homePresenter.getNotificationsPermissions()
    }

    private fun updateSwitch(notifsPermissions: NotifInAppPermission?) {
        notifsPermissions?.let {
            binding.uiSwitchNotifsActions.isChecked = it.action
            binding.uiSwitchNotifsEvents.isChecked = it.outing
            binding.uiSwitchNotifsGroups.isChecked = it.neighborhood
            binding.uiSwitchNotifsMessages.isChecked = it.chat_message

            binding.uiSwitchNotifsAll.isChecked = it.isAllChecked()
        }
    }

    private fun populate() {
        binding.uiSwitchNotifsAll.setOnClickListener {
            val isOn = binding.uiSwitchNotifsAll.isChecked
            binding.uiSwitchNotifsActions.isChecked = isOn
            binding.uiSwitchNotifsEvents.isChecked = isOn
            binding.uiSwitchNotifsGroups.isChecked = isOn
            binding.uiSwitchNotifsMessages.isChecked = isOn
        }

        binding.uiSwitchNotifsActions.setOnCheckedChangeListener {  compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsEvents.setOnCheckedChangeListener {  compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsGroups.setOnCheckedChangeListener {  compoundButton, b ->
            checkSwitchs()
        }

        binding.uiSwitchNotifsMessages.setOnCheckedChangeListener {  compoundButton, b ->
            checkSwitchs()
        }

        binding.validate.button.setOnClickListener {
                homePresenter.notificationsPermission.value?.action = ui_switch_notifs_actions.isChecked
                homePresenter.notificationsPermission.value?.neighborhood = ui_switch_notifs_groups.isChecked
                homePresenter.notificationsPermission.value?.outing = ui_switch_notifs_events.isChecked
                homePresenter.notificationsPermission.value?.chat_message = ui_switch_notifs_messages.isChecked

            homePresenter.updateNotificationsPermissions()
            dismiss()
        }
    }

    private fun checkSwitchs() {
        val isAllOn = binding.uiSwitchNotifsActions.isChecked &&
                binding.uiSwitchNotifsEvents.isChecked &&
                binding.uiSwitchNotifsGroups.isChecked &&
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