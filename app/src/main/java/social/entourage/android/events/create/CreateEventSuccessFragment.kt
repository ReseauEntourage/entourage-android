package social.entourage.android.events.create

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.play.core.review.ReviewManagerFactory
import social.entourage.android.databinding.NewFragmentCreateEventSuccessBinding
import social.entourage.android.RefreshController
import social.entourage.android.events.details.feed.FeedActivity
import social.entourage.android.tools.utils.Const

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
            startActivityForResult(
                Intent(requireContext(), FeedActivity::class.java).putExtra(
                    Const.EVENT_ID,
                    args.eventID
                ), 0
            )
            requireActivity().finish()
            RefreshController.shouldRefreshEventFragment = true
        }
    }

    override fun onResume() {
        super.onResume()
        requestInAppReview(requireContext())
    }

    fun requestInAppReview(context: Context) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val flow = manager.launchReviewFlow(context as Activity, task.result)
                flow.addOnCompleteListener {
                }
            } else {
            }
        }
    }
}