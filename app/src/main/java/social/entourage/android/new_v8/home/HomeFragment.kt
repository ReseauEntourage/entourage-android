package social.entourage.android.new_v8.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewFragmentHomeBinding
import social.entourage.android.new_v8.models.Summary
import social.entourage.android.new_v8.profile.ProfileActivity

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
        with(binding) {
            summary.meetingsCount?.let {
                meetingLabel.text =
                    if (it <= 0) getString(R.string.contributions_meeting_empty) else getString(R.string.contributions_meeting)
                meetingValue.text = it.toString()
                heartIcon.isVisible = it > 0
                heartIconEmpty.isVisible = it <= 0
            }

            summary.chatMessagesCount?.let {
                messageCard.value.text = it.toString()
                messageCard.isEmpty.isVisible = it <= 0
                messageCard.isNotEmpty.isVisible = it > 0
            }

            summary.outingParticipationsCount?.let {
                eventCard.value.text = it.toString()
                eventCard.isEmpty.isVisible = it <= 0
                eventCard.isNotEmpty.isVisible = it > 0
            }

            summary.neighborhoodParticipationsCount?.let {
                groupCard.value.text = it.toString()
                groupCard.isEmpty.isVisible = it <= 0
                groupCard.isNotEmpty.isVisible = it > 0
            }
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