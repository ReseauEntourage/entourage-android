package social.entourage.android.new_v8.events.create

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventBinding
import social.entourage.android.new_v8.RefreshController
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.events.create.CommunicationHandler.canExitEventCreation
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.nextPage
import social.entourage.android.new_v8.utils.previousPage


class CreateEventFragment : Fragment() {

    private var _binding: NewFragmentCreateEventBinding? = null
    val binding: NewFragmentCreateEventBinding get() = _binding!!
    private var event: Events? = null


    private lateinit var viewPager: ViewPager2

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private var isAlreadySend = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewPager()
        handleBackButton()
        eventPresenter.newEventCreated.observe(viewLifecycleOwner, ::handleCreateEventResponse)
        eventPresenter.isEventUpdated.observe(viewLifecycleOwner, ::isEventUpdated)
        event = activity?.intent?.getSerializableExtra(Const.EVENT_UI) as Events?
        CommunicationHandler.eventEdited = event
        setView()
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

    private fun initializeViewPager() {
        viewPager = binding.viewPager
        val adapter = CreateEventViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, viewPager) { tab: TabLayout.Tab, _: Int ->
            tab.view.isClickable = false
        }.attach()
        setNextClickListener()
        setPreviousClickListener()
        handleNextButtonState()
    }

    private fun setNextClickListener() {
        binding.next.setOnClickListener {
            CommunicationHandler.clickNext.value = true
        }
        CommunicationHandler.isCondition.observe(
            viewLifecycleOwner,
            ::handleIsCondition
        )
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.next.text =
                    getString(
                        if (position == NB_TABS - 1) {
                            if (CommunicationHandler.eventEdited != null) {
                                R.string.edit
                            } else R.string.create
                        } else R.string.new_next
                    )
            }
        })
    }


    private fun handleIsCondition(isCondition: Boolean) {
        if (isCondition) {
            if (viewPager.currentItem == NB_TABS - 1) {
                // Create event here
                if (CommunicationHandler.eventEdited != null)
                    if (CommunicationHandler.eventEdited?.recurrence != null) {
                        showAlertDialogUpdateEventWithRecurrence()
                    } else {
                        updateEventWithoutRecurrence()
                    }
                else {
                    if (isAlreadySend) return
                    isAlreadySend = true
                    eventPresenter.createEvent(CommunicationHandler.event)
                }
            } else {
                viewPager.nextPage(true)
                if (viewPager.currentItem > 0) binding.previous.visibility = View.VISIBLE
                CommunicationHandler.resetValues()
            }
        }
    }

    private fun updateEventWithRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        CommunicationHandler.eventEdited?.id?.let {
            eventPresenter.updateEventSiblings(
                it,
                CommunicationHandler.event
            )
        }
    }

    private fun updateEventWithoutRecurrence() {
        if (isAlreadySend) return
        isAlreadySend = true
        CommunicationHandler.eventEdited?.id?.let {
            eventPresenter.updateEvent(
                it,
                CommunicationHandler.event
            )
        }
    }

    private fun showAlertDialogUpdateEventWithRecurrence() {
        val layoutInflater = LayoutInflater.from(requireContext())
        val customDialog: View =
            layoutInflater.inflate(R.layout.new_custom_alert_dialog_cancel_event, null)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(customDialog)
        val alertDialog = builder.create()
        val cancelOneEvent = customDialog.findViewById<RadioButton>(R.id.one_event)
        val cancelAllEvents =
            customDialog.findViewById<RadioButton>(R.id.all_events_recurrent)
        with(customDialog.findViewById<Button>(R.id.yes)) {
            text = getString(R.string.validate)
            setOnClickListener {
                if (cancelOneEvent.isChecked) updateEventWithoutRecurrence()
                if (cancelAllEvents.isChecked) updateEventWithRecurrence()
                alertDialog.dismiss()
                activity?.finish()
            }
        }
        customDialog.findViewById<TextView>(R.id.title).text =
            getString(R.string.event_edit_recurrent_event)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            if (canExitEventCreation)
                requireActivity().finish()
            else {
                Utils.showAlertDialogButtonClicked(
                    requireContext(),
                    getString(R.string.back_create_group_title),
                    getString(R.string.back_create_group_content),
                    getString(R.string.exit)
                ) {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setView() {
        binding.header.title =
            getString(if (CommunicationHandler.eventEdited != null) R.string.edit_event else R.string.new_event)
    }

    private fun handleCreateEventResponse(eventCreated: Events?) {
        if (eventCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            eventPresenter.newEventCreated.value?.id?.let {
                val action =
                    CreateEventFragmentDirections.actionCreateEventFragmentToCreateEventSuccessFragment(
                        it
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun handleNextButtonState() {
        CommunicationHandler.isButtonClickable.observe(
            viewLifecycleOwner,
            ::handleButtonState
        )
    }


    private fun setPreviousClickListener() {
        binding.previous.setOnClickListener {
            CommunicationHandler.resetValues()
            viewPager.previousPage(true)
            if (viewPager.currentItem == 0) {
                binding.previous.visibility = View.INVISIBLE
            }
        }
    }

    private fun handleButtonState(isButtonActive: Boolean) {
        val background = ContextCompat.getDrawable(
            requireContext(),
            if (isButtonActive) R.drawable.new_rounded_button_light_orange else R.drawable.new_bg_rounded_inactive_button_light_orange
        )
        binding.next.background = background
    }


    override fun onDestroy() {
        super.onDestroy()
        CommunicationHandler.resetValues()
        CommunicationHandler.event = CreateEvent()
        CommunicationHandler.canExitEventCreation = true
    }
}