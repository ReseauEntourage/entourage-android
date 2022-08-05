package social.entourage.android.new_v8.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentAboutEventBinding
import social.entourage.android.new_v8.models.GroupUiModel

class AboutEventFragment : Fragment() {

    private var _binding: NewFragmentAboutEventBinding? = null
    val binding: NewFragmentAboutEventBinding get() = _binding!!
    var event: GroupUiModel? = null
    //private val args: AboutEventFragment by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAboutEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // event = args.group
    }
}