package social.entourage.android.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.databinding.NewFragmentUserProfileBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.User
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : Fragment() {

    private var _binding: NewFragmentUserProfileBinding? = null
    val binding: NewFragmentUserProfileBinding get() = _binding!!
    private val userPresenter: UserPresenter by lazy { UserPresenter() }
    private lateinit var user: User
    private var interestsList: ArrayList<String> = ArrayList()
    private val args: UserProfileFragmentArgs by navArgs()

    private var isMe = false

    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = args.userId
        userPresenter.getUser(userId)
        initializeInterests()
        setBackButton()
        userPresenter.isGetUserSuccess.observe(requireActivity(), ::handleResponse)
        onReportUser()

        //Use to show or create conversation 1 to 1
        discussionPresenter.newConversation.observe(requireActivity(), ::handleGetConversation)

        binding.message.root.setOnClickListener {
            discussionPresenter.createOrGetConversation(userId)
        }
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            startActivityForResult(
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
                    ), 0
            )
        }
    }

    private fun handleResponse(success: Boolean) {
        if (success) updateView()
        else Toast.makeText(requireActivity(), R.string.user_retrieval_error, Toast.LENGTH_SHORT)
            .show()

    }

    private fun onReportUser() {
        binding.report.setOnClickListener {
            val reportUserBottomDialogFragment =
                ReportModalFragment.newInstance(
                    args.userId,
                    Const.DEFAULT_VALUE,
                    ReportTypes.REPORT_USER
                ,false,false, false, contentCopied = "" )
            reportUserBottomDialogFragment.show(parentFragmentManager, ReportModalFragment.TAG)
        }
        binding.blockUser.setOnClickListener {
            val desc = String.format(getString(R.string.params_block_user_conv_pop_message,userPresenter.user.value?.displayName ?: args.userId))
            CustomAlertDialog.showButtonClickedWithCrossClose(
                requireContext(),
                getString(R.string.params_block_user_conv_pop_title),
                desc,
                getString(R.string.params_block_user_conv_pop_bt_cancel),
                getString(R.string.params_block_user_conv_pop_bt_quit), showCross = false, onNo = {}, onYes = {
                    //TODO: la suite
                    args.userId.let {
                        discussionPresenter.blockUser(it)
                    }
                }
            )
        }
    }

    private fun initializeInterests() {
        with(binding.interests) {
            val layoutManagerFlex = FlexboxLayoutManager(context)
            layoutManagerFlex.flexDirection = FlexDirection.ROW
            layoutManagerFlex.justifyContent = JustifyContent.CENTER
            layoutManager = layoutManagerFlex
            adapter = InterestsAdapter(interestsList)
        }
    }

    private fun handleMetaData() {
        interestsList.clear()
        val userInterests = user.interests
        MetaDataRepository.metaData.value?.interests?.forEach { interest ->
            if (userInterests.contains(interest.id)) interest.name?.let { it -> interestsList.add(it) }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun updateView() {
        userPresenter.user.value?.let { user = it }

        isMe = userPresenter.user.value?.id == EntourageApplication.get().me()?.id

        if (isMe) {
            binding.message.root.visibility = View.GONE
            binding.report.visibility = View.GONE
            binding.blockUser.visibility = View.GONE
            binding.information.text = getString(R.string.my_activity)
        }

        handleMetaData()
        with(binding) {
            name.text = user.displayName
            if (!user.about.isNullOrEmpty()) {
                description.visibility = View.VISIBLE
                description.text = user.about
            }
            user.stats?.let {
                contribution.content.text = it.neighborhoodsCount.toString()
                events.content.text = it.outingsCount.toString()
            }
            user.createdAt?.let {
                var locale = LanguageManager.getLocaleFromPreferences(requireContext())
                binding.joined.date.text = SimpleDateFormat(
                    requireContext().getString(R.string.profile_date_format),
                    locale
                ).format(
                    it
                )
            }
            user.roles?.let {
                if (it.contains("ambassador") || it.contains("Ambassadeur") ) pins.ambassador.visibility = View.VISIBLE
                else pins.ambassador.visibility = View.GONE
            }
            user.partner?.let {
                pins.associationName.text = it.name
                it.smallLogoUrl.let { logo ->
                    Glide.with(requireActivity())
                        .load(logo)
                        .circleCrop()
                        .into(pins.associationAvatar)
                }
            } ?: run {
                pins.association.visibility = View.GONE
            }
            user.avatarURL?.let {
                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(imageUser)
            } ?: run {
                Glide.with(requireActivity())
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(imageUser)
            }
            message.button.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ResourcesCompat.getDrawable(resources, R.drawable.new_message, null),
                null
            )
        }
    }

    private fun setBackButton() {
        binding.iconBack.setOnClickListener { activity?.finish() }
    }
}