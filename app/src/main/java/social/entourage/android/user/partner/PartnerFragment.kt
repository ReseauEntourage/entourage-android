package social.entourage.android.user.partner

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.collection.ArrayMap
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
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
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.tools.EntBus
import timber.log.Timber

class PartnerFragment : BaseDialogFragment() {

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

    override fun onStop() {
        super.onStop()
        EntBus.post(Events.OnRefreshEntourageInformation())
    }

    fun getPartnerInfos() {
        partnerId?.let { partnerId ->
            EntourageApplication.get().apiModule.userRequest
                    .getPartnerDetail(partnerId)
                    .enqueue(object : Callback<PartnerResponse> {
                        override fun onResponse(call: Call<PartnerResponse>, response: Response<PartnerResponse>) {
                            if (response.isSuccessful) {
                                response.body()?.let { partner = it.partner}
                                configureViews()
                            }
                            else {
                                dismiss()
                            }
                        }
                        override fun onFailure(call: Call<PartnerResponse>, t: Throwable) {
                            dismiss()
                            return
                        }
                    })
        }
    }

    fun updatePartnerFollow(isFollow:Boolean) {
        val params = ArrayMap<String, Any>()
        val isFollowParam = ArrayMap<String, Any>()
        isFollowParam["partner_id"] = partner?.id.toString()
        isFollowParam["active"] = if (isFollow) "true" else "false"
        params["following"] = isFollowParam

        EntourageApplication.get().apiModule.userRequest.updateUserPartner(params).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    partner?.let {
                        it.isFollowing = isFollow
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

        partner?.let { partner ->
            ui_asso_tv_title?.text = partner.name
            ui_asso_tv_subtitle?.text = ""
            ui_asso_iv_logo?.let { logoView ->
                partner.largeLogoUrl?.let { url ->
                    Glide.with(this)
                            .load(Uri.parse(url))
                            .placeholder(R.drawable.partner_placeholder)
                            .circleCrop()
                            .into(logoView)
                }
            }

            ui_asso_tv_description?.let { description ->
                description.text = partner.description
                description.visibility = if (partner.description?.length ?: 0 > 0) View.VISIBLE else View.GONE
                DeepLinksManager.linkify(description)
            }

            if (partner.donationsNeeds.isNullOrEmpty() && partner.volunteersNeeds.isNullOrEmpty()) {
                ui_asso_layout_top_needs?.visibility = View.GONE
                ui_asso_layout_needs?.visibility = View.GONE
            }
            else {
                ui_asso_layout_top_needs?.visibility = View.VISIBLE
                ui_asso_layout_needs?.visibility = View.VISIBLE

                if (partner.donationsNeeds.isNullOrEmpty()) {
                    ui_asso_layout_top_donates?.visibility = View.GONE
                    ui_layout_description_donates?.visibility = View.GONE
                }
                else {
                    ui_asso_layout_top_donates?.visibility = View.VISIBLE
                    ui_layout_description_donates?.visibility = View.VISIBLE
                    ui_asso_tv_donates_description?.let { description->
                        description.text = partner.donationsNeeds
                        DeepLinksManager.linkify(description)
                    }
                }
                if (partner.volunteersNeeds.isNullOrEmpty()) {
                    ui_asso_layout_top_volunteers?.visibility = View.GONE
                    ui_layout_description_volunteers?.visibility = View.GONE
                }
                else {
                    ui_asso_layout_top_volunteers?.visibility = View.VISIBLE
                    ui_layout_description_volunteers?.visibility = View.VISIBLE
                    ui_asso_tv_volunteers_description?.let { description ->
                        description.text = partner.volunteersNeeds
                        DeepLinksManager.linkify(description)
                    }
                }
            }
            ui_button_asso_web?.text = partner.websiteUrl
            ui_button_asso_phone?.text = partner.phone
            ui_button_asso_address?.text = partner.address
            ui_button_asso_mail?.text = partner.email

            ui_layout_phone?.visibility = if (partner.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_address?.visibility = if (partner.address.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_mail?.visibility = if (partner.email.isNullOrEmpty()) View.GONE else View.VISIBLE
            ui_layout_web?.visibility = if (partner.websiteUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            updateButtonFollow()
        }

        ui_button_asso_address?.setOnClickListener {
            partner?.address?.let {  address ->
                openLink("geo:0,0?q=$address", Intent.ACTION_VIEW)
            }
        }
        ui_button_asso_mail?.setOnClickListener {
            partner?.email?.let { email ->
                openLink("mailto:$email", Intent.ACTION_SENDTO)
            }
        }
        ui_button_asso_phone?.setOnClickListener {
            partner?.phone?.let { phone ->
                openLink("tel:$phone", Intent.ACTION_DIAL)
            }
        }
        ui_button_asso_web?.setOnClickListener {
            partner?.websiteUrl?.let { url ->
                openLink(url, Intent.ACTION_VIEW)
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
                ui_button_follow?.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.bg_button_rounded_pre_onboard_orange_plain,
                    null
                )
            }
            else {
                ui_button_follow?.text = getString(R.string.buttonFollowOffPartner)
                ui_button_follow?.setTextColor(resources.getColor(R.color.accent))
                ui_button_follow?.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.bg_button_rounded_pre_onboard_orange_stroke,
                    null
                )
            }
        }
    }

    fun openLink(url: String, action: String) {
        val uri = Uri.parse(url)
        val intent = Intent(action, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.partner_fragment_new"
        const val KEY_PARTNER = "param1"
        const val KEY_PARTNERID = "partnerID"

        fun newInstance(partner: Partner) : PartnerFragment {
            val fragment = PartnerFragment()
            val args = Bundle()
            args.putSerializable(KEY_PARTNER, partner)
            fragment.arguments = args
            return fragment
        }
        fun newInstance(partnerId:Int) : PartnerFragment {
            val fragment = PartnerFragment()
            val args = Bundle()
            args.putInt(KEY_PARTNERID, partnerId)
            fragment.arguments = args
            return fragment
        }
    }
}