package social.entourage.android.new_v8.profile.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentSettingsBinding
import social.entourage.android.new_v8.utils.Utils

class SettingsFragment : Fragment() {

    private var _binding: NewFragmentSettingsBinding? = null
    val binding: NewFragmentSettingsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        addOnClickListeners()
    }

    private fun initializeView() {
        binding.deleteAccount.divider.visibility = View.GONE
        binding.deleteAccount.arrow.visibility = View.GONE
        binding.signOut.arrow.visibility = View.GONE
    }

    private fun addOnClickListeners() {
        binding.share.layout.setOnClickListener {
            shareApplication()
        }
        binding.deleteAccount.layout.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.delete_account_dialog_title),
                getString(R.string.delete_account_dialog_content),
                getString(R.string.delete),
                {}
            )
        }
        binding.signOut.layout.setOnClickListener {
            Utils.showAlertDialogButtonClicked(
                requireView(),
                getString(R.string.sign_out_dialog_title),
                getString(R.string.sign_out_dialog_content),
                getString(R.string.signing_out),
                {}
            )
        }
        binding.helpAbout.layout.setOnClickListener {
            findNavController()
                .navigate(R.id.action_profile_fragment_to_help_about_fragment)
        }
    }

    private fun shareApplication() {
        val intent = Intent(Intent.ACTION_SEND)
        val msgBody = getString(R.string.share_app)
        intent.putExtra(Intent.EXTRA_TEXT, msgBody)
        intent.type = "text/plain"
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }
}