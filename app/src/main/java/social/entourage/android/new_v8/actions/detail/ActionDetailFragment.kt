package social.entourage.android.new_v8.actions.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import social.entourage.android.databinding.NewFragmentActionDetailBinding
import social.entourage.android.new_v8.utils.Const
import timber.log.Timber

class ActionDetailFragment : Fragment() {
    private var _binding: NewFragmentActionDetailBinding? = null
    val binding: NewFragmentActionDetailBinding get() = _binding!!

    private var actionId:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            actionId = it.getInt(Const.ACTION_ID)
            Timber.d("***** Args ? ${actionId}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewFragmentActionDetailBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}