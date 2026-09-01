package social.entourage.android.enhanced_onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.databinding.FragmentOnboardingCongratsFragmentBinding
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding
import social.entourage.android.enhanced_onboarding.OnboardingViewModel
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.list.EVENTS_PER_PAGE
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.tools.log.AnalyticsEvents

class OnboardingCongratsFragment: Fragment() {

    private lateinit var binding: FragmentOnboardingCongratsFragmentBinding
    private lateinit var viewModel: OnboardingViewModel
    private lateinit var eventsPresenter: EventsPresenter
    private var currentFilters = EventActionLocationFilters()
    var haveEvent = false
    var category: String? = "both_actions"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOnboardingCongratsFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(OnboardingViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_end_view)
        binding.lottieAnimation.setAnimation(R.raw.congrats_animation)
        binding.lottieAnimation.playAnimation()
        binding.buttonStart.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_end_browse_events_clic)
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_end_congrats_clic_on_ + category)
            viewModel.registerAndQuit(category)
        }
        binding.buttonSkip.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.onboarding_end_skip_clic)
            viewModel.registerAndQuit(category)
        }
        eventsPresenter = ViewModelProvider(this).get(EventsPresenter::class.java)
        eventsPresenter.getFilteredEvents.observe(requireActivity(), ::handleResponseGetEvents)
        eventsPresenter.getAllEventsWithFilter(
            0,
            EVENTS_PER_PAGE,
            MainFilterActivity.savedGroupInterestsFromOnboarding.joinToString(","),
            currentFilters.travel_distance(),
            currentFilters.latitude(),
            currentFilters.longitude(),
            "future")

    }
    override fun onResume() {
        super.onResume()
        viewModel.toggleBtnBack(false)

    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        if(allEvents != null) {
            //create a list of event beeing not online
            val allEventsFiltered = allEvents.filter { it.online == false }
            haveEvent = allEventsFiltered.isNotEmpty()
            configureOnboardingView()
            if(MainActivity.isFromProfile) {
                binding.buttonStart.text = "Revenir au profil"
            }
            binding.progressbar.visibility = View.GONE
            binding.viewParent.visibility = View.VISIBLE
        }
    }

    private fun configureOnboardingView() {
        val categoriesList = viewModel.actionsWishes.value?.filter { it.isSelected }?.map { it.id } ?: listOf()
        var titleRes = R.string.onboarding_congrats_title
        var contentRes = R.string.onboarding_congrats_content
        var buttonTextRes = R.string.onboarding_congrats_leave

        // Priorité 0 : profil "solliciter" (préférence contribution) ayant choisi le coup de pouce -> demande d'aide
        if (EnhancedOnboarding.preference == "contribution" && categoriesList.contains("both_actions")) {
            titleRes = R.string.onboarding_start_action_ask_title
            contentRes = R.string.onboarding_start_action_ask_content
            buttonTextRes = R.string.onboarding_start_action_ask_button
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
            binding.buttonSkip.setText(R.string.onboarding_start_action_ask_skip)
            category = "ask_help"
        }
        // Condition par défaut : si la liste est vide ou que la préférence est "contribution"
        else if (categoriesList.isEmpty() || EnhancedOnboarding.preference == "contribution") {
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
            category = "event"
        }
        // Priorité 1 : Présence de "both_actions" dans la liste
        else if (categoriesList.contains("both_actions")) {
            titleRes = R.string.onboarding_start_action_title
            contentRes = R.string.onboarding_start_action_content
            buttonTextRes = R.string.onboarding_start_action_button
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
            category = "both_actions"
        }
        // Priorité 2 : Présence de "outings" avec distinction entre "event" et "no_event" via `haveEvent`
        else if (categoriesList.contains("outings")) {
            if (haveEvent) {
                titleRes = R.string.onboarding_experience_event_title
                contentRes = R.string.onboarding_experience_event_content
                buttonTextRes = R.string.onboarding_experience_event_button
                category = "event"
            } else {
                titleRes = R.string.onboarding_no_event_title
                contentRes = R.string.onboarding_no_event_content
                buttonTextRes = R.string.onboarding_no_event_button
                category = "no_event"
            }
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
        }
        // Priorité 3 : Présence de "resources" dans la liste
        else if (categoriesList.contains("resources")) {
            titleRes = R.string.onboarding_experience_resource_title
            contentRes = R.string.onboarding_experience_resource_content
            buttonTextRes = R.string.onboarding_experience_resource_button
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
            category = "resources"
        }
        // Priorité 4 : Présence de "neighborhoods" dans la liste
        else if (categoriesList.contains("neighborhoods")) {
            titleRes = R.string.onboarding_ready_action_title
            contentRes = R.string.onboarding_ready_action_content
            buttonTextRes = R.string.onboarding_ready_action_button
            binding.tvTitle.setText(titleRes)
            binding.tvDescription.setText(contentRes)
            binding.buttonStart.setText(buttonTextRes)
            category = "neighborhoods"
        }
    }

}