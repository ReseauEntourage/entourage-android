package social.entourage.android.new_v8.events.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentCreateEventSuccessBinding


class CreateEventSuccessFragment : Fragment() {
    private var _binding: NewFragmentCreateEventSuccessBinding? = null
    val binding: NewFragmentCreateEventSuccessBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }
}