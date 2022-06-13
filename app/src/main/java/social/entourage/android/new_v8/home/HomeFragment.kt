package social.entourage.android.new_v8.home

import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Summary
import social.entourage.android.new_v8.profile.ProfileActivity
import timber.log.Timber

class HomeFragment : Fragment() {
    private var _binding: NewFragmentHomeBinding? = null
    val binding: NewFragmentHomeBinding get() = _binding!!
    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    private var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = EntourageApplication.me(activity) ?: return
        homePresenter.getSummary()
        homePresenter.summary.observe(requireActivity(), ::updateContributionsView)
        updateView()
        handleProfileButton()
    }

    private fun updateContributionsView(summary: Summary) {
        summary.meetingsCount?.let {
            binding.meetingLabel.text =
                if (it <= 0) getString(R.string.contributions_meeting_empty) else getString(R.string.contributions_meeting)
            binding.meetingValue.text = it.toString()
            binding.heartIcon.visibility = if (it <= 0) View.GONE else View.VISIBLE
            binding.heartIconEmpty.visibility = if (it <= 0) View.VISIBLE else View.GONE
        }

        summary.chatMessagesCount?.let {
            binding.messageCard.value.text = it.toString()
            binding.messageCard.isEmpty.visibility = if (it <= 0) View.VISIBLE else View.GONE
            binding.messageCard.isNotEmpty.visibility = if (it <= 0) View.GONE else View.VISIBLE
        }

        summary.outingParticipationsCount?.let {
            binding.eventCard.value.text = it.toString()
            binding.eventCard.isEmpty.visibility = if (it <= 0) View.VISIBLE else View.GONE
            binding.eventCard.isNotEmpty.visibility = if (it <= 0) View.GONE else View.VISIBLE
        }

        summary.neighborhoodParticipationsCount?.let {
            binding.groupCard.value.text = it.toString()
            binding.groupCard.isEmpty.visibility = if (it <= 0) View.VISIBLE else View.GONE
            binding.groupCard.isNotEmpty.visibility = if (it <= 0) View.GONE else View.VISIBLE
        }
    }

    private fun updateView() {
        with(binding) {
            imageUser.let { photoView ->
                user?.avatarURL?.let { avatarURL ->
                    Glide.with(requireActivity())
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(photoView)
                } ?: run {
                    photoView.setImageResource(R.drawable.ic_user_photo_small)
                }

            }
            welcomeUser.text = String.format(
                getString(R.string.welcome_user),
                user?.displayName
            )


        }
    }

    private fun handleProfileButton() {
        binding.imageUser.setOnClickListener {
            startActivity(
                Intent(context, ProfileActivity::class.java)
            )
        }
    }
}