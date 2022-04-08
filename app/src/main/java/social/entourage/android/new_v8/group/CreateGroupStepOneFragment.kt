package social.entourage.android.new_v8.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import social.entourage.android.databinding.NewFragmentCreateGroupStepOneBinding


class CreateGroupStepOneFragment : Fragment() {


    private var _binding: NewFragmentCreateGroupStepOneBinding? = null
    val binding: NewFragmentCreateGroupStepOneBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewFragmentCreateGroupStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }
}