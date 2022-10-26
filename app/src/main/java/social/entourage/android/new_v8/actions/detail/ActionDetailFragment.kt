package social.entourage.android.new_v8.actions.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.databinding.NewFragmentActionDetailBinding
import social.entourage.android.new_v8.actions.ActionsPresenter
import social.entourage.android.new_v8.groups.details.rules.GroupRulesActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.ActionSection
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.px


class ActionDetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: NewFragmentActionDetailBinding? = null
    val binding: NewFragmentActionDetailBinding get() = _binding!!

    private var mCallback:OnDetailActionReceive? = null

    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }

    private var actionId:Int = 0

    private var mMap: MapView? = null
    private var mGoogleMap:GoogleMap? = null

    var action:Action? = null
    var isDemand = false
    var isMine = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            actionId = it.getInt(Const.ACTION_ID)
            isDemand = it.getBoolean(Const.IS_ACTION_DEMAND)
            isMine = it.getBoolean(Const.IS_ACTION_MINE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NewFragmentActionDetailBinding.inflate(inflater,container,false)

        mMap = binding.uiMapview
        mMap?.onCreate(savedInstanceState)
        mMap?.getMapAsync(this)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews()
        setupButtons()

        loadAction()
        actionsPresenter.getAction.observe(viewLifecycleOwner, ::handleResponseGetDetail)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mCallback = activity as? OnDetailActionReceive
        } catch (e: ClassCastException) {
        }
    }

    private fun loadAction() {
        actionsPresenter.getDetailAction(actionId,isDemand)
    }

    private fun handleResponseGetDetail(action: Action?) {
        this.action = action
        updateViews()

        action?.let {
            if (it.isCancel()) {
                mCallback?.hideIconReport()
            }
        }
    }

    private fun initializeViews() {
        binding.layoutTopCancel.isVisible = false
        binding.layoutTopDemand.isVisible = isDemand
        binding.layoutTopContrib.isVisible = !isDemand

        if (isMine) {
            binding.layoutActionsMy.isVisible = true
            binding.uiBtContact.isVisible = false
        }
        else {
            binding.layoutActionsMy.isVisible = false
            binding.uiBtContact.isVisible = true
        }
    }

    private fun setupButtons() {
        binding.uiLayoutCharte.setOnClickListener {
            val intent = Intent(context, GroupRulesActivity::class.java)
            intent.putExtra(Const.RULES_TYPE,Const.RULES_ACTION)
            startActivity(intent)
        }
        binding.layoutUser.setOnClickListener {
            startActivity(Intent(context, UserProfileActivity::class.java).putExtra(
                Const.USER_ID,
                action?.author?.userID
            ))

        }
        //TODO: à faire
        binding.uiBtModify.setOnClickListener {
            //edit action
            Utils.showToast(requireContext(), getString(R.string.not_implemented))
        }
        binding.uiBtDelete.setOnClickListener {
            //cancel action
            Utils.showToast(requireContext(), getString(R.string.not_implemented))
        }
        binding.uiBtContact.setOnClickListener {
            //contact 1to1
            Utils.showToast(requireContext(), getString(R.string.not_implemented))
        }

        binding.uiBtBackEmpty.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun updateViews() {
        action?.let {
            if (it.isCancel()) {
                binding.uiLayoutAuthor.isVisible = false
                binding.uiBtContact.isVisible = false
                binding.layoutTopCancel.isVisible = true
                binding.uiLayoutWhiteTopCancel.isVisible = true
                binding.uiLayoutWhiteMapCancel.isVisible = true
                binding.layoutFullCancel.isVisible = true
            }
            else {
                binding.layoutFullCancel.isVisible = false

                action?.imageUrl?.let {
                    Glide.with(binding.uiImageContrib.context)
                        .load(it)
                        .error(R.drawable.ic_placeholder_action)
                        .transform(CenterCrop(), RoundedCorners(14.px))
                        .into(binding.uiImageContrib)
                } ?: kotlin.run {
                    Glide.with(binding.uiImageContrib.context)
                        .load(R.drawable.ic_placeholder_action)
                        .transform(CenterCrop(), RoundedCorners(14.px))
                        .into(binding.uiImageContrib)
                }


                binding.uiTitleCatContrib.text = MetaDataRepository.getActionSectionNameFromId(it.sectionName)
                binding.uiIvCatContrib.setImageDrawable(ResourcesCompat.getDrawable(resources,ActionSection.getIconFromId(it.sectionName),null))

                binding.uiTitleCatDemand.text = MetaDataRepository.getActionSectionNameFromId(it.sectionName)
                binding.uiIvCatDemand.setImageDrawable(ResourcesCompat.getDrawable(resources,ActionSection.getIconFromId(it.sectionName),null))

                binding.uiActionDescription.text = action?.description

                val _addr = action?.metadata?.displayAddress ?: "-"
                binding.uiLocation?.text = getString(R.string.atKm,_addr,"xxx")

                action?.author?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(binding.uiUserIv.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.ic_user_photo_small)
                        .error(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(binding.uiUserIv)
                } ?: kotlin.run {
                    binding.uiUserIv.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.placeholder_user,null))
                }

                binding.uiTitleAuthor.text = action?.createdDateString(requireContext())

                binding.uiUserName.text = action?.author?.userName
                binding.uiUserMember.text = action?.memberSinceDateString(requireContext())

                updateMarker()
            }
        }
    }

    //Google map
    override fun onResume() {
        super.onResume()
        mMap?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMap?.onPause()
    }

    override fun onStart() {
        super.onStart()
        mMap?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMap?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMap?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMap?.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mGoogleMap = googleMap
        updateMarker()
        googleMap.setOnMapClickListener {
            openMap()
        }
    }

    private fun updateMarker() {
        val latitude = action?.location?.latitude ?: 48.866669
        val longitude = action?.location?.longitude ?: 2.33333
        val latLong = LatLng(latitude, longitude)
        mGoogleMap?.clear()
        mGoogleMap?.addMarker(
            MarkerOptions().position(latLong).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_radius_location)))
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude)).zoom(15f).build()
        mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun openMap() {
        val geoUri =
            String.format(getString(R.string.geoUri), action?.metadata?.displayAddress)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        startActivity(intent)
    }
}