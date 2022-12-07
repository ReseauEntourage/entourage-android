package social.entourage.android.new_v8.actions.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
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
import social.entourage.android.new_v8.actions.create.CreateActionActivity
import social.entourage.android.new_v8.discussions.DetailConversationActivity
import social.entourage.android.new_v8.discussions.DiscussionsPresenter
import social.entourage.android.new_v8.groups.details.rules.GroupRulesActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.ActionSection
import social.entourage.android.new_v8.models.Conversation
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.px


class ActionDetailFragment : Fragment(), OnMapReadyCallback {
    private var _binding: NewFragmentActionDetailBinding? = null
    val binding: NewFragmentActionDetailBinding get() = _binding!!

    private var mCallback:OnDetailActionReceive? = null

    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    private var actionId:Int = 0

    private var mMap: MapView? = null
    private var mGoogleMap:GoogleMap? = null

    var action:Action? = null
    var isDemand = false
    var isMine = false
    var isFromEdit = false

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
    ): View {
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
        //Use to show or create conversation 1 to 1
        discussionPresenter.newConversation.observe(requireActivity(), ::handleGetConversation)
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
            mCallback?.updateTitle(action.title)
        } ?: kotlin.run {
            mCallback?.hideIconReport()
            showCancelView()
        }
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            context?.startActivity(
                Intent(context, DetailConversationActivity::class.java)
                    .putExtras(
                        bundleOf(
                            Const.ID to conversation.id,
                            Const.POST_AUTHOR_ID to conversation.user?.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.NAME to conversation.title,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true,
                            Const.HAS_TO_SHOW_MESSAGE to conversation.hasToShowFirstMessage()
                        )
                    )
            )
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

        binding.uiBtModify.setOnClickListener {
            val intent = Intent(context, CreateActionActivity::class.java)
            intent.putExtra(Const.ACTION_OBJ,action)
            if (isDemand) {
                intent.putExtra(Const.IS_ACTION_DEMAND, true)
            }
            else {
                intent.putExtra(Const.IS_ACTION_DEMAND, false)
            }
            isFromEdit = true
            startActivity(intent)
        }

        binding.uiBtDelete.setOnClickListener {
            val _title = getString(R.string.action_cancel_pop_title, if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
            val _subtitle = getString(R.string.action_cancel_pop_subtitle, if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
            Utils.showAlertDialogButtonClickedWithCrossClose(requireContext(),_title,_subtitle, getString(R.string.action_cancel_pop_bt_no), getString(R.string.action_cancel_pop_bt_yes), showCross = true, {
                showCancelActionMessage(false)
            },
                {
                    showCancelActionMessage(true)
                }
            )
        }
        binding.uiBtContact.setOnClickListener {
            action?.author?.userID?.let { it -> discussionPresenter.createOrGetConversation(it) }
        }

        binding.uiBtBackEmpty.setOnClickListener {
            requireActivity().finish()

        }
    }

    private fun showCancelActionMessage(isOk:Boolean) {
        val _title = getString(R.string.action_cancel_pop_title, if (isDemand) getString(R.string.action_name_demand) else getString(R.string.action_name_contrib))
        val _comment = getString(R.string.action_cancel_pop_comment)
        val _optional = getString(R.string.optional)
        val _placeholder = getString(R.string.action_cancel_pop_comment_placeholder)
        val _btSend = getString(R.string.action_cancel_pop_bt_send)
        Utils.showAlertDialogButtonEditText(requireContext(),_title,_comment,_optional , _placeholder,_btSend) { message ->
            sendCancelAction(isOk,message)
        }
    }

    private fun sendCancelAction(isOk: Boolean, message:String) {
        val _message:String? = if (message.isNotEmpty()) message else null
        actionsPresenter.cancelAction(actionId,isDemand,isOk, _message)
    }

    private fun updateViews() {
        action?.let {
            if (it.isCancel()) {
                showCancelView()
            }
            else {
                binding.layoutFullCancel.isVisible = false
                binding.uiImagePlaceholder.isVisible = false

                action?.imageUrl?.let {
                    Glide.with(binding.uiImageContrib.context)
                        .load(it)
                        .error(R.drawable.ic_placeholder_action)
                        .transform(CenterCrop(), RoundedCorners(14.px))
                        .into(binding.uiImageContrib)
                } ?: kotlin.run {
                    binding.uiImagePlaceholder.isVisible = true
                }

                binding.uiTitleCatContrib.text = MetaDataRepository.getActionSectionNameFromId(it.sectionName)
                binding.uiIvCatContrib.setImageDrawable(ResourcesCompat.getDrawable(resources,ActionSection.getIconFromId(it.sectionName),null))
                binding.uiTitleCatDemand.text = MetaDataRepository.getActionSectionNameFromId(it.sectionName)
                binding.uiIvCatDemand.setImageDrawable(ResourcesCompat.getDrawable(resources,ActionSection.getIconFromId(it.sectionName),null))
                binding.uiActionDescription.text = action?.description

                val _addr = action?.metadata?.displayAddress ?: "-"
                binding.uiLocation.text = getString(R.string.atKm,_addr,"xxx")

                action?.author?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(binding.uiUserIv.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
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

    private fun showCancelView() {
        binding.uiLayoutAuthor.isVisible = false
        binding.uiBtContact.isVisible = false
        binding.layoutTopCancel.isVisible = true
        binding.uiLayoutWhiteTopCancel.isVisible = true
        binding.uiLayoutWhiteMapCancel.isVisible = true
        binding.layoutFullCancel.isVisible = true
    }

    //Google map
    override fun onResume() {
        super.onResume()
        mMap?.onResume()
        if (isFromEdit) {
            isFromEdit = false
            loadAction()
        }
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
        mGoogleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun openMap() {
        val geoUri =
            String.format(getString(R.string.geoUri), action?.metadata?.displayAddress)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        startActivity(intent)
    }
}