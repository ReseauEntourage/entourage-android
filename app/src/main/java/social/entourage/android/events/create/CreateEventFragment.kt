package social.entourage.android.events.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentCreateEventBinding
import social.entourage.android.RefreshController
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.create.CommunicationHandler.canExitEventCreation
import social.entourage.android.api.model.Events
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.*

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


        //buttonYes.background = requireContext().getDrawable(R.drawable.btn_shape_orange_alert_dialog)

        with(customDialog.findViewById<Button>(R.id.yes)) {
           this.background = requireContext().getDrawable(R.drawable.btn_shape_light_orange)

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
                CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    getString(R.string.back_create_group_title),
                    getString(R.string.back_create_event_content),
                    getString(R.string.exit)
                ) {
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setView() {
        // Check if the current layout direction is RTL
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        val originalDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.new_profile_header_orange)

        if (isRTL && originalDrawable != null) {
            // Create a mirrored version of the drawable
            val mirroredDrawable = mirrorDrawable(originalDrawable)
            binding.createEventLayout.background = mirroredDrawable
        } else {
            // Use the default background for LTR languages
            binding.createEventLayout.background = originalDrawable
        }

        binding.header.title =
            getString(if (CommunicationHandler.eventEdited != null) R.string.edit_event else R.string.new_event)
    }

    private fun handleCreateEventResponse(eventCreated: Events?) {
        if (eventCreated == null) {
            isAlreadySend = false
            Utils.showToast(requireContext(), getString(R.string.error_create_group))
        } else {
            eventPresenter.newEventCreated.value?.id?.let {
                if (CommunicationHandler.eventEdited == null) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_end)
                }
                val action =
                    CreateEventFragmentDirections.actionCreateEventFragmentToCreateEventSuccessFragment(
                        it
                    )
                findNavController().navigate(action)
            }
        }
    }
    private fun mirrorDrawable(drawable: Drawable): Drawable {
        val matrix = Matrix().apply {
            preScale(-1f, 1f)
        }

        val mirroredBitmap = drawableToBitmap(drawable).let {
            Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
        }

        return BitmapDrawable(resources, mirroredBitmap)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
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