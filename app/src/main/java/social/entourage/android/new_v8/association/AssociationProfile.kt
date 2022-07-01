package social.entourage.android.new_v8.association

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.databinding.NewFragmentAssociationProfileBinding
import social.entourage.android.new_v8.utils.Utils


class AssociationProfile : Fragment() {

    private var _binding: NewFragmentAssociationProfileBinding? = null
    val binding: NewFragmentAssociationProfileBinding get() = _binding!!
    private val associationPresenter: AssociationPresenter by lazy { AssociationPresenter() }
    var partner: Partner? = null
    private val args: AssociationProfileArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentAssociationProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackButton()
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
                name.visibility = View.VISIBLE
                name.text = it
            }
            partner?.description?.let {
                description.visibility = View.VISIBLE
                description.text = it
            }
            partner?.phone?.let {
                phone.root.visibility = View.VISIBLE
                phone.content.text = it
            }
            partner?.websiteUrl?.let {
                web.root.visibility = View.VISIBLE
                web.content.text = it
            }
            partner?.address?.let {
                address.root.visibility = View.VISIBLE
                address.content.text = it
            }
            partner?.email?.let {
                email.root.visibility = View.VISIBLE
                email.content.text = it
            }
            partner?.donationsNeeds?.let {
                donation.root.visibility = View.VISIBLE
                needs.visibility = View.VISIBLE
                donation.content.text = it
            }
            partner?.volunteersNeeds?.let {
                volunteers.root.visibility = View.VISIBLE
                needs.visibility = View.VISIBLE
                volunteers.content.text = it
            }
            partner?.largeLogoUrl.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(imageAssociation)
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
            binding.subscribe.button.text = label
            binding.subscribe.button.setTextColor(textColor)
            binding.subscribe.button.background = background
            binding.subscribe.button.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                rightDrawable,
                null
            )
        }
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun handleFollowButton() {
        binding.subscribe.button.setOnClickListener {
            partner?.let {
                if (it.isFollowing) Utils.showAlertDialogButtonClicked(
                    requireView(),
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