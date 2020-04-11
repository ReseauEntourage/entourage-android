package social.entourage.android.guide.poi

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_guide_poi_read.*
import kotlinx.android.synthetic.main.layout_view_title.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.base.EntourageDialogFragment
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
        presenter.displayPoi(poi)

        title_close_button?.setOnClickListener {dismiss()}
        poi_report_button?.setOnClickListener {onReportButtonClicked()}
    }

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerReadPoiComponent.builder()
                .entourageComponent(entourageComponent)
                .readPoiModule(ReadPoiModule(this))
                .build()
                .inject(this)
    }

    fun onDisplayedPoi(poi: Poi, onAddressClickListener: OnAddressClickListener?, onPhoneClickListener: OnPhoneClickListener?) {
        textview_poi_name?.text = poi.name
        textview_poi_description?.text = poi.description
        setActionButton(button_poi_phone, poi.phone)
        setActionButton(button_poi_mail, poi.email)
        setActionButton(button_poi_web, poi.website)
        setActionButton(button_poi_address, poi.address)
        if (onAddressClickListener != null) {
            button_poi_address?.setOnClickListener(onAddressClickListener)
        }
        if (onPhoneClickListener != null) {
            button_poi_phone?.setOnClickListener(onPhoneClickListener)
        }
        val categoryType = CategoryType.findCategoryTypeById(poi.categoryId)
        poi_type_layout?.setBackgroundColor(categoryType.color)
        poi_type_label?.text = categoryType.displayName
        poi_type_image?.setImageResource(categoryType.resourceTransparentId)
    }

    private fun setActionButton(btn: Button, value: String?) {
        if (value != null && value.isNotEmpty()) {
            btn.visibility = View.VISIBLE
            btn.text = value
            btn.paintFlags = btn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
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
        val emailSubject = getString(R.string.poi_report_email_subject_format, title, poi.id)
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        if (activity!=null && intent.resolveActivity(requireActivity().packageManager) != null) { // Start the intent
            startActivity(intent)
        } else { // No Email clients
            Toast.makeText(context, R.string.error_no_email, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        @JvmField
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