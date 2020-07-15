package social.entourage.android.partner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_partner_v2.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.tools.CropCircleTransformation

private const val KEY_PARTNER = "param1"

class PartnerFragmentV2 : EntourageDialogFragment() {

    private var partner: Partner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            partner = arguments?.getSerializable(KEY_PARTNER) as Partner?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_partner_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (partner == null)  {
            dismiss()
            return
        }

        configureViews()
    }

    fun configureViews() {
        user_title_layout?.title_text?.text = requireActivity().resources.getString(R.string.title_association)
        user_title_layout?.title_close_button?.setOnClickListener {dismiss()}

        partner?.let {
            ui_asso_tv_title?.text = it.name
            ui_asso_tv_subtitle?.text = ""
            ui_asso_iv_logo?.let { logoView ->
                partner?.largeLogoUrl?.let { url ->
                    Picasso.get()
                            .load(Uri.parse(url))
                            .placeholder(R.drawable.partner_placeholder)
                            .transform(CropCircleTransformation())
                            .into(logoView)
                }
            }

            ui_asso_tv_description?.text = it.description

            if (it.donationsNeeds.isNullOrEmpty() && it.volunteersNeeds.isNullOrEmpty()) {
                ui_asso_layout_top_needs?.visibility = View.GONE
                ui_asso_layout_needs?.visibility = View.GONE
            }
            else {
                ui_asso_layout_top_needs?.visibility = View.VISIBLE
                ui_asso_layout_needs?.visibility = View.VISIBLE

                if (it.donationsNeeds.isNullOrEmpty()) {
                    ui_asso_layout_top_donates?.visibility = View.GONE
                    ui_layout_description_donates?.visibility = View.GONE
                }
                else {
                    ui_asso_layout_top_donates?.visibility = View.VISIBLE
                    ui_layout_description_donates?.visibility = View.VISIBLE
                    ui_asso_tv_donates_description?.text = it.donationsNeeds
                }
                if (it.volunteersNeeds.isNullOrEmpty()) {
                    ui_asso_layout_top_volunteers?.visibility = View.GONE
                    ui_layout_description_volunteers?.visibility = View.GONE
                }
                else {
                    ui_asso_layout_top_volunteers?.visibility = View.VISIBLE
                    ui_layout_description_volunteers?.visibility = View.VISIBLE
                    ui_asso_tv_volunteers_description?.text = it.volunteersNeeds
                }
            }
            ui_asso_tv_website?.text = it.websiteUrl
            ui_asso_tv_phone?.text = it.phone
            ui_asso_tv_address?.text = it.address
        }

        ui_asso_button_address?.setOnClickListener {
            partner?.address?.let {  address ->
                openLink("geo:0,0?q=$address",Intent.ACTION_VIEW)
            }
        }
        ui_asso_button_message?.setOnClickListener {
            partner?.phone?.let { phone ->
                openLink("sms:$phone",Intent.ACTION_SENDTO)
            }
        }
        ui_asso_button_phone?.setOnClickListener {
            partner?.phone?.let { phone ->
                openLink("tel:$phone",Intent.ACTION_DIAL)
            }
        }
        ui_asso_button_website?.setOnClickListener {
            partner?.websiteUrl?.let { url ->
                openLink(url,Intent.ACTION_VIEW)
            }
        }
    }

    fun openLink(url: String, action: String) {
        val uri = Uri.parse(url)
        val intent = Intent(action, uri)
        startActivity(intent)
    }

    companion object {
        const val TAG = "social.entourage.android.partner_fragment_new"
        @JvmStatic
        fun newInstance(partner: Partner?) : PartnerFragmentV2 {
            val fragment = PartnerFragmentV2()
            val args = Bundle()
            args.putSerializable(KEY_PARTNER, partner)
            fragment.arguments = args
            return fragment
        }
    }
}