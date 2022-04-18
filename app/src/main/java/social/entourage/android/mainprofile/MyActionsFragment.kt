package social.entourage.android.mainprofile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_my_actions.*
import kotlinx.android.synthetic.main.fragment_my_actions.ui_progress
import kotlinx.android.synthetic.main.fragment_my_actions.ui_recyclerView
import kotlinx.android.synthetic.main.layout_view_title.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageContribution
import social.entourage.android.api.model.EntourageDemand
import social.entourage.android.api.model.EntourageEvent
import social.entourage.android.api.request.EntouragesResponse
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber


class MyActionsFragment : BaseDialogFragment() {
    private var adapter:MyActionsAdapter? = null
    private var arrayItems = ArrayList<BaseEntourage>()
    private var arrayItemsSelected = ArrayList<BaseEntourage>()
    private var indexSelected = 0
    private var isAlreadyShow = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ui_progress?.visibility = View.GONE

        if (indexSelected == 1) {
            ui_tv_title.text = getString(R.string.myAsk)
        }
        else {
            ui_tv_title.text = getString(R.string.myContrib)
        }

        title_close_button?.setOnClickListener {
            dismiss()
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_LISTACTIONS_SHOW)
        setupRecyclerView()

        getMyActions()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EntBus.register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EntBus.unregister(this)
    }

    fun onRefreshActions() {
        isAlreadyShow = false
        getMyActions()
    }

    fun setupRecyclerView() {
        val isContrib = indexSelected == 0
        adapter = MyActionsAdapter(arrayItemsSelected, isContrib,true) { position ->
            if (isAlreadyShow) return@MyActionsAdapter
            val feed = arrayItemsSelected[position]
            try {
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_LISTACTIONS_SHOW_DETAIL)
                val fragmentManager = requireActivity().supportFragmentManager
                FeedItemInformationFragment.newInstance(feed, 0, position,true).show(fragmentManager, FeedItemInformationFragment.TAG)
                isAlreadyShow = true
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
        ui_recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        ui_recyclerView?.layoutManager = layoutManager
        ui_recyclerView?.adapter = adapter
    }

    fun changeSelectedItems() {
        arrayItemsSelected.clear()

        for (_feed in arrayItems) {
            if (_feed !is EntourageEvent) {
                if (indexSelected == 0 &&_feed is EntourageContribution) {
                    arrayItemsSelected.add(_feed)
                }
                else if (indexSelected == 1 && _feed is EntourageDemand) {
                    arrayItemsSelected.add(_feed)
                }
            }
        }
        val isContrib = indexSelected == 0
        adapter?.updateAdapter(arrayItemsSelected,isContrib,false)

        ui_recyclerView?.layoutManager?.scrollToPosition(0)
    }

    fun getMyActions() {
        val request = EntourageApplication.get().apiModule.entourageRequest
        val call = request.getMyActions()
        ui_progress?.visibility = View.VISIBLE
        call.enqueue(object : Callback<EntouragesResponse> {
            override fun onResponse(call: Call<EntouragesResponse>, response: Response<EntouragesResponse>) {
                AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDSEARCH_SEARCHRESULTS)
                response.body()?.let {
                    if (response.isSuccessful) {
                        arrayItems.clear()
                        response.body()?.entourages?.let { arrayItems.addAll(it) }
                        changeSelectedItems()
                    }
                }
                ui_progress?.visibility = View.GONE
            }

            override fun onFailure(call: Call<EntouragesResponse>, t: Throwable) {
                arrayItems.clear()
                ui_progress?.visibility = View.GONE
            }
        })
    }

    companion object {
        val TAG: String? = MyActionsFragment::class.java.simpleName

        fun newInstance(isContrib:Boolean): MyActionsFragment {
            val _intent = MyActionsFragment()
            _intent.indexSelected = if (isContrib) 0 else 1
            return _intent
        }
    }
}