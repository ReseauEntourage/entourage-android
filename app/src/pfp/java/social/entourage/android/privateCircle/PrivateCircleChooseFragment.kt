package social.entourage.android.privateCircle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Newsfeed.NewsfeedWrapper
import social.entourage.android.api.model.map.BaseEntourage
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.base.EntouragePagination
import social.entourage.android.entourage.my.filter.MyEntouragesFilterFactory
import social.entourage.android.privateCircle.PrivateCircleDateFragment.Companion.newInstance
import java.util.*

/**
 * A [EntourageDialogFragment] subclass that shows a list of private circles with the capability of choosing one
 */
class PrivateCircleChooseFragment  // ----------------------------------
// LIFECYCLE
// ----------------------------------
    : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @BindView(R.id.privatecircle_choose_list)
    var privateCircleRecyclerView: RecyclerView? = null

    @JvmField
    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null
    var adapter: PrivateCircleChooseAdapter? = null
    var pagination = EntouragePagination(Constants.ITEMS_PER_PAGE)
    private val scrollListener = OnScrollListener()
    override fun onResume() {
        super.onResume()
        privateCircleRecyclerView!!.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        privateCircleRecyclerView!!.removeOnScrollListener(scrollListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_privatecircle_choose, container, false)
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    private fun configureView() {
        //adapter
        if (adapter == null) {
            privateCircleRecyclerView!!.layoutManager = LinearLayoutManager(context)
            adapter = PrivateCircleChooseAdapter()
            privateCircleRecyclerView!!.adapter = adapter
        }
        //get the list of neighborhoods
        neighborhoods
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------
    @OnClick(R.id.title_close_button)
    fun onCloseButtonClicked() {
        dismiss()
    }

    @OnClick(R.id.title_action_button)
    fun onNextButtonClicked() {
        if (adapter!!.selectedPrivateCircle == AdapterView.INVALID_POSITION) {
            Toast.makeText(context, R.string.privatecircle_choose_error, Toast.LENGTH_SHORT).show()
            return
        }
        val entourage = adapter!!.getItemAt(adapter!!.selectedPrivateCircle)
        if (entourage != null) {
            val privateCircleDateFragment = newInstance(entourage.id)
            privateCircleDateFragment.show(parentFragmentManager, PrivateCircleDateFragment.TAG)
        }
    }

    // ----------------------------------
    // API Calls
    // ----------------------------------
    private val neighborhoods: Unit
        get() {
            val newsfeedRequest = EntourageApplication.get().entourageComponent.newsfeedRequest
            val filter = MyEntouragesFilterFactory.getMyEntouragesFilter(context)
            val call = newsfeedRequest.retrieveMyFeeds(
                    pagination.page,
                    pagination.itemsPerPage,
                    filter.entourageTypes,
                    filter.tourTypes,
                    filter.status,
                    filter.isShowOwnEntouragesOnly,
                    filter.isShowPartnerEntourages,
                    filter.isShowJoinedEntourages
            )
            progressBar!!.visibility = View.VISIBLE
            call.enqueue(object : Callback<NewsfeedWrapper> {
                override fun onResponse(call: Call<NewsfeedWrapper>, response: Response<NewsfeedWrapper>) {
                    if (response.isSuccessful) {
                        val entourageList: MutableList<Entourage> = ArrayList()
                        val newsfeedList = response.body()!!.newsfeed
                        if (newsfeedList != null) {
                            for (newsfeed in newsfeedList) {
                                val feedData = newsfeed.data
                                if (feedData == null || feedData !is Entourage) {
                                    continue
                                }
                                val entourage = newsfeed.data as Entourage
                                if (BaseEntourage.TYPE_PRIVATE_CIRCLE.equals(entourage.groupType, ignoreCase = true)) {
                                    entourageList.add(entourage)
                                }
                            }
                        }
                        adapter!!.addPrivateCircleList(entourageList)
                        pagination.loadedItems(newsfeedList!!.size)
                    }
                    progressBar!!.visibility = View.GONE
                }

                override fun onFailure(call: Call<NewsfeedWrapper>, t: Throwable) {
                    progressBar!!.visibility = View.GONE
                }
            })
        }

    // ----------------------------------
    // PRIVATE CLASSES
    // ----------------------------------
    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0) {
                // Scrolling down
                val visibleItemCount = recyclerView.childCount
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstVisibleItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                val totalItemCount = linearLayoutManager.itemCount
                if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                    neighborhoods
                }
            } else {
                // Scrolling up
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG = PrivateCircleChooseFragment::class.java.simpleName
    }
}