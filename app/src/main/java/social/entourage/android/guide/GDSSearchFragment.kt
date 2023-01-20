package social.entourage.android.guide

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
import kotlinx.android.synthetic.main.fragment_guide_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.PoiResponse
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.guide.poi.PoiListFragment
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.showKeyboard
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber

class GDSSearchFragment : BaseDialogFragment(), PoiListFragment {
    private val MIN_CHARS_SEARCH = 3
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var distance: Double = 0.0

    var arrayPois = ArrayList<Poi>()
    var rvAdapter:GDSSearchAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getDouble(ARG_LAT)
            longitude = it.getDouble(ARG_LONG)
            distance = it.getDouble(ARG_DIST
            )
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_STARTSEARCH)
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
            arrayPois.clear()
            rvAdapter?.notifyDataSetChanged()
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
        rvAdapter = GDSSearchAdapter(arrayPois)
        ui_recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        ui_recyclerView?.layoutManager = layoutManager
        ui_recyclerView?.adapter = rvAdapter

    }

    fun sendSearch() {
        if (ui_et_search?.text?.length ?: 0 < MIN_CHARS_SEARCH) return
        val imm = view?.context?.let { getSystemService(it, InputMethodManager::class.java) }
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        ui_et_search?.clearFocus()

        ui_progress?.visibility = View.VISIBLE
        val poiRequest = EntourageApplication.get().apiModule.poiRequest
        val call = poiRequest.retrievePoisSearch(latitude, longitude, distance, ui_et_search.text.toString(), "2")
        call.enqueue(object : Callback<PoiResponse> {
            override fun onResponse(call: Call<PoiResponse>, response: Response<PoiResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SEARCHRESULTS)
                        arrayPois.clear()
                        arrayPois.addAll(it.pois)
                        rvAdapter?.updateAdapter(arrayPois)
                    }
                }
                ui_progress?.visibility = View.GONE
            }

            override fun onFailure(call: Call<PoiResponse>, t: Throwable) {
                arrayPois.clear()
                rvAdapter?.notifyDataSetChanged()
                ui_progress?.visibility = View.GONE
            }
        })
    }

    override fun showPoiDetails(poi: Poi, isTxtSearch: Boolean) {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_POI)
        try {
            poi.partner_id?.let { partner_id ->
                PartnerFragment.newInstance(partner_id).show(parentFragmentManager, PartnerFragment.TAG)
            } ?: run {
                ReadPoiFragment.newInstance(poi,"").show(parentFragmentManager, ReadPoiFragment.TAG)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    companion object {
        private const val ARG_LAT = "latitude"
        private const val ARG_LONG = "longitude"
        private const val ARG_DIST = "distance"

        val TAG: String? = GDSSearchFragment::class.java.simpleName
        fun newInstance(latitude: Double, longitude: Double, distance: Double): GDSSearchFragment {
            val fragment = GDSSearchFragment()
            val args = Bundle()
            args.putDouble(ARG_LAT, latitude)
            args.putDouble(ARG_LONG, longitude)
            args.putDouble(ARG_DIST, distance)

            fragment.arguments = args
            return fragment
        }
    }
}