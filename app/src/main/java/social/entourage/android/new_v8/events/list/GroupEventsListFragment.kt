package social.entourage.android.new_v8.events.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.databinding.NewFragmentGroupEventsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Category
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.Pedago

class GroupEventsListFragment : Fragment() {
    private var _binding: NewFragmentGroupEventsListBinding? = null
    val binding: NewFragmentGroupEventsListBinding get() = _binding!!

    lateinit var eventsAdapter: GroupEventsListAdapter
    private val sections: MutableList<SectionHeader> = mutableListOf()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    private val childListJanuary: MutableList<Events> = mutableListOf()
    private val childListMai: MutableList<Events> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventsAdapter = GroupEventsListAdapter(requireContext(), sections)
        setEventsList()
        groupPresenter.getAllEvents.observe(
            viewLifecycleOwner,
            ::handleGetEventsResponse
        )

    }

    private fun handleGetEventsResponse(events: MutableList<Events>) {
        events.forEach {
            childListJanuary.add(it)
            childListMai.add(it)
        }
        sections.add(SectionHeader(childListJanuary, "Janvier"))
        sections.add(SectionHeader(childListMai, "Mai"))
        eventsAdapter.notifyDataChanged(sections)
    }

    private fun setEventsList() {
        binding.events.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }
}