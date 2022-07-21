package social.entourage.android.new_v8.events.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentGroupEventsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Events
import java.util.*

class GroupEventsListFragment : Fragment() {
    private var _binding: NewFragmentGroupEventsListBinding? = null
    val binding: NewFragmentGroupEventsListBinding get() = _binding!!

    lateinit var eventsAdapter: GroupEventsListAdapter
    private val sections: MutableList<SectionHeader> = mutableListOf()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private val childListJanuary: MutableList<Events> = mutableListOf()
    private val childListFebruary: MutableList<Events> = mutableListOf()
    private val childListMarch: MutableList<Events> = mutableListOf()
    private val childListApril: MutableList<Events> = mutableListOf()
    private val childListMai: MutableList<Events> = mutableListOf()
    private val childListJune: MutableList<Events> = mutableListOf()
    private val childListJuly: MutableList<Events> = mutableListOf()
    private val childListAugust: MutableList<Events> = mutableListOf()
    private val childListSeptember: MutableList<Events> = mutableListOf()
    private val childListOctober: MutableList<Events> = mutableListOf()
    private val childListNovember: MutableList<Events> = mutableListOf()
    private val childListDecember: MutableList<Events> = mutableListOf()

    private val args: GroupEventsListFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setView()
        eventsAdapter = GroupEventsListAdapter(requireContext(), sections)
        setEventsList()
        groupPresenter.getGroupEvents(args.groupID)
        groupPresenter.getAllEvents.observe(
            viewLifecycleOwner,
            ::handleGetEventsResponse
        )
        setBackButton()

    }

    private fun setView() {
        binding.header.subtitle.text = args.groupName
    }

    private fun setBackButton() {
        binding.header.iconBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleGetEventsResponse(events: MutableList<Events>) {
        events.forEach {
            it.metadata?.startsAt?.let { date ->
                val cal = Calendar.getInstance()
                cal.time = date
                when (cal.get(Calendar.MONTH)) {
                    0 -> childListJanuary.add(it)
                    1 -> childListFebruary.add(it)
                    2 -> childListMarch.add(it)
                    3 -> childListApril.add(it)
                    4 -> childListMai.add(it)
                    5 -> childListJune.add(it)
                    6 -> childListJuly.add(it)
                    7 -> childListAugust.add(it)
                    8 -> childListSeptember.add(it)
                    9 -> childListOctober.add(it)
                    10 -> childListNovember.add(it)
                    11 -> childListDecember.add(it)
                }
            }
        }
        if (childListJanuary.isNotEmpty()) sections.add(
            SectionHeader(
                childListJanuary,
                getString(R.string.january)
            )
        )
        if (childListFebruary.isNotEmpty()) sections.add(
            SectionHeader(
                childListFebruary,
                getString(R.string.february)
            )
        )
        if (childListMarch.isNotEmpty()) sections.add(
            SectionHeader(
                childListMarch,
                getString(R.string.march)
            )
        )
        if (childListApril.isNotEmpty()) sections.add(
            SectionHeader(
                childListApril,
                getString(R.string.april)
            )
        )
        if (childListMai.isNotEmpty()) sections.add(
            SectionHeader(
                childListMai,
                getString(R.string.may)
            )
        )
        if (childListJune.isNotEmpty()) sections.add(
            SectionHeader(
                childListJune,
                getString(R.string.june)
            )
        )
        if (childListJuly.isNotEmpty()) sections.add(
            SectionHeader(
                childListJuly,
                getString(R.string.july)
            )
        )
        if (childListAugust.isNotEmpty()) sections.add(
            SectionHeader(
                childListAugust,
                getString(R.string.august)
            )
        )
        if (childListSeptember.isNotEmpty()) sections.add(
            SectionHeader(
                childListSeptember,
                getString(R.string.september)
            )
        )
        if (childListOctober.isNotEmpty()) sections.add(
            SectionHeader(
                childListOctober,
                getString(R.string.october)
            )
        )
        if (childListNovember.isNotEmpty()) sections.add(
            SectionHeader(
                childListNovember,
                getString(R.string.november)
            )
        )
        if (childListDecember.isNotEmpty()) sections.add(
            SectionHeader(
                childListDecember,
                getString(R.string.december)
            )
        )
        eventsAdapter.notifyDataChanged(sections)
    }

    private fun setEventsList() {
        binding.events.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }
}