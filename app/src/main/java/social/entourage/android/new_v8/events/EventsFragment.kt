package social.entourage.android.new_v8.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentEventsBinding


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
}