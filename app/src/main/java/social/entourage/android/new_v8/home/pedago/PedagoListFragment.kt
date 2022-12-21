package social.entourage.android.new_v8.home.pedago

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.databinding.NewFragmentPedagoListBinding
import social.entourage.android.new_v8.home.HomePresenter
import social.entourage.android.new_v8.models.Category
import social.entourage.android.new_v8.models.Pedago

class PedagoListFragment : Fragment() {

    private var _binding: NewFragmentPedagoListBinding? = null
    val binding: NewFragmentPedagoListBinding get() = _binding!!

    lateinit var pedagoAdapter: PedagoListAdapter
    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    private val childListUnderstand: MutableList<Pedago> = mutableListOf()
    private val childListAct: MutableList<Pedago> = mutableListOf()
    private val childListInspire: MutableList<Pedago> = mutableListOf()

    private val sections: MutableList<SectionHeader> = mutableListOf()

    private var selectedFilter: Category = Category.ALL
    private var selectedFilterPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentPedagoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pedagoAdapter = PedagoListAdapter(requireContext(), sections, object : OnItemClick {
            override fun onItemClick(pedagogicalContent: Pedago) {
                if (pedagogicalContent.html != null && pedagogicalContent.id != null) {
                    val action =
                        PedagoListFragmentDirections.actionPedagogicalListFragmentToPedagogicalDetailsFragment(
                            pedagogicalContent.html, pedagogicalContent.id,false
                        )
                    findNavController().navigate(action)
                }
            }
        })
        handleBackButton()
        setFilters()
        setPedagoList()
        homePresenter.getPedagogicalResources()
        homePresenter.pedagogicalContent.observe(
            viewLifecycleOwner,
            ::handleGetPedagogicalResourcesResponse
        )
    }

    private fun handleGetPedagogicalResourcesResponse(pedagogicalResources: MutableList<Pedago>) {
        childListAct.clear()
        childListUnderstand.clear()
        childListInspire.clear()
        sections.clear()
        pedagogicalResources.forEach {
            when (it.category) {
                Category.ACT -> childListAct.add(it)
                Category.UNDERSTAND -> childListUnderstand.add(it)
                Category.INSPIRE -> childListInspire.add(it)
                else -> {}
            }
        }
        sections.add(SectionHeader(childListUnderstand, getString(Category.UNDERSTAND.id)))
        sections.add(SectionHeader(childListAct, getString(Category.ACT.id)))
        sections.add(SectionHeader(childListInspire, getString(Category.INSPIRE.id)))
        applyFilter(selectedFilter)
        pedagoAdapter.notifyDataChanged(sections)
    }

    private fun applyFilter(filter: Category) {
        selectedFilter = filter
        clearFilter()
        when (filter) {
            Category.ALL -> {
                sections.add(
                    SectionHeader(
                        childListUnderstand,
                        getString(Category.UNDERSTAND.id)
                    )
                )
                sections.add(SectionHeader(childListAct, getString(Category.ACT.id)))
                sections.add(
                    SectionHeader(
                        childListInspire,
                        getString(Category.INSPIRE.id)
                    )
                )
            }
            Category.ACT -> {
                sections.add(SectionHeader(childListAct, getString(Category.ACT.id)))
            }
            Category.UNDERSTAND -> {
                sections.add(
                    SectionHeader(
                        childListUnderstand,
                        getString(Category.UNDERSTAND.id)
                    )
                )
            }
            Category.INSPIRE -> {
                sections.add(
                    SectionHeader(
                        childListInspire,
                        getString(Category.INSPIRE.id)
                    )
                )

            }
        }
        pedagoAdapter.notifyDataChanged(sections)
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setFilters() {
        val filterList: MutableList<Category> =
            mutableListOf(
                Category.ALL,
                Category.UNDERSTAND,
                Category.ACT,
                Category.INSPIRE,
            )
        binding.filters.apply {
            layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = FilterAdapter(filterList, object : OnItemClickListener {
                override fun onItemClick(filter: Category, position: Int) {
                    applyFilter(filter)
                    selectedFilterPosition = position
                }
            }, selectedFilterPosition)
        }
    }

    private fun clearFilter() {
        sections.clear()
    }

    private fun setPedagoList() {
        binding.content.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pedagoAdapter
        }
    }
}