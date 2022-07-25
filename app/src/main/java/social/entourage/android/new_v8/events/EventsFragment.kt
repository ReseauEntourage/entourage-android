package social.entourage.android.new_v8.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentEventsBinding
import social.entourage.android.new_v8.events.create.CreateEventActivity
import social.entourage.android.new_v8.groups.create.CreateGroupActivity
import social.entourage.android.tools.log.AnalyticsEvents


class EventsFragment : Fragment() {
    private var _binding: NewFragmentEventsBinding? = null
    val binding: NewFragmentEventsBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createEvent()
    }

    private fun createEvent() {
        binding.createEvent.setOnClickListener {
            startActivity(
                Intent(context, CreateEventActivity::class.java)
            )
        }
    }
}