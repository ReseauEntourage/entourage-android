package social.entourage.android.newsfeed.v2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_guide_search.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.log.AnalyticsEvents.ACTION_FEEDSEARCH_START_ASK
import social.entourage.android.tools.log.AnalyticsEvents.ACTION_FEEDSEARCH_START_CONTRIB
import social.entourage.android.tools.log.AnalyticsEvents.ACTION_FEEDSEARCH_START_EVENT
import social.entourage.android.tools.showKeyboard
import timber.log.Timber

private const val ARG_SEARCH = "searchType"

class EntouragesSearchFragment : BaseDialogFragment() {
    private val MIN_CHARS_SEARCH = 3

    var arrayItems = ArrayList<NewsfeedItem>()
    var rvAdapter: EntouragesSearchAdapter? = null
    var searchType:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchType = it.getString(ARG_SEARCH)
        }

        var tag = ""
        when(searchType) {
            "ask" -> tag = ACTION_FEEDSEARCH_START_ASK
            "contrib" -> tag = ACTION_FEEDSEARCH_START_CONTRIB
            "outing" -> tag = ACTION_FEEDSEARCH_START_EVENT
        }
        AnalyticsEvents.logEvent(tag)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_guide_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_progress?.visibility = View.GONE
        ui_bt_search_close?.visibility = View.INVISIBLE

        ui_bt_back?.setOnClickListener {
            ui_et_search?.hideKeyboard()
            dismiss()
        }

        ui_bt_search_close?.setOnClickListener {
            ui_et_search?.setText("")
            arrayItems.clear()
            rvAdapter?.updateAdapter(arrayItems)
        }

        ui_et_search?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                sendSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    ui_bt_search_close?.visibility = View.VISIBLE
                }
                else {
                    ui_bt_search_close?.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }

        ui_et_search?.addTextChangedListener(textWatcher)

        setupRecyclerView()
        ui_et_search?.showKeyboard()
    }

    fun setupRecyclerView(){
        rvAdapter = EntouragesSearchAdapter(arrayItems) { position ->
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEEDSEARCH_SHOW_DETAIL)
            val item = arrayItems[position]
            if (item.data is FeedItem) {
                openFeedItem(item.data as FeedItem, position)
            }
        }
        ui_recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        ui_recyclerView?.layoutManager = layoutManager
        ui_recyclerView?.adapter = rvAdapter
    }

    fun openFeedItem(feedItem: FeedItem,position:Int) {
        try {
            val fragmentManager = requireActivity().supportFragmentManager
            FeedItemInformationFragment.newInstance(feedItem, 0, position).show(fragmentManager, FeedItemInformationFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun sendSearch() {
        if (ui_et_search?.text?.length ?: 0 < MIN_CHARS_SEARCH) return
        val imm = view?.context?.let { getSystemService(it, InputMethodManager::class.java) }
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        ui_et_search?.clearFocus()

        ui_progress?.visibility = View.VISIBLE
        val request = EntourageApplication.get().components.entourageRequest

        val userAddress = EntourageApplication.me(requireContext())?.address

        val types: String
        when(searchType) {
            "outing" -> types = "ou"
            "ask" -> types = "as,ae,am,ar,ai,ak,ao,ah"
            "contrib" -> types = "cs,ce,cm,cr,ci,ck,co,ch"
            else -> types = "as,ae,am,ar,ai,ak,ao,ah"
        }

        val call = request.searchEntourages(userAddress?.latitude, userAddress?.longitude,types, ui_et_search.text.toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDSEARCH_SEARCHRESULTS)
                response.body()?.let {
                    if (response.isSuccessful) {
                        arrayItems.clear()

                        val rootJson = JSONObject(it.string())
                        val jsonArray = rootJson.getJSONArray("entourages")
                        for (i in 0 until jsonArray.length()) {
                            (jsonArray[i] as? JSONObject)?.let { jsonObject ->
                                arrayItems.add(getFeedItem(jsonObject))
                            }
                        }
                        rvAdapter?.updateAdapter(arrayItems)
                    }
                }
                ui_progress?.visibility = View.GONE
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                arrayItems.clear()
                rvAdapter?.updateAdapter(arrayItems)
                ui_progress?.visibility = View.GONE
            }
        })
    }

    private fun getFeedItem(jsonObject: JSONObject) : NewsfeedItem {
        val newsfeed = NewsfeedItem()
        newsfeed.type = BaseEntourage.NEWSFEED_TYPE

        try {
            NewsfeedItem.getClassFromString(BaseEntourage.NEWSFEED_TYPE,
                    jsonObject["group_type"].toString(),
                    jsonObject["entourage_type"].toString())?.let { newsfeedClass ->
                val gson = GsonBuilder()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                        .create()
                newsfeed.data = gson.fromJson<Any>(jsonObject.toString(), newsfeedClass)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return newsfeed
    }

    companion object {
        val TAG: String? = EntouragesSearchFragment::class.java.simpleName
        fun newInstance(searchType:String): EntouragesSearchFragment {
            val fragment = EntouragesSearchFragment()
            val args = Bundle()
            args.putString(ARG_SEARCH,searchType)
            fragment.arguments = args
            return fragment
        }
    }
}