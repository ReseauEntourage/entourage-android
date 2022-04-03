package social.entourage.android.new_v8.association

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import social.entourage.android.databinding.NewFragmentAssociationProfileBinding


class AssociationProfile : Fragment() {

    private var _binding: NewFragmentAssociationProfileBinding? = null
    val binding: NewFragmentAssociationProfileBinding get() = _binding!!
    private val settingsPresenter: AssociationPresenter by lazy { AssociationPresenter() }


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
        settingsPresenter.getPartnerInfos(1)
        settingsPresenter.getPartnerSuccess.observe(requireActivity(), ::handleResponse)
    }

    private fun handleResponse(success: Boolean) {
        if (success) updateView()
        //TODO display error
    }

    private fun updateView() {
        val partner = settingsPresenter.partner.value
        with(binding) {
            name.text = partner?.name
            description.text = partner?.description
            phone.content.text = partner?.phone
            web.content.text = partner?.websiteUrl
            address.content.text = partner?.address
            email.content.text = partner?.email
            imageAssociation
            partner?.largeLogoUrl.let {
                Glide.with(requireActivity())
                    .load(Uri.parse(it))
                    .circleCrop()
                    .into(imageAssociation)
            }
        }
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener { findNavController().popBackStack() }
    }

}