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
    private var summary: Summary? = null


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
        homePresenter.summary.observe(requireActivity(),::updateContributionsView)
        updateView()
        handleProfileButton()
    }

    private fun updateContributionsView(summary: Summary) {
        val meetingsCount: Long? = if (summary == null ) 0 else summary.meetingsCount;
        val messagesCount: Long? = if (summary == null) 0 else summary.chatMessagesCount;
        val eventsCount: Long? = if (summary == null) 0 else summary.outingParticipationsCount;
        val groupsCount: Long? = if (summary == null) 0 else summary.neighborhoodParticipationsCount;
        val isMeetingsEmpty: Boolean = (meetingsCount != null && meetingsCount <= 0)
        val isMessagesEmpty: Boolean = (messagesCount != null && messagesCount <= 0)
        val isEventsEmpty: Boolean = (eventsCount != null && eventsCount <= 0)
        val isGroupsEmpty: Boolean = (groupsCount != null && groupsCount <= 0)

        binding.meetingValue.text = meetingsCount.toString()
        binding.meetingLabel.text = if (isMeetingsEmpty) getString(R.string.contributions_meeting_empty) else getString(R.string.contributions_meeting)
        binding.heartIcon.visibility = if (isMeetingsEmpty) View.GONE else View.VISIBLE
        binding.heartIconEmpty.visibility = if (isMeetingsEmpty) View.VISIBLE else View.GONE



        binding.messageCard.value.text = messagesCount.toString()
        binding.messageCard.value.visibility = if (isMessagesEmpty) View.GONE else View.VISIBLE
        binding.messageCard.icon.visibility = if (isMessagesEmpty) View.VISIBLE else View.GONE
        binding.messageCard.label.visibility = if (isMessagesEmpty) View.GONE else View.VISIBLE
        binding.messageCard.labelEmpty.visibility = if (isMessagesEmpty) View.VISIBLE else View.GONE

        binding.eventCard.value.text = eventsCount.toString()
        binding.eventCard.value.visibility = if (isEventsEmpty) View.GONE else View.VISIBLE
        binding.eventCard.icon.visibility = if (isEventsEmpty) View.VISIBLE else View.GONE
        binding.eventCard.label.visibility = if (isEventsEmpty) View.GONE else View.VISIBLE
        binding.eventCard.labelEmpty.visibility = if (isEventsEmpty) View.VISIBLE else View.GONE

        binding.groupCard.value.text=groupsCount.toString()
        binding.groupCard.value.visibility = if (isGroupsEmpty) View.GONE else View.VISIBLE
        binding.groupCard.icon.visibility = if (isGroupsEmpty) View.VISIBLE else View.GONE
        binding.groupCard.label.visibility = if (isGroupsEmpty) View.GONE else View.VISIBLE
        binding.groupCard.labelEmpty.visibility = if (isGroupsEmpty) View.VISIBLE else View.GONE

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