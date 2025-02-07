package social.entourage.android.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.Rules
import social.entourage.android.databinding.NewFragmentCreateActionCguBinding
import social.entourage.android.groups.details.rules.RulesListAdapter
import social.entourage.android.tools.log.AnalyticsEvents

class CreateActionCGUFragment : Fragment() {

    private var _binding: NewFragmentCreateActionCguBinding? = null
    val binding: NewFragmentCreateActionCguBinding get() = _binding!!

    private var isDemand = false
    private var actionEdited: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isDemand = CreateActionCGUFragmentArgs.fromBundle(it).isActionDemand
            actionEdited = CreateActionCGUFragmentArgs.fromBundle(it).actionObj
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateActionCguBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layout) { view, windowInsets ->
            // Get the insets for the statusBars() type:
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(
                top = insets.top
            )
            // Return the original insets so they aren’t consumed
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNextClickListener()
        initializeGroups()
        handleBackButton()

        if (actionEdited == null) {
            if (isDemand) {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_demand_chart)
            }
            else {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_contrib_chart)
            }
        }
    }

    private fun initializeGroups() {
        val line1 = Rules(getString(R.string.action_CGU_1_title),getString(R.string.action_CGU_1))
        val line2 = Rules(getString(R.string.action_CGU_2_title),getString(R.string.action_CGU_2))
        val line3 = Rules(getString(R.string.action_CGU_3_title),getString(R.string.action_CGU_3))
        val line4 = Rules(getString(R.string.action_CGU_4_title),getString(R.string.action_CGU_4))
        val line5 = Rules(getString(R.string.action_CGU_5_title),getString(R.string.action_CGU_5))
        val line6 = Rules(getString(R.string.friendly_links_title),getString(R.string.friendly_links_text))
        val cgus = arrayListOf(line1,line2,line3,line4,line5,line6)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RulesListAdapter(cgus)
        }
    }

    private fun setNextClickListener() {

        binding.accept.setOnClickListener {
            val action = CreateActionCGUFragmentDirections.actionCreateActionCguFragmentToCreateActionFragment(isDemand)
            action.isActionDemand = isDemand
            action.actionObj = actionEdited
            findNavController().navigate(action)
        }
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }
}