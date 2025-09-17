package social.entourage.android.profile.association

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.databinding.FragmentAssociationProfileBinding
import social.entourage.android.tools.utils.CustomAlertDialog

class AssociationProfileFragment : Fragment() {

    private var _binding: FragmentAssociationProfileBinding? = null
    val binding: FragmentAssociationProfileBinding get() = _binding!!
    private val associationPresenter: AssociationPresenter by lazy { AssociationPresenter() }
    var partner: Partner? = null
    private val args: AssociationProfileFragmentArgs by navArgs()
    private var isFromNotifs = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssociationProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButton()
        isFromNotifs = args.isFromNotif
        associationPresenter.getPartnerInfos(args.partnerId)
        associationPresenter.getPartnerSuccess.observe(requireActivity(), ::handleResponse)
        associationPresenter.followSuccess.observe(requireActivity(), ::handleFollowResponse)
        handleFollowButton()
    }

    private fun handleFollowResponse(success: Boolean) {
        if (success) updateButtonFollow()
    }

    private fun handleResponse(success: Boolean) {
        if (success) {
            partner = associationPresenter.partner.value
            updateView()
        }
        //TODO display error
    }

    private fun updateView() {
        with(binding) {
            partner?.name?.let {
                assoProfileName.visibility = View.VISIBLE
                assoProfileName.text = it
            }
            partner?.description?.let {
                assoProfileDescription.visibility = View.VISIBLE
                assoProfileDescription.text = it
            }
            partner?.phone?.let {
                assoProfilePhone.root.visibility = View.VISIBLE
                assoProfilePhone.assoInfoContent.text = it
            }
            partner?.websiteUrl?.let {
                assoProfileWeb.root.visibility = View.VISIBLE
                assoProfileWeb.assoInfoContent.text = it
            }
            partner?.address?.let {
                assoProfileAddress.root.visibility = View.VISIBLE
                assoProfileAddress.assoInfoContent.text = it
            }
            partner?.email?.let {
                assoProfileEmail.root.visibility = View.VISIBLE
                assoProfileEmail.assoInfoContent.text = it
            }
            partner?.donationsNeeds?.let {
                assoProfileDonation.root.visibility = View.VISIBLE
                assoProfileNeeds.visibility = View.VISIBLE
                assoProfileDonation.assoNeedsContent.text = it
            }
            partner?.volunteersNeeds?.let {
                assoProfileVolunteers.root.visibility = View.VISIBLE
                assoProfileNeeds.visibility = View.VISIBLE
                assoProfileVolunteers.assoNeedsContent.text = it
            }
            partner?.largeLogoUrl.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(assoProfileImageAssociation)
            }
        }
        updateButtonFollow()
    }

    private fun updateButtonFollow() {
        partner?.let {
            val label =
                if (it.isFollowing) getString(R.string.following) else getString(R.string.follow)
            val textColor = ContextCompat.getColor(
                requireContext(),
                if (it.isFollowing) R.color.orange else R.color.white
            )
            val background = ResourcesCompat.getDrawable(
                resources,
                if (it.isFollowing) R.drawable.new_bg_rounded_button_orange_stroke else R.drawable.new_bg_rounded_button_orange_fill,
                null
            )
            val rightDrawable = ResourcesCompat.getDrawable(
                resources,
                if (it.isFollowing) R.drawable.new_check else R.drawable.new_plus_white,
                null
            )
            binding.assoProfileSubscribe.button.text = label
            binding.assoProfileSubscribe.button.setTextColor(textColor)
            binding.assoProfileSubscribe.button.background = background
            binding.assoProfileSubscribe.button.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                rightDrawable,
                null
            )
        }
    }

    private fun setBackButton() {
        binding.assoProfileIconBack.setOnClickListener {
            if(isFromNotifs) {
                activity?.onBackPressed()
                return@setOnClickListener
            }

            findNavController().popBackStack()
        }
    }

    private fun handleFollowButton() {
        binding.assoProfileSubscribe.button.setOnClickListener {
            partner?.let {
                if (it.isFollowing) CustomAlertDialog.showWithCancelFirst(
                    requireContext(),
                    getString(R.string.unsubscribe_title),
                    getString(R.string.unsubscribe_content),
                    getString(R.string.yes)
                ) {
                    associationPresenter.updatePartnerFollow(
                        !it.isFollowing,
                        it.id
                    )
                } else associationPresenter.updatePartnerFollow(!it.isFollowing, it.id)
                it.isFollowing != it.isFollowing
            }
        }
    }
}