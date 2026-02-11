package social.entourage.android.guide.poi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.base.map.OnAddressClickListener
import social.entourage.android.databinding.FragmentGuidePoiReadBinding
import social.entourage.android.guide.filter.GuideFilterItemAdapter
import social.entourage.android.guide.poi.PoiRenderer.CategoryType
import social.entourage.android.guide.poi.ReadPoiPresenter.OnPhoneClickListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.ShareMessageFragment

/**
 * Activity showing the detail of a POI
 */
class ReadPoiFragment : BaseDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var _binding: FragmentGuidePoiReadBinding? = null
    val binding: FragmentGuidePoiReadBinding get() = _binding!!

    private lateinit var poi: Poi
    private var filtersSelectedFromMap:String? = null
    var presenter: ReadPoiPresenter = ReadPoiPresenter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_OPEN_POI_FROM_MAP)
        _binding = FragmentGuidePoiReadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        poi = arguments?.getSerializable(BUNDLE_KEY_POI) as Poi
        filtersSelectedFromMap = arguments?.getString(BUNDLE_KEY_SEARCH,"")

        //Actually WS return id and not uuid for entourage poi
        presenter.getPoiDetail(poi.uuid)

        binding.poiReadToolbar.binding.titleCloseButton.setOnClickListener {dismiss()}
        binding.poiReportButton.setOnClickListener {onReportButtonClicked()}
        binding.poiReadToolbar.binding.uiButtonShare.setOnClickListener { onShareClicked() }
        binding.poiReadToolbar.binding.uiButtonShare.visibility = View.VISIBLE
        binding.uiLayoutHelp.setOnClickListener {
            binding.uiLayoutFullHelpInfo.visibility = View.VISIBLE
        }
        binding.uiLayoutFullHelpInfo.visibility = View.GONE
        setupRVHelp()

        binding.uiBtShareClose.setOnClickListener {
            binding.uiLayoutShare.visibility = View.GONE
        }

        binding.uiBtShareInside.setOnClickListener {
            binding.uiLayoutShare.visibility = View.GONE

            ShareMessageFragment.newInstanceForPoi(poi.uuid)
                    .show(parentFragmentManager, ShareMessageFragment.TAG)
        }

        binding.uiBtShareOutside.setOnClickListener {
            binding.uiLayoutShare.visibility = View.GONE
            shareOnly()
        }

        binding.uiButtonShowSoliguide.setOnClickListener {
            //TODO: link inside or outside app ?
            poi.soliguideUrl?.let {
                val stringTag = String.format(AnalyticsEvents.SOLIGUIDE_CLICK,poi.soliguideId,poi.uuid,filtersSelectedFromMap)
                AnalyticsEvents.logEvent(stringTag)

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    private fun setupRVHelp() {
        val filterAdapter = GuideFilterItemAdapter(requireContext())
        filterAdapter.setHelpOnly()
        binding.guideFilterList.adapter = filterAdapter

        binding.uiLayoutFullHelpInfo.setOnClickListener {
            binding.uiLayoutFullHelpInfo.visibility = View.GONE
        }

        binding.guideFilterList.setOnItemClickListener { _, _, _, _ ->
            binding.uiLayoutFullHelpInfo.visibility = View.GONE
        }
    }

    fun noData() {
        dismissAllowingStateLoss()
    }

    fun onDisplayedPoi(poi: Poi, onAddressClickListener: OnAddressClickListener?, onPhoneClickListener: OnPhoneClickListener?) {
        binding.textviewPoiName.text = poi.name
        binding.textviewPoiDescription.text = poi.description
        setActionButton(binding.buttonPoiPhone, poi.phone,binding.uiLayoutPhone)
        setActionButton(binding.buttonPoiMail, poi.email,binding.uiLayoutMail)
        setActionButton(binding.buttonPoiWeb, poi.website,binding.uiLayoutWeb)
        setActionButton(binding.buttonPoiAddress, poi.address,binding.uiLayoutLocation)

        if (onAddressClickListener != null) {
            binding.buttonPoiAddress.setOnClickListener(onAddressClickListener)
        }
        if (onPhoneClickListener != null) {
            binding.buttonPoiPhone.setOnClickListener(onPhoneClickListener)
        }
        //Setup icons categories
        for (i in 0 until 6) {
            getImageId(i)?.visibility = View.INVISIBLE
            getImageTransId(i)?.visibility = View.GONE
        }

        for (i in 0 until poi.categories.size) {
            val catType = CategoryType.findCategoryTypeById(poi.categories[i])
            val pictoPoi = getImageId(i)
            pictoPoi?.visibility = View.VISIBLE
            pictoPoi?.setImageResource(catType.filterId)
            getImageTransId(i)?.visibility = View.INVISIBLE
        }

        if (!poi.audience.isNullOrEmpty()) {
            binding.uiLayoutPublic.visibility = View.VISIBLE
            binding.uiTvPoiPublic.text = poi.audience
        }
        else {
            binding.uiLayoutPublic.visibility = View.GONE
        }

        //Soliguide
        binding.layoutTopSoliguide.visibility = View.GONE
        binding.uiLayoutSoliguideLanguage.visibility = View.GONE
        binding.uiLayoutSoliguideOpenTime.visibility = View.GONE

        if (poi.isSoliguide) {
            binding.layoutTopSoliguide.visibility = View.VISIBLE
            poi.openTimeTxt?.let {
                binding.uiLayoutSoliguideOpenTime.visibility = View.VISIBLE
                binding.uiTvPoiOpenTime.text = it
            }
            poi.languagesTxt?.let {
                binding.uiLayoutSoliguideLanguage.visibility = View.VISIBLE
                binding.uiTvPoiLanguage.text = it
            }
            binding.poiReportLayout.visibility = View.GONE

            val stringTag = String.format(AnalyticsEvents.SOLIGUIDE_SHOW_POI,poi.soliguideId,poi.uuid,filtersSelectedFromMap)
            AnalyticsEvents.logEvent(stringTag)
        }
        else {
            binding.poiReportLayout.visibility = View.VISIBLE
        }
        this.poi = poi
    }

    private fun getImageId(position:Int) : ImageView? {

        when(position) {
            0 -> {
              return  binding.uiIvPicto1
            }
            1 -> {
               return binding.uiIvPicto2
            }
            2 -> {
               return binding.uiIvPicto3
            }
            3 -> {
                return binding.uiIvPicto4
            }
            4 -> {
                return binding.uiIvPicto5
            }
            5 -> {
                return binding.uiIvPicto6
            }
            else -> return null
        }
    }

    private fun getImageTransId(position:Int) : ImageView? {

        when(position) {
            0 -> {
                return  binding.uiIvTransPicto1
            }
            1 -> {
                return binding.uiIvTransPicto2
            }
            2 -> {
                return binding.uiIvTransPicto3
            }
            3 -> {
                return binding.uiIvTransPicto4
            }
            4 -> {
                return binding.uiIvTransPicto5
            }
            5 -> {
                return binding.uiIvTransPicto6
            }
            else -> return null
        }
    }

    private fun setActionButton(btn: Button?, value: String?,layout:ConstraintLayout?) {
        if (btn !=null && !value.isNullOrEmpty()) {
            btn.visibility = View.VISIBLE
            btn.text = value
            btn.paintFlags = btn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            layout?.visibility = View.VISIBLE
        }
        else {
            layout?.visibility = View.GONE
        }
    }

    private fun onReportButtonClicked() {
        // Build the email intent
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        // Set the email to
        val addresses = arrayOf(getString(R.string.contact_email))
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        // Set the subject
        val title = poi.name ?:""
        val uuid = poi.uuid
        val emailSubject = getString(R.string.poi_report_email_subject_format, title, uuid)
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException){
            Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onShareClicked() {
        binding.uiLayoutShare.visibility = View.VISIBLE
    }

    private fun shareOnly() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SHAREPOI)
        val poiName = if(poi.name == null) "" else poi.name
        val address = if((poi.address?.length ?: 0) == 0) "" else "Adresse: ${poi.address}"
        val phone = if((poi.phone?.length ?: 0) == 0) "" else "Tel: ${poi.phone}"
        val urlShare = getString(R.string.url_share_entourage_bitly)

        val shareText = getString(R.string.info_share_poi_sms).format(poiName,address,phone,urlShare)
        // start the share intent
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.entourage_share_intent_title)))
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        val TAG: String = ReadPoiFragment::class.java.simpleName
        const val BUNDLE_KEY_POI = "BUNDLE_KEY_POI"
        const val BUNDLE_KEY_SEARCH = "BUNDLE_KEY_SEARCH"
        // ----------------------------------
        // LIFECYCLE
        // ----------------------------------
        fun newInstance(poi: Poi,filtersSelectedFromMap:String): ReadPoiFragment {
            val readPoiFragment = ReadPoiFragment()
            val args = Bundle()
            args.putSerializable(BUNDLE_KEY_POI, poi)
            args.putString(BUNDLE_KEY_SEARCH,filtersSelectedFromMap)
            readPoiFragment.arguments = args
            return readPoiFragment
        }
    }
}