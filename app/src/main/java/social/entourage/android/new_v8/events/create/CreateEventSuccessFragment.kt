package social.entourage.android.new_v8.events.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import social.entourage.android.databinding.NewFragmentCreateEventSuccessBinding
import social.entourage.android.new_v8.events.details.feed.FeedActivity
import social.entourage.android.new_v8.groups.RefreshController
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents


class CreateEventSuccessFragment : Fragment() {

    private var _binding: NewFragmentCreateEventSuccessBinding? = null
    val binding: NewFragmentCreateEventSuccessBinding get() = _binding!!

    private val args: CreateEventSuccessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSeeEventButton()
    }

    private fun handleSeeEventButton() {
        binding.seeEvent.setOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_NEW_GROUP_CONFIRMATION_SKIP
            )
            startActivity(
                Intent(requireContext(), FeedActivity::class.java).putExtra(
                    Const.EVENT_ID,
                    args.eventID
                )
            )
            requireActivity().finish()
            RefreshController.shouldRefreshFragment = true
        }
    }
}