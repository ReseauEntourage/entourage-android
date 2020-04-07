package social.entourage.android.partner

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.CropCircleTransformation
import timber.log.Timber
import kotlinx.android.synthetic.main.fragment_partner.*
import kotlinx.android.synthetic.main.layout_view_title.view.*

/**
 * Fragment that displays the details of a partner organisation
 */
class PartnerFragment : EntourageDialogFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var partnerId: Long = 0
    private var partner: Partner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            partner = requireArguments().getSerializable(KEY_PARTNER) as Partner?
            partnerId = requireArguments().getLong(KEY_PARTNER_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_partner, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (partner != null) {
            configureView()
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun configureView() { // Check for valid activity
        if (activity == null || requireActivity().isFinishing) {
            Timber.i("No activity for this View")
            return
        }
        if(partner==null) {
            Timber.e("No partner for Partner View")
            return
        }
        user_title_layout?.title_close_button?.setOnClickListener {dismiss()}
        // url
        Picasso.get()
                .load(Uri.parse(partner!!.largeLogoUrl))
                .placeholder(R.drawable.partner_placeholder)
                .transform(CropCircleTransformation())
                .into(partner_view_logo!!)
        // name
        partner_view_name?.text = partner!!.name
        // description
        partner_view_details?.text = partner!!.description
        // phone
        val phone = partner!!.phone
        if (phone == null || phone.isEmpty()) {
            partner_view_phone_layout?.visibility = View.GONE
        } else {
            partner_view_phone_layout?.visibility = View.VISIBLE
            partner_view_phone?.text = phone
        }
        // address
        val address = partner!!.address
        if (address == null || address.isEmpty()) {
            partner_view_address_layout?.visibility = View.GONE
        } else {
            partner_view_address_layout?.visibility = View.VISIBLE
            partner_view_address?.text = address
        }
        // website
        val website = partner!!.websiteUrl
        if (website == null || website.isEmpty()) {
            partner_view_website_layout?.visibility = View.GONE
        } else {
            partner_view_website_layout?.visibility = View.VISIBLE
            partner_view_website?.text = website
        }
        // email
        val email = partner!!.email
        if (email == null || email.isEmpty()) {
            partner_view_email_layout?.visibility = View.GONE
        } else {
            partner_view_email_layout?.visibility = View.VISIBLE
            partner_view_email?.text = email
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.partner_fragment"
        private const val KEY_PARTNER_ID = "social.entourage.android.partner_id"
        private const val KEY_PARTNER = "social.entourage.android.partner"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param partnerId Partner ID.
         * @return A new instance of fragment PartnerFragment.
         */
        fun newInstance(partnerId: Long): PartnerFragment {
            val fragment = PartnerFragment()
            val args = Bundle()
            args.putLong(KEY_PARTNER_ID, partnerId)
            fragment.arguments = args
            return fragment
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param partner Partner object.
         * @return A new instance of fragment PartnerFragment.
         */
        fun newInstance(partner: Partner?): PartnerFragment {
            val fragment = PartnerFragment()
            val args = Bundle()
            args.putSerializable(KEY_PARTNER, partner)
            fragment.arguments = args
            return fragment
        }
    }
}