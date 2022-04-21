package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentCreateGroupStepThreeBinding


class CreateGroupStepThreeFragment : Fragment() {

    private var _binding: NewFragmentCreateGroupStepThreeBinding? = null
    val binding: NewFragmentCreateGroupStepThreeBinding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateGroupStepThreeBinding.inflate(inflater, container, false)
        return binding.root
    }

}