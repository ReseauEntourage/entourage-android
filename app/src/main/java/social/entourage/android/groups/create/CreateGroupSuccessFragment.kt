package social.entourage.android.groups.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import social.entourage.android.databinding.NewFragmentCreateGroupSuccessBinding
import social.entourage.android.RefreshController
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.groups.details.feed.FeedActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents

class CreateGroupSuccessFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupSuccessBinding? = null
    val binding: NewFragmentCreateGroupSuccessBinding get() = _binding!!

    private val args: CreateGroupSuccessFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handlePassButton()
        handlePostButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupSuccessBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_NEW_GROUP_CONFIRMATION
        )
        return binding.root
    }

    private fun handlePassButton() {
        binding.pass.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_CONFIRMATION_SKIP
            )
            startActivityForResult(
                Intent(requireContext(), FeedActivity::class.java).putExtra(
                    Const.GROUP_ID,
                    args.groupID
                ), 0
            )
            requireActivity().finish()
            RefreshController.shouldRefreshFragment = true
        }
    }

    private fun handlePostButton() {
        binding.post.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_CONFIRMATION_NEW_POST
            )
            val intent = Intent(context, CreatePostGroupActivity::class.java)
            intent.putExtra(Const.GROUP_ID, args.groupID)
            intent.putExtra(Const.FROM_CREATE_GROUP, true)
            startActivityForResult(intent, 0)
            requireActivity().finish()
            RefreshController.shouldRefreshFragment = true
        }
    }
}