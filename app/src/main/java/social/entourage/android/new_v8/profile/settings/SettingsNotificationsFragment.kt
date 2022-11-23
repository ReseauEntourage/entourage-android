package social.entourage.android.new_v8.profile.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.new_fragment_settings_notifs.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentHelpAboutBinding
import social.entourage.android.databinding.NewFragmentSettingsNotifsBinding
import social.entourage.android.new_v8.home.HomePresenter
import social.entourage.android.new_v8.models.NotifInAppPermission
import social.entourage.android.new_v8.profile.ProfileActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar

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
        setBackButton()
        populate()
        handleCloseButton()

        homePresenter.notificationsPermission.observe(requireActivity(), ::updateSwitch)
        homePresenter.getNotificationsPermissions()
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun updateSwitch(notifsPermissions: NotifInAppPermission?) {
        notifsPermissions?.let {
            binding.uiSwitchNotifsActions.isChecked = it.action
            binding.uiSwitchNotifsEvents.isChecked = it.outing
            binding.uiSwitchNotifsGroups.isChecked = it.neighborhood
            binding.uiSwitchNotifsMessages.isChecked = it.chat_message
        }
    }

    private fun populate() {
        binding.uiSwitchNotifsAll.setOnCheckedChangeListener { compoundButton, b ->
                binding.uiSwitchNotifsActions.isChecked = b
                binding.uiSwitchNotifsEvents.isChecked = b
                binding.uiSwitchNotifsGroups.isChecked = b
                binding.uiSwitchNotifsMessages.isChecked = b
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

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
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