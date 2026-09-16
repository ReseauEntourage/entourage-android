package social.entourage.android.actions.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import social.entourage.android.R
import social.entourage.android.api.model.Action
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingForEdgeToEdge
import social.entourage.android.tools.view.WebViewFragment

class CreateActionCGUFragment : Fragment() {

    private var isDemand = false
    private var actionEdited: Action? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            isDemand = CreateActionCGUFragmentArgs.fromBundle(it).isActionDemand
            actionEdited = CreateActionCGUFragmentArgs.fromBundle(it).actionObj
        }

        if (actionEdited == null) {
            if (isDemand) {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_demand_chart)
            } else {
                AnalyticsEvents.logEvent(AnalyticsEvents.Help_create_contrib_chart)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            updatePaddingForEdgeToEdge(this)
            setContent {
                CreateActionCharterScreen(
                    isDemand = isDemand,
                    onBackClick = { requireActivity().finish() },
                    onReadFullCharterClick = { openFullCharter() },
                    onAcceptClick = { navigateToCreateAction() }
                )
            }
        }
    }

    private fun openFullCharter() {
        WebViewFragment.newInstance(getString(R.string.action_cgu_full_charter_url), sendRead = true)
            .show(parentFragmentManager, WebViewFragment.TAG)
    }

    private fun navigateToCreateAction() {
        val action = CreateActionCGUFragmentDirections
            .actionCreateActionCguFragmentToCreateActionFragment(isDemand)
        action.isActionDemand = isDemand
        action.actionObj = actionEdited
        findNavController().navigate(action)
    }
}
