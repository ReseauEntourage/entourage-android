package social.entourage.android.new_v8.groups.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import social.entourage.android.databinding.NewFragmentCreateGroupSuccessBinding
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.groups.details.posts.CreatePostActivity
import social.entourage.android.new_v8.user.UserProfileFragmentArgs
import social.entourage.android.new_v8.utils.Const


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
        return binding.root
    }

    private fun handlePassButton() {
        binding.pass.setOnClickListener {
            startActivity(
                Intent(requireContext(), FeedActivity::class.java).putExtra(
                    Const.GROUP_ID,
                    args.groupID
                )
            )
        }
    }

    private fun handlePostButton() {
        binding.post.setOnClickListener {
            val intent = Intent(context, CreatePostActivity::class.java)
            intent.putExtra(Const.GROUP_ID, args.groupID)
            startActivity(intent)
        }
    }
}