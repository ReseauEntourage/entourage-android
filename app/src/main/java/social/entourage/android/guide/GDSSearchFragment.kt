package social.entourage.android.guide

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.PoiResponse
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.FragmentGuideSearchBinding
import social.entourage.android.guide.poi.PoiListFragment
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.tools.hideKeyboard
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.showKeyboard
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber

class GDSSearchFragment : BaseDialogFragment(), PoiListFragment {
    private lateinit var binding: FragmentGuideSearchBinding

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
        binding = FragmentGuideSearchBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uiProgress?.visibility = View.GONE
        binding.uiBtSearchClose?.visibility = View.INVISIBLE

        binding.uiBtBack?.setOnClickListener {
            binding.uiEtSearch?.hideKeyboard()
            dismiss()
        }

        binding.uiBtSearchClose?.setOnClickListener {
            binding.uiEtSearch?.setText("")
            arrayPois.clear()
            rvAdapter?.notifyDataSetChanged()
        }

        binding.uiEtSearch?.setOnEditorActionListener { v, actionId, event ->
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
                    binding.uiBtSearchClose?.visibility = View.VISIBLE
                }
                else {
                    binding.uiBtSearchClose?.visibility = View.INVISIBLE
                }
            }

            override fun afterTextChanged(s: Editable) {}
        }

        binding.uiEtSearch?.addTextChangedListener(textWatcher)

        setupRecyclerView()
        binding.uiEtSearch?.showKeyboard()
    }

    fun setupRecyclerView(){
        rvAdapter = GDSSearchAdapter(arrayPois)
        binding.uiRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.uiRecyclerView?.layoutManager = layoutManager
        binding.uiRecyclerView?.adapter = rvAdapter

    }

    fun sendSearch() {
        if ((binding.uiEtSearch?.text?.length ?: 0) < MIN_CHARS_SEARCH) return
        view?.hideKeyboard()
        binding.uiEtSearch?.clearFocus()

        binding.uiProgress?.visibility = View.VISIBLE
        val poiRequest = EntourageApplication.get().apiModule.poiRequest
        val call = poiRequest.retrievePoisSearch(latitude, longitude, distance, binding.uiEtSearch.text.toString(), "2")
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
                binding.uiProgress?.visibility = View.GONE
            }

            override fun onFailure(call: Call<PoiResponse>, t: Throwable) {
                arrayPois.clear()
                rvAdapter?.notifyDataSetChanged()
                binding.uiProgress?.visibility = View.GONE
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