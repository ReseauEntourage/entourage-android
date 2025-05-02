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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.FragmentPartnerBinding
import social.entourage.android.api.model.Partner
import social.entourage.android.api.request.PartnerResponse
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.deeplinks.DeepLinksManager
import timber.log.Timber

class PartnerFragment : BaseDialogFragment() {

    private var partner: Partner? = null
    private var partnerId:Int? = null

    private var _binding: FragmentPartnerBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            partner = arguments?.getSerializable(KEY_PARTNER) as Partner?
            partnerId = arguments?.getInt(KEY_PARTNERID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPartnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (partner == null) {
            if (partnerId == null) {
                dismiss()
                return
            }
            else {
                getPartnerInfo()
            }
        }
        else {
            configureViews()
        }
    }

    private fun getPartnerInfo() {
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

    private fun updatePartnerFollow(isFollow:Boolean) {
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
        binding.userTitleLayout.binding.titleText.text = requireActivity().resources.getString(R.string.title_association)
        binding.userTitleLayout.binding.titleCloseButton.setOnClickListener {dismiss()}

        partner?.let { partner ->
            binding.uiAssoTvTitle.text = partner.name
            binding.uiAssoTvSubtitle.text = ""
            binding.uiAssoIvLogo.let { logoView ->
                partner.largeLogoUrl?.let { url ->
                    Glide.with(this)
                            .load(Uri.parse(url))
                            .placeholder(R.drawable.partner_placeholder)
                            .circleCrop()
                            .into(logoView)
                }
            }

            binding.uiAssoTvDescription.let { description ->
                description.text = partner.description
                description.visibility = if ((partner.description?.length ?: 0) > 0) View.VISIBLE else View.GONE
                DeepLinksManager.linkify(description)
            }

            if (partner.donationsNeeds.isNullOrEmpty() && partner.volunteersNeeds.isNullOrEmpty()) {
                binding.uiAssoLayoutTopNeeds.visibility = View.GONE
                binding.uiAssoLayoutNeeds.visibility = View.GONE
            }
            else {
                binding.uiAssoLayoutTopNeeds.visibility = View.VISIBLE
                binding.uiAssoLayoutNeeds.visibility = View.VISIBLE

                if (partner.donationsNeeds.isNullOrEmpty()) {
                    binding.uiAssoLayoutTopDonates.visibility = View.GONE
                    binding.uiLayoutDescriptionDonates.visibility = View.GONE
                }
                else {
                    binding.uiAssoLayoutTopDonates.visibility = View.VISIBLE
                    binding.uiLayoutDescriptionDonates.visibility = View.VISIBLE
                    binding.uiAssoTvDonatesDescription.let { description->
                        description.text = partner.donationsNeeds
                        DeepLinksManager.linkify(description)
                    }
                }
                if (partner.volunteersNeeds.isNullOrEmpty()) {
                    binding.uiAssoLayoutTopVolunteers.visibility = View.GONE
                    binding.uiLayoutDescriptionVolunteers.visibility = View.GONE
                }
                else {
                    binding.uiAssoLayoutTopVolunteers.visibility = View.VISIBLE
                    binding.uiLayoutDescriptionVolunteers.visibility = View.VISIBLE
                    binding.uiAssoTvVolunteersDescription.let { description ->
                        description.text = partner.volunteersNeeds
                        DeepLinksManager.linkify(description)
                    }
                }
            }
            binding.uiButtonAssoWeb.text = partner.websiteUrl
            binding.uiButtonAssoPhone.text = partner.phone
            binding.uiButtonAssoAddress.text = partner.address
            binding.uiButtonAssoMail.text = partner.email

            binding.uiLayoutPhone.visibility = if (partner.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.uiLayoutAddress.visibility = if (partner.address.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.uiLayoutMail.visibility = if (partner.email.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.uiLayoutWeb.visibility = if (partner.websiteUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            updateButtonFollow()
        }

        binding.uiButtonAssoAddress.setOnClickListener {
            partner?.address?.let {  address ->
                openLink("geo:0,0?q=$address", Intent.ACTION_VIEW)
            }
        }
        binding.uiButtonAssoMail.setOnClickListener {
            partner?.email?.let { email ->
                openLink("mailto:$email", Intent.ACTION_SENDTO)
            }
        }
        binding.uiButtonAssoPhone.setOnClickListener {
            partner?.phone?.let { phone ->
                openLink("tel:$phone", Intent.ACTION_DIAL)
            }
        }
        binding.uiButtonAssoWeb.setOnClickListener {
            partner?.websiteUrl?.let { url ->
                openLink(url, Intent.ACTION_VIEW)
            }
        }

        binding.uiButtonFollow.setOnClickListener {
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

    private fun showPopInfoUnfollow() {
        val alertDialog = AlertDialog.Builder(requireContext())
        val title = getString(R.string.partnerFollowTitle).format(partner?.name)
        val message = getString(R.string.partnerFollowMessage).format(partner?.name)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
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
                binding.uiButtonFollow.text = getString(R.string.buttonFollowOnPartner)
                binding.uiButtonFollow.setTextColor(resources.getColor(R.color.white, null))
                binding.uiButtonFollow.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.bg_button_rounded_pre_onboard_orange_plain,
                    null
                )
            }
            else {
                binding.uiButtonFollow.text = getString(R.string.buttonFollowOffPartner)
                binding.uiButtonFollow.setTextColor(resources.getColor(R.color.accent, null))
                binding.uiButtonFollow.background = ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.bg_button_rounded_pre_onboard_orange_stroke,
                    null
                )
            }
        }
    }

    private fun openLink(url: String, action: String) {
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