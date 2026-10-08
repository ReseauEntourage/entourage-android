package social.entourage.android.events.create

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Events
import social.entourage.android.databinding.FragmentCreateEventBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.serializableExtra
import timber.log.Timber

/**
 * Conteneur de l'assistant de création / d'édition : barre du haut, progression, libellé
 * « Étape N sur X » et pied de page Retour / Continuer. Les étapes et l'aperçu sont des pages
 * d'un ViewPager2 qui partagent le [CreateEventViewModel] de l'activité.
 */
class CreateEventFragment : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    val binding: FragmentCreateEventBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    private lateinit var viewPager: ViewPager2

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private var isAlreadySend = false

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (_binding != null && viewPager.currentItem > 0) {
                goToPage(viewPager.currentItem - 1)
            } else {
                confirmExit()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)

        updatePaddingForEdgeToEdge(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel(savedInstanceState == null)
        initializeViewPager()
        handleTopBar()
        handleFooter()
        eventPresenter.newEventCreated.observe(viewLifecycleOwner, ::handleCreateEventResponse)
        eventPresenter.isEventUpdated.observe(viewLifecycleOwner, ::isEventUpdated)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
        binding.title.text =
            getString(if (viewModel.isEdition) R.string.edit_event else R.string.new_event)
        binding.save.visibility = if (viewModel.isEdition) View.GONE else View.VISIBLE
    }

    private fun initializeViewModel(announceDraft: Boolean) {
        val activity = requireActivity()
        val edited = activity.intent?.serializableExtra<Events>(Const.EVENT_UI)
        // Pas de brouillon en édition : il n'est ni lu ni écrit.
        val draft = if (edited == null) {
            EntourageApplication.me(activity)?.id?.let { CreateEventDraftStore.load(activity, it) }
        } else null
        val groupId = activity.intent?.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
            ?.takeIf { it != Const.DEFAULT_VALUE }
        viewModel.initialize(edited, draft, groupId)
        if (announceDraft && viewModel.draftRestored) {
            Utils.showToast(requireContext(), getString(R.string.create_event_draft_restored))
        }
    }

    private fun initializeViewPager() {
        viewPager = binding.viewPager
        viewPager.isUserInputEnabled = false
        viewPager.adapter = CreateEventViewPagerAdapter(childFragmentManager, lifecycle, viewModel.steps)
        binding.progressBar.max = viewModel.steps.size
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
                updateChrome(position)
            }
        })
        // La vue est recréée au retour de la sélection d'adresse : on reprend là où on était.
        val page = viewModel.currentPage.value ?: 0
        viewPager.setCurrentItem(page, false)
        updateChrome(page)
    }

    /** Libellé d'étape, progression et boutons du pied de page pour la page affichée. */
    private fun updateChrome(page: Int) {
        val stepCount = viewModel.steps.size
        val isPreview = page >= viewModel.previewPageIndex
        binding.stepLabel.text = if (isPreview) {
            getString(R.string.create_event_last_step)
        } else {
            getString(R.string.create_event_step_progress, page + 1, stepCount)
        }
        binding.progressBar.setProgress((page + 1).coerceAtMost(stepCount), true)
        binding.previous.visibility = if (page == 0) View.INVISIBLE else View.VISIBLE
        binding.next.text = getString(
            when {
                !isPreview -> R.string.create_event_continue
                viewModel.isEdition -> R.string.edit
                else -> R.string.create_event_publish
            }
        )
    }

    private fun handleTopBar() {
        binding.iconBack.setOnClickListener { confirmExit() }
        binding.save.setOnClickListener { saveDraftAndExit() }
    }

    private fun handleFooter() {
        binding.previous.setOnClickListener {
            if (viewPager.currentItem > 0) goToPage(viewPager.currentItem - 1)
        }
        binding.next.setOnClickListener { onNextClicked() }
    }

    private fun goToPage(page: Int) {
        hideKeyboard()
        viewPager.setCurrentItem(page, true)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun onNextClicked() {
        val page = viewPager.currentItem
        if (page < viewModel.steps.size) {
            if (!viewModel.validateStep(viewModel.steps[page])) return
            val nextIsPreview = page + 1 == viewModel.previewPageIndex
            if (nextIsPreview && viewModel.isUploading.value == true) {
                // L'aperçu ne s'ouvre pas tant que la photo n'est pas envoyée.
                Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
                return
            }
            goToPage(page + 1)
        } else {
            publish()
        }
    }

    // --- Envoi ----------------------------------------------------------------------------

    private fun publish() {
        if (viewModel.isUploading.value == true) {
            Utils.showToast(requireContext(), getString(R.string.create_event_photo_uploading_wait))
            return
        }
        val edited = viewModel.editedEvent
        if (edited != null) {
            if (edited.recurrence != null) {
                showAlertDialogUpdateEventWithRecurrence()
            } else {
                updateEventWithoutRecurrence()
            }
        } else {
            if (isAlreadySend) return
            isAlreadySend = true
            eventPresenter.createEvent(requestBody())
        }
    }

    private fun requestBody(): CreateEvent =
        viewModel.buildRequestBody(getString(R.string.event_date_formatter_to_string))

    private fun updateEventWithRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        viewModel.editedEvent?.id?.let {
            eventPresenter.updateEventSiblings(it, requestBody())
        }
    }

    private fun updateEventWithoutRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        viewModel.editedEvent?.id?.let {
            eventPresenter.updateEvent(it, requestBody())
        }
    }

    private fun showAlertDialogUpdateEventWithRecurrence() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val customDialog: View =
            layoutInflater.inflate(R.layout.layout_custom_alert_dialog_cancel_event, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(customDialog)
        val alertDialog = builder.create()
        val cancelOneEvent = customDialog.findViewById<RadioButton>(R.id.one_event)
        val cancelAllEvents =
            customDialog.findViewById<RadioButton>(R.id.all_events_recurrent)
        val radioGroup = customDialog.findViewById<android.widget.RadioGroup>(R.id.recurrence)

        val btnYes = customDialog.findViewById<Button>(R.id.yes)
        with(btnYes) {
            this.background = requireContext().getDrawable(R.drawable.btn_shape_light_orange)
            text = getString(R.string.validate)
            isEnabled = false

            setOnClickListener {
                if (cancelOneEvent.isChecked) updateEventWithoutRecurrence()
                if (cancelAllEvents.isChecked) updateEventWithRecurrence()
                alertDialog.dismiss()
                activity?.finish()
            }
        }

        radioGroup.setOnCheckedChangeListener { _, _ ->
            btnYes.isEnabled = true
            btnYes.background = requireContext().getDrawable(R.drawable.btn_shape_orange_alert_dialog)
        }
        customDialog.findViewById<TextView>(R.id.title).text =
            getString(R.string.event_edit_recurrent_event)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    private fun isEventUpdated(updated: Boolean) {
        if (updated) {
            Utils.showToast(requireContext(), getString(R.string.group_updated))
            activity?.finish()
            RefreshController.shouldRefreshEventFragment = true
        } else {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.group_error_updated))
        }
    }

    private fun handleCreateEventResponse(eventCreated: Events?) {
        if (eventCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            eventPresenter.newEventCreated.value?.id?.let { eventID ->
                if (!viewModel.isEdition) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_end)
                    // Publication réussie : le brouillon n'a plus de raison d'être.
                    EntourageApplication.me(requireContext())?.id?.let {
                        viewModel.deleteDraft(requireContext(), it)
                    }
                }
                val action =
                    CreateEventFragmentDirections.actionCreateEventFragmentToCreateEventSuccessFragment(
                        eventID
                    )
                try {
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Utils.showToast(requireContext(), getString(R.string.error_create_group))
                    Timber.e(e, "Navigation error in CreateEventFragment")
                }
            }
        }
    }

    // --- Sortie ---------------------------------------------------------------------------

    /** Quitter sans enregistrer : les modifications sont perdues (création comme édition). */
    private fun confirmExit() {
        if (!viewModel.isDirty()) {
            requireActivity().finish()
            return
        }
        CustomAlertDialog.showWithCancelFirst(
            requireContext(),
            getString(R.string.back_create_group_title),
            getString(R.string.back_create_event_content),
            getString(R.string.exit)
        ) {
            requireActivity().finish()
        }
    }

    private fun saveDraftAndExit() {
        if (viewModel.isEdition) return
        val userId = EntourageApplication.me(requireContext())?.id ?: return
        viewModel.saveDraft(requireContext(), userId)
        Utils.showToast(requireContext(), getString(R.string.create_event_draft_saved))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
