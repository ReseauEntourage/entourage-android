package social.entourage.android.guide.poi

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.fragment_guide_poi_read.*
import kotlinx.android.synthetic.main.fragment_guide_poi_read.guide_filter_list
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.entourage.ShareEntourageFragment
import social.entourage.android.guide.filter.GuideFilterAdapter
import social.entourage.android.guide.poi.PoiRenderer.CategoryType
import social.entourage.android.guide.poi.ReadPoiPresenter.OnPhoneClickListener
import social.entourage.android.map.OnAddressClickListener
import javax.inject.Inject

/**
 * Activity showing the detail of a POI
 */
class ReadPoiFragment : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private lateinit var poi: Poi
    @Inject lateinit var presenter: ReadPoiPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_POI_FROM_MAP)
        return inflater.inflate(R.layout.fragment_guide_poi_read, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        poi = arguments?.getSerializable(BUNDLE_KEY_POI) as Poi
        setupComponent(EntourageApplication.get(activity).entourageComponent)

        //Actually WS return id and not uuid for entourage poi
        val uuid = if (poi.uuid.length == 0) poi.id.toString() else poi.uuid
        presenter.getPoiDetail(uuid)

        title_close_button?.setOnClickListener {dismiss()}
        poi_report_button?.setOnClickListener {onReportButtonClicked()}
        ui_button_share?.setOnClickListener { onShareClicked() }
        ui_button_share?.visibility = View.VISIBLE
        ui_layout_help?.setOnClickListener {
            ui_layout_full_help_info?.visibility = View.VISIBLE
        }
        ui_layout_full_help_info?.visibility = View.GONE
        setupRVHelp()

        ui_bt_share_close?.setOnClickListener {
            ui_layout_share?.visibility = View.GONE
        }

        ui_bt_share_inside?.setOnClickListener {
            ui_layout_share?.visibility = View.GONE

            val fragment = ShareEntourageFragment.newInstance("fuck",poi.id.toInt(),true)
            fragment.show(parentFragmentManager, ShareEntourageFragment.TAG)
        }

        ui_bt_share_outside?.setOnClickListener {
            ui_layout_share?.visibility = View.GONE
            shareOnly()
        }

        ui_button_show_soliguide?.setOnClickListener {
            //TODO: link inside or outside app ?
            poi.soliguideUrl?.let {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(it)
                startActivity(intent)
            }
        }
    }

    fun setupRVHelp() {
        val filterAdapter = GuideFilterAdapter()
        filterAdapter.setHelpOnly()
        guide_filter_list?.adapter = filterAdapter

        ui_layout_full_help_info?.setOnClickListener {
            ui_layout_full_help_info?.visibility = View.GONE
        }

        guide_filter_list?.setOnItemClickListener { parent, view, position, id ->
            ui_layout_full_help_info?.visibility = View.GONE
        }
    }

    private fun setupComponent(entourageComponent: EntourageComponent?) {
        entourageComponent?.let {
            DaggerReadPoiComponent.builder()
                    .entourageComponent(entourageComponent)
                    .readPoiModule(ReadPoiModule(this,entourageComponent.poiRequest))
                    .build()
                    .inject(this)
        } ?: kotlin.run { dismiss() }
    }

    fun noData() {
        dismiss()
    }

    fun onDisplayedPoi(poi: Poi, onAddressClickListener: OnAddressClickListener?, onPhoneClickListener: OnPhoneClickListener?) {
        textview_poi_name?.text = poi.name
        textview_poi_description?.text = poi.description
        setActionButton(button_poi_phone, poi.phone,ui_layout_phone)
        setActionButton(button_poi_mail, poi.email,ui_layout_mail)
        setActionButton(button_poi_web, poi.website,ui_layout_web)
        setActionButton(button_poi_address, poi.address,ui_layout_location)

        if (onAddressClickListener != null) {
            button_poi_address?.setOnClickListener(onAddressClickListener)
        }
        if (onPhoneClickListener != null) {
            button_poi_phone?.setOnClickListener(onPhoneClickListener)
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
            ui_layout_public?.visibility = View.VISIBLE
            ui_tv_poi_public?.text = poi.audience
        }
        else {
            ui_layout_public?.visibility = View.GONE
        }

        //Soliguide
        layout_top_soliguide?.visibility = View.GONE
        ui_layout_soliguide_language?.visibility = View.GONE
        ui_layout_soliguide_openTime?.visibility = View.GONE

        if (poi.isSoliguide) {
            layout_top_soliguide?.visibility = View.VISIBLE
            poi.openTimeTxt?.let {
                ui_layout_soliguide_openTime?.visibility = View.VISIBLE
                ui_tv_poi_open_time?.text = it
            }
            poi.languagesTxt?.let {
                ui_layout_soliguide_language?.visibility = View.VISIBLE
                ui_tv_poi_language?.text = it
            }
            poi_report_layout?.visibility = View.GONE
        }
        else {
            poi_report_layout?.visibility = View.VISIBLE
        }
        this.poi = poi
    }

    private fun getImageId(position:Int) : ImageView? {

        when(position) {
            0 -> {
              return  ui_iv_picto_1
            }
            1 -> {
               return ui_iv_picto_2
            }
            2 -> {
               return ui_iv_picto_3
            }
            3 -> {
                return ui_iv_picto_4
            }
            4 -> {
                return ui_iv_picto_5
            }
            5 -> {
                return ui_iv_picto_6
            }
            else -> return null
        }
    }

    private fun getImageTransId(position:Int) : ImageView? {

        when(position) {
            0 -> {
                return  ui_iv_trans_picto_1
            }
            1 -> {
                return ui_iv_trans_picto_2
            }
            2 -> {
                return ui_iv_trans_picto_3
            }
            3 -> {
                return ui_iv_trans_picto_4
            }
            4 -> {
                return ui_iv_trans_picto_5
            }
            5 -> {
                return ui_iv_trans_picto_6
            }
            else -> return null
        }
    }

    private fun setActionButton(btn: Button?, value: String?,layout:ConstraintLayout?) {
        if (btn !=null && value != null && value.isNotEmpty()) {
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
        val uuid = if (poi.uuid.length == 0) poi.id.toString() else poi.uuid
        val emailSubject = getString(R.string.poi_report_email_subject_format, title, uuid)
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        startActivity(intent)
//        if (activity!=null && intent.resolveActivity(requireActivity().packageManager) != null) { // Start the intent
//            startActivity(intent)
//        } else { // No Email clients
//            Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
//        }
    }

    fun onShareClicked() {
        ui_layout_share?.visibility = View.VISIBLE
    }

    fun shareOnly() {
        EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_SHAREPOI)
        val poiName = if(poi.name == null) "" else poi.name
        val address = if(poi.address?.length ?: 0 == 0) "" else "Adresse: ${poi.address}"
        val phone = if(poi.phone?.length ?: 0 == 0) "" else "Tel: ${poi.phone}"
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
        val TAG = ReadPoiFragment::class.java.simpleName
        const val BUNDLE_KEY_POI = "BUNDLE_KEY_POI"
        // ----------------------------------
        // LIFECYCLE
        // ----------------------------------
        fun newInstance(poi: Poi): ReadPoiFragment {
            val readPoiFragment = ReadPoiFragment()
            val args = Bundle()
            args.putSerializable(BUNDLE_KEY_POI, poi)
            readPoiFragment.arguments = args
            return readPoiFragment
        }
    }
}