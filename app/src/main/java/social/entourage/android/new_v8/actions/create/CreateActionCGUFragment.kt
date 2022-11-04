package social.entourage.android.new_v8.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateActionCguBinding
import social.entourage.android.new_v8.groups.details.rules.RulesListAdapter
import social.entourage.android.new_v8.models.Rules

class CreateActionCGUFragment : Fragment() {

    private var _binding: NewFragmentCreateActionCguBinding? = null
    val binding: NewFragmentCreateActionCguBinding get() = _binding!!

    private var isDemand = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isDemand = CreateActionCGUFragmentArgs.fromBundle(it).isActionDemand
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionCguBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNextClickListener()
        initializeGroups()
        handleBackButton()
    }

    private fun initializeGroups() {
        val line1 = Rules(getString(R.string.action_CGU_1_title),getString(R.string.action_CGU_1))
        val line2 = Rules(getString(R.string.action_CGU_2_title),getString(R.string.action_CGU_2))
        val line3 = Rules(getString(R.string.action_CGU_3_title),getString(R.string.action_CGU_3))
        val line4 = Rules(getString(R.string.action_CGU_4_title),getString(R.string.action_CGU_4))
        val line5 = Rules(getString(R.string.action_CGU_5_title),getString(R.string.action_CGU_5))
        var cgus = arrayListOf(line1,line2,line3,line4,line5)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RulesListAdapter(cgus)
        }
    }

    private fun setNextClickListener() {

        binding.accept.setOnClickListener {
            val action = CreateActionCGUFragmentDirections.actionCreateActionCguFragmentToCreateActionFragment(isDemand)
            action.isActionDemand = isDemand
            findNavController().navigate(action)
        }
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }
}