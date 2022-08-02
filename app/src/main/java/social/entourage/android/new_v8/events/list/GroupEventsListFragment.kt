package social.entourage.android.new_v8.events.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.databinding.NewFragmentGroupEventsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.Utils

class GroupEventsListFragment : Fragment() {
    private var _binding: NewFragmentGroupEventsListBinding? = null
    val binding: NewFragmentGroupEventsListBinding get() = _binding!!

    lateinit var eventsAdapter: GroupEventsListAdapter
    private var sections: MutableList<SectionHeader> = mutableListOf()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

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
        eventsAdapter = GroupEventsListAdapter(requireContext(), sections, null)
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
        sections = Utils.getSectionHeaders(events, sections)
        eventsAdapter.notifyDataChanged(sections)
    }

    private fun setEventsList() {
        binding.events.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }
}