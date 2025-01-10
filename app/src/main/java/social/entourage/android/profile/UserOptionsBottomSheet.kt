package social.entourage.android.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.BottomSheetProfilOptionLayoutBinding
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog

class UserOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetProfilOptionLayoutBinding? = null
    private val binding get() = _binding!!
    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetProfilOptionLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reportUser.setOnClickListener {
            val reportUserBottomDialogFragment =
                ReportModalFragment.newInstance(
                    user?.id ?: 0,
                    Const.DEFAULT_VALUE,
                    ReportTypes.REPORT_USER
                    ,false,false, false, contentCopied = "" )
            reportUserBottomDialogFragment.show(parentFragmentManager, ReportModalFragment.TAG)
            dismiss()
        }

        binding.blockUser.setOnClickListener {
            val desc = String.format(
                getString(R.string.params_block_user_conv_pop_message),
                user?.displayName ?: "cet utilisateur"
            )
            CustomAlertDialog.showButtonClickedWithCrossClose(
                requireContext(),
                getString(R.string.params_block_user_conv_pop_title),
                desc,
                getString(R.string.params_block_user_conv_pop_bt_cancel),
                getString(R.string.params_block_user_conv_pop_bt_quit), showCross = false, onNo = {}, onYes = {
                    //TODO: la suite
                    user.let {
                        discussionPresenter.blockUser(it?.id ?: 0)
                        dismiss()
                    }
                }
            )
        }

        binding.blockUserImage.setOnClickListener {
            val desc = String.format(
                getString(R.string.params_block_user_conv_pop_message),
                user?.displayName ?: "cet utilisateur"
            )
            CustomAlertDialog.showButtonClickedWithCrossClose(
                requireContext(),
                getString(R.string.params_block_user_conv_pop_title),
                desc,
                getString(R.string.params_block_user_conv_pop_bt_cancel),
                getString(R.string.params_block_user_conv_pop_bt_quit), showCross = false, onNo = {}, onYes = {
                    //TODO: la suite
                    user.let {
                        discussionPresenter.blockUser(it?.id ?: 0)
                        dismiss()
                    }
                }
            )
        }
        binding.reportUserImage.setOnClickListener {
            val reportUserBottomDialogFragment =
                ReportModalFragment.newInstance(
                    user?.id ?: 0,
                    Const.DEFAULT_VALUE,
                    ReportTypes.REPORT_USER
                    ,false,false, false, contentCopied = "" )
            reportUserBottomDialogFragment.show(parentFragmentManager, ReportModalFragment.TAG)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "UserOptionsBottomSheet"
        var user: User? = null
    }
}
