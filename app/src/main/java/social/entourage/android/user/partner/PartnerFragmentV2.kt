package social.entourage.android.user.partner

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_partner_v2.*
import kotlinx.android.synthetic.main.layout_view_title.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.api.request.PartnerResponse
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.tools.CropCircleTransformation

private const val KEY_PARTNER = "param1"
private const val KEY_PARTNERID = "partnerID"

class PartnerFragmentV2 : EntourageDialogFragment() {

    private var partner: Partner? = null
    private var partnerId:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            partner = arguments?.getSerializable(KEY_PARTNER) as Partner?
            partnerId = arguments?.getInt(KEY_PARTNERID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_partner_v2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (partner == null) {
            if (partnerId == null) {
                dismiss()
                return
            }
            else {
                getPartnerInfos()
            }
        }
        else {
            configureViews()
        }
    }

    fun getPartnerInfos() {
        val application = EntourageApplication.get()
       val call = application.entourageComponent.userRequest.getPartnerDetail(partnerId!!)

        call.enqueue(object : Callback<PartnerResponse> {
            override fun onResponse(call: Call<PartnerResponse>, response: Response<PartnerResponse>) {
                if (response.isSuccessful) {
                   val body = response.body()
                   partner = body?.partner
                    configureViews()
                }
                else {
                    dismiss()
                    return
                }
            }
            override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                dismiss()
                return
            }
        })
    }

    fun updatePartnerFollow(isFollow:Boolean) {
        val application = EntourageApplication.get()

        val params = ArrayMap<String, Any>()
        val isFollowParam = ArrayMap<String,Any>()
        isFollowParam["partner_id"] = partner?.id.toString()
        isFollowParam["active"] = if (isFollow) "true" else "false"
        params["following"] = isFollowParam

        val call = application.entourageComponent.userRequest.updateUserPartner(params)

        call.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    partner?.let {
                        partner?.isFollowing = isFollow
                        updateButtonFollow()
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
        })

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

            if (it.description?.length ?: 0 > 0) {
                ui_layout_description?.visibility = View.VISIBLE
            }
            else {
                ui_layout_description?.visibility = View.GONE
            }

            DeepLinksManager.linkify(ui_asso_tv_description)

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
                    DeepLinksManager.linkify(ui_asso_tv_donates_description)
                }
                if (it.volunteersNeeds.isNullOrEmpty()) {
                    ui_asso_layout_top_volunteers?.visibility = View.GONE
                    ui_layout_description_volunteers?.visibility = View.GONE
                }
                else {
                    ui_asso_layout_top_volunteers?.visibility = View.VISIBLE
                    ui_layout_description_volunteers?.visibility = View.VISIBLE
                    ui_asso_tv_volunteers_description?.text = it.volunteersNeeds
                    DeepLinksManager.linkify(ui_asso_tv_volunteers_description)
                }
            }
            ui_button_asso_web?.text = it.websiteUrl
            ui_button_asso_phone?.text = it.phone
            ui_button_asso_address?.text = it.address
            ui_button_asso_mail.text = it.email

            ui_layout_phone?.visibility = if (it.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_address?.visibility = if (it.address.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_mail?.visibility = if (it.email.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_web?.visibility = if (it.websiteUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            updateButtonFollow()
        }

        ui_button_asso_address?.setOnClickListener {
            partner?.address?.let {  address ->
                openLink("geo:0,0?q=$address",Intent.ACTION_VIEW)
            }
        }
        ui_button_asso_mail?.setOnClickListener {
            partner?.email?.let { email ->
                openLink("mailto:$email",Intent.ACTION_SENDTO)
            }
        }
        ui_button_asso_phone?.setOnClickListener {
            partner?.phone?.let { phone ->
                openLink("tel:$phone",Intent.ACTION_DIAL)
            }
        }
        ui_button_asso_web?.setOnClickListener {
            partner?.websiteUrl?.let { url ->
                openLink(url,Intent.ACTION_VIEW)
            }
        }

        ui_button_follow?.setOnClickListener {
            partner?.let {
                if (it.isFollowing) {
                    showPopInfoUnfollow()
                }
                else {
                    updatePartnerFollow(true)
                }
            }
        }
    }

    fun showPopInfoUnfollow() {
        val alertDialog = AlertDialog.Builder(requireContext())
        val _title = getString(R.string.partnerFollowTitle).format(partner?.name)
        val _message = getString(R.string.partnerFollowMessage).format(partner?.name)
        alertDialog.setTitle(_title)
        alertDialog.setMessage(_message)
        alertDialog.setPositiveButton(R.string.partnerFollowButtonValid) { _, _ ->
            updatePartnerFollow(false)
        }
        alertDialog.setNegativeButton(R.string.partnerFollowButtonCancel) { dialog, _ ->
            dialog.dismiss()
        }

        alertDialog.show()
    }

    fun updateButtonFollow() {
        partner?.let {
            if (it.isFollowing) {
                ui_button_follow?.text = getString(R.string.buttonFollowOnPartner)
                ui_button_follow?.setTextColor(resources.getColor(R.color.white))
                ui_button_follow?.background = resources.getDrawable(R.drawable.bg_button_rounded_pre_onboard_orange_plain)
            }
            else {
                ui_button_follow?.text = getString(R.string.buttonFollowOffPartner)
                ui_button_follow?.setTextColor(resources.getColor(R.color.accent))
                ui_button_follow?.background = resources.getDrawable(R.drawable.bg_button_rounded_pre_onboard_orange_stroke)
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
        fun newInstance(partner: Partner?,partnerId:Int?) : PartnerFragmentV2 {
            val fragment = PartnerFragmentV2()
            val args = Bundle()
            args.putSerializable(KEY_PARTNER, partner)
            partnerId?.let { args.putInt(KEY_PARTNERID, it) }
            fragment.arguments = args
            return fragment
        }
    }
}