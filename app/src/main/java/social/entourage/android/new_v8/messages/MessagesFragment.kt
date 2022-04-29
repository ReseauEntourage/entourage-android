package social.entourage.android.new_v8.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: NewFragmentMessagesBinding? = null
    val binding: NewFragmentMessagesBinding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }
}