package social.entourage.android.new_v8.home.pedago

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityPedagoListBinding
import social.entourage.android.new_v8.home.HomePresenter
import social.entourage.android.new_v8.models.Category
import social.entourage.android.new_v8.models.Pedago

class PedagoListActivity : AppCompatActivity() {
    lateinit var binding: NewActivityPedagoListBinding
    lateinit var pedagoAdapter: PedagoListAdapter
    private val homePresenter: HomePresenter by lazy { HomePresenter() }

    private val childListUnderstand: MutableList<Pedago> = mutableListOf()
    private val childListAct: MutableList<Pedago> = mutableListOf()
    private val childListInspire: MutableList<Pedago> = mutableListOf()

    private val sections: MutableList<SectionHeader> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_pedago_list
        )
        pedagoAdapter = PedagoListAdapter(this, sections)
        handleBackButton()
        setFilters()
        setPedagoList()
        homePresenter.getPedagogicalResources()
        homePresenter.pedagogicalContent.observe(this, ::handleGetPedagogicalResourcesResponse)

    }

    private fun handleGetPedagogicalResourcesResponse(pedagogicalResources: MutableList<Pedago>) {
        pedagogicalResources.forEach {
            when (it.category) {
                Category.ACT -> childListAct.add(it)
                Category.UNDERSTAND -> childListUnderstand.add(it)
                Category.INSPIRE -> childListInspire.add(it)
            }
        }
        sections.add(SectionHeader(childListUnderstand, getString(Category.UNDERSTAND.id)))
        sections.add(SectionHeader(childListAct, getString(Category.ACT.id)))
        sections.add(SectionHeader(childListInspire, getString(Category.INSPIRE.id)))
        pedagoAdapter.notifyDataChanged(sections)
    }

    private fun handleBackButton() {
        binding.header.iconBack.setOnClickListener {
            finish()
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
                override fun onItemClick(filter: Category) {
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
            })
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