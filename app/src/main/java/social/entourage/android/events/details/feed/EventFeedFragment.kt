package social.entourage.android.events.details.feed

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.play.core.review.ReviewManagerFactory
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Status
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.Tags
import social.entourage.android.comment.ReactionInterface
import social.entourage.android.comment.SurveyInteractionListener
import social.entourage.android.databinding.FragmentFeedEventBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.SettingsModalFragment
import social.entourage.android.groups.details.feed.CallbackReportFragment
import social.entourage.android.groups.details.feed.GroupMembersPhotosAdapter
import social.entourage.android.groups.details.members.MembersType
import social.entourage.android.language.LanguageManager
import social.entourage.android.members.MembersActivity
import social.entourage.android.profile.myProfile.InterestsAdapter
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.survey.SurveyPresenter
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.CustomAlertDialog
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.Utils.enableCopyOnLongClick
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.utils.px
import social.entourage.android.tools.utils.underline
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.math.abs


class EventFeedFragment : Fragment(), CallbackReportFragment, ReactionInterface,
    SurveyInteractionListener, OnMapReadyCallback {

    private var initialSwipeTopMargin: Int = 0
    private var _binding: FragmentFeedEventBinding? = null
    val binding: FragmentFeedEventBinding get() = _binding!!

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val surveyPresenter: SurveyPresenter by lazy { SurveyPresenter() }
    private var interestsList: ArrayList<String> = ArrayList()
    private var eventId = Const.DEFAULT_VALUE
    private var event: Events? = null
    private var myId: Int? = null
    private val args: EventFeedFragmentArgs by navArgs()
    private var shouldShowPopUp = true
    private var mMap: GoogleMap? = null

    private var memberList: MutableList<EntourageUser> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedEventBinding.inflate(inflater, container, false)
        updatePaddingTopForEdgeToEdge(binding.header)
        reduceButtonSizeImage()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventId = args.eventID
        myId = EntourageApplication.me(activity)?.id
        eventPresenter.getEvent.observe(viewLifecycleOwner, ::handleResponseGetEvent)
        eventPresenter.isEventReported.observe(requireActivity(), ::handleDeletedResponse)
        eventPresenter.hasUserLeftEvent.observe(requireActivity(),::handleLeaveResponse)
        eventPresenter.isUserParticipating.observe(viewLifecycleOwner, ::handleParticipateResponse)
        surveyPresenter.isSurveyVoted.observe(requireActivity(), ::handleSurveyPostResponse)
        eventPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        getPrincipalMember()
        handleMembersButton()
        fragmentResult()
        handleBackButton()
        handleSettingsButton()
        handleAboutButton()
        handleParticipateButton()
        onFragmentResult()
        openLink()
        AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_main)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    fun reduceButtonSizeImage(){
        // Pour le bouton de partage
        binding.bigBtnShare.post {
            try {
                val shareDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.share_icon)
                shareDrawable?.let {
                    val sizeInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 14f, resources.displayMetrics
                    ).toInt()
                    it.setBounds(0, 0, sizeInPx, sizeInPx)
                    binding.bigBtnShare.setCompoundDrawablesRelative(null, null, it, null)
                }
            } catch (e: IllegalStateException) {
                Timber.e(e, "Error setting share icon")
            }
        }
        binding.btnAddCalendar.post {
            try {
                ContextCompat.getDrawable(requireContext(), R.drawable.new_calendar)?.let {
                    val sizeInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 14f, resources.displayMetrics
                    ).toInt()
                    it.setBounds(0, 0, sizeInPx, sizeInPx)
                    binding.btnAddCalendar.setCompoundDrawablesRelative(null, null, it, null)
                }
            } catch (e: IllegalStateException) {
                Timber.e(e, "Error setting calendar icon")
            }
        }

    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    private fun handleResponseGetEvent(getEvent: Events?) {
        getEvent?.let {
            Timber.wtf("wtf event id : ${it.id}")
            event = it
            updateView()
            if(shouldAddToAgenda){
                val startMillis: Long = Calendar.getInstance().run {
                    time = it.metadata?.startsAt ?: time
                    timeInMillis
                }
                val endMillis: Long = Calendar.getInstance().run {
                    time = it.metadata?.endsAt ?: time
                    timeInMillis
                }
                val intent = Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                    .putExtra(CalendarContract.Events.TITLE, it.title)
                    .putExtra(
                        CalendarContract.Events.EVENT_LOCATION,
                        it.metadata?.displayAddress
                    )
                    .putExtra(
                        CalendarContract.Events.AVAILABILITY,
                        CalendarContract.Events.AVAILABILITY_BUSY
                    )
                requireContext().startActivity(intent)
            }
        }
        binding.progressBar.visibility = View.GONE
        handleImageViewAnimation()
        val latitude = event?.location?.latitude ?: 0.0
        val longitude = event?.location?.longitude ?: 0.0
        if(latitude == 0.0 && longitude == 0.0){
            binding.mapView.visibility = View.GONE
            return
        }
        if(event?.member == true) {
           binding.participateView.visibility = View.VISIBLE
           binding.btnAddCalendar.visibility = View.VISIBLE
        }else{
            binding.participateView.visibility = View.GONE
            binding.btnAddCalendar.visibility = View.GONE
        }
        val latLng = LatLng(latitude, longitude)
        // Mise à jour de la carte
        mMap?.apply {
            clear() // Optionnel, pour effacer les anciens marqueurs
            addMarker(MarkerOptions().position(latLng))
            val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(ZOOM)
                .build()
            animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.toolbarLayout.alpha = 1f - res
            binding.eventImageToolbar.alpha = res
            binding.eventNameToolbar.alpha = res
            binding.eventImage.alpha = 1f - res
            //change topmargin when collapsing and no image is shown
            val lp = binding.swipeRefresh.layoutParams as ViewGroup.MarginLayoutParams
            if (res != 0F) {
                lp.topMargin = 0
            } else {
                lp.topMargin = initialSwipeTopMargin
            }
            binding.swipeRefresh.layoutParams = lp
        }
    }

    @SuppressLint("StringFormatInvalid")
    private fun openGoogleMaps() {
        if (event?.online != true) {
            binding.location.root.setOnClickListener {
                val geoUri =
                    String.format(getString(R.string.geoUri), event?.metadata?.displayAddress)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun updateView() {

        MetaDataRepository.metaData.observe(requireActivity(), ::handleMetaData)
        with(binding) {
            eventName.text = event?.title
            eventNameToolbar.text = event?.title
            eventDescription.enableCopyOnLongClick(requireContext())
            if(event != null && event?.membersCount!! > 1){
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.participant_number),
                    event?.membersCount,
                )
            }else{
                eventMembersNumberLocation.text = String.format(
                    getString(R.string.participant_number_singular),
                    event?.membersCount,
                )
            }
            val locale = LanguageManager.getLocaleFromPreferences(requireContext())


            event?.metadata?.placeLimit?.let {
                if(it == 0){
                    placesLimit.root.visibility = View.GONE
                }else{
                    placesLimit.root.visibility = View.VISIBLE
                }
                placesLimit.content.text = String.format(
                    getString(R.string.places_numbers),
                    it,
                )
            }
            event?.metadata?.startsAt?.let {
                binding.dateStartsAt.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_date),
                    locale
                ).format(
                    it
                )
            }
            event?.metadata?.startsAt?.let {
                binding.time.content.text = SimpleDateFormat(
                    context?.getString(R.string.feed_event_time),
                    locale
                ).format(
                    it
                )
            }
            initializeMembersPhotos()
            initializeInterests()
            eventDescription.visibility = View.VISIBLE
            eventDescription.text = event?.description
            if(event?.descriptionTranslations != null){
                eventDescription.text = event?.descriptionTranslations?.translation
            }
            binding.location.icon = AppCompatResources.getDrawable(
                requireContext(),
                if (event?.online == true) R.drawable.new_web else R.drawable.new_location
            )

            (if (event?.online == true) event?.eventUrl else event?.metadata?.displayAddress)?.let {
                binding.location.content.underline(
                    it
                )
            }
            event?.author.let {author->
                //binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), author?.userName)
                author?.partner?.name?.let { partnerName->
                    if(partnerName.isNotEmpty()){
                        binding.tvAssociation.text = String.format(getString(R.string.event_organisez_asso),partnerName)
                        binding.tvAssociation.visibility = View.VISIBLE
                    }
                }
            }
            event?.metadata?.landscapeUrl?.let {
                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.new_group_illu)
                    .centerCrop()
                    .into(eventImage)

                Glide.with(requireActivity())
                    .load(it)
                    .error(R.drawable.new_group_illu)
                    .transform(CenterCrop(), RoundedCorners(5.px))
                    .into(eventImageToolbar)
            } ?: kotlin.run {
                Glide.with(requireActivity())
                    .load(R.drawable.new_group_illu)
                    .centerCrop()
                    .into(eventImage)

                Glide.with(requireActivity())
                    .load(R.drawable.new_group_illu)
                    .transform(CenterCrop(), RoundedCorners(5.px))
                    .into(eventImageToolbar)
            }

            canceled.isVisible = event?.status == Status.CLOSED
            if (event?.status == Status.CLOSED) {
                eventName.setTextColor(getColor(requireContext(), R.color.grey))
                dateStartsAt.content.setTextColor(getColor(requireContext(), R.color.grey))
                dateStartsAt.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.new_calendar_grey
                )
                time.content.setTextColor(getColor(requireContext(), R.color.grey))
                time.icon = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.new_time_grey
                )
            }
        }
        if(event?.member == true){
            binding.buttonJoin.text = getString(R.string.see_conversation_event)
        }else{
            binding.buttonJoin.text = getString(R.string.share_and_join_event)
        }
        openGoogleMaps()
    }

    private fun handleBackButton() {
        binding.iconBack.setOnClickListener {
            requireActivity().finish()
        }
    }


    @SuppressLint("StringFormatInvalid")
    private fun openMap() {
        val geoUri =
            String.format(getString(R.string.geoUri), event?.metadata?.displayAddress)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        startActivityForResult(intent, 0)
    }

    private fun openLink() {
        binding.location.root.setOnClickListener {
            if (event?.online != true) {
                openMap()
            } else {
                var url = event?.eventUrl
                url?.let {
                    url = Utils.checkUrlWithHttps(it)
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivityForResult(browserIntent, 0)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleDeletedResponse(success: Boolean) {
        if(isAdded){
            if (success){
                showToast(getString(R.string.delete_success_send))
            }else{
                showToast(getString(R.string.delete_error_send_failed))
            }
        }
    }

    private fun handleSurveyPostResponse(success: Boolean) {
        if(isAdded && !success){
            showToast("Erreur serveur, veuillez réessayer plus tard")
        }
    }

    private fun fragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            val shouldRefresh = bundle.getBoolean(Const.SHOULD_REFRESH)
            if (shouldRefresh) eventPresenter.getEvent(eventId.toString())
        }
    }

    private fun handleSettingsButton() {
        if(isAdded){
            binding.iconSettings.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Event_detail_action_param)
                event?.let { event ->
                    SettingsModalFragment.newInstance(event).show(parentFragmentManager, SettingsModalFragment.TAG)
                }

            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        eventPresenter.getEvent(eventId.toString())

    }

    private fun handleAboutButton() {
        binding.btnAddCalendar.setOnClickListener {
            shouldAddToAgenda = false
            val startMillis: Long = Calendar.getInstance().run {
                time = event?.metadata?.startsAt ?: time
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                time = event?.metadata?.endsAt ?: time
                timeInMillis
            }
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, event?.title)
                .putExtra(
                    CalendarContract.Events.EVENT_LOCATION,
                    event?.metadata?.displayAddress
                )
                .putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )
            requireContext().startActivity(intent)
        }

        binding.bigBtnShare.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SHARED)
            val shareTitle = getString(R.string.share_title_event)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareTitle + "\n" + event?.title + ": " + "\n" + createShareUrl())
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'URL via"))
        }
    }

    private fun createShareUrl():String{
        val deepLinksHostName = BuildConfig.DEEP_LINKS_URL
        return "https://" + deepLinksHostName + "/app/outings/" + event?.uuid_v2
    }

    private fun getPrincipalMember(){
        eventPresenter.getEventMembers(eventId)

    }

    private fun handleResponseGetMembers(allMembers: MutableList<EntourageUser>?) {
        if (allMembers != null) {
            this.memberList.clear()
            this.memberList.addAll(allMembers)
            var numberOrganizer = 0
            var nameOrganizers = ""
            for(member in allMembers){
                if(member.groupRole == "organizer"){
                    numberOrganizer += 1
                    if(numberOrganizer < 3){
                        nameOrganizers += ", " + member.displayName
                    }
                }
                if(member.id.toInt() == event?.author?.userID){
                    if(member.communityRoles?.contains("Équipe Entourage") == true || member.communityRoles?.contains("Ambassadeur") == true){
                        binding.tvAssociation.text = getString(R.string.event_organisez_entourage)
                        binding.tvAssociation.visibility = View.VISIBLE
                    }
                }
            }
            nameOrganizers.removePrefix(", ")
            if(numberOrganizer > 2 ){
                nameOrganizers += " + " + (numberOrganizer - 2).toString()
            }
            binding.organizer.content.text = String.format(getString(R.string.event_organisez_by), nameOrganizers)

        }

        eventPresenter.getEvent(eventId.toString())
    }

    fun goDiscussion(){
        VibrationUtil.vibrate(requireContext())
        DetailConversationActivity.isSmallTalkMode = false
        startActivity(
            Intent(context, DetailConversationActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                .putExtras(
                    bundleOf(
                        Const.ID to event?.id,
                        Const.SHOULD_OPEN_KEYBOARD to false,
                        Const.IS_CONVERSATION_1TO1 to true,
                        Const.IS_CONVERSATION_1TO1 to false,
                        Const.IS_MEMBER to true,
                        Const.IS_CONVERSATION to true,

                        )
                )
        )
    }

    fun requestInAppReview(context: Context) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val flow = manager.launchReviewFlow(context as Activity, task.result)
                flow.addOnCompleteListener {
                }
            }
        }
    }

    private fun handleParticipateButton() {
        binding.buttonJoin.setOnClickListener {
            requestInAppReview(requireContext())
            val meUser = EntourageApplication.me(activity)
            if(meUser?.roles?.contains("Ambassadeur") == true){
                if(event?.member==true){
                    goDiscussion()
                }
                CustomAlertDialog.showAmbassadorWithTwoButton(requireContext(),
                    onNo = {
                        if (event?.member==false){
                            eventPresenter.joinAsOrganizer(eventId)
                        }
                    }, onYes = {
                        if (event?.member==false){
                            eventPresenter.participate(eventId)
                        }
                    })
            }else{
                if (event?.member==false){
                    eventPresenter.participate(eventId)
                    binding.participateView.visibility = View.VISIBLE
                }else{
                    goDiscussion()
                }
            }
        }

    }

    private fun handleMembersButton() {
        binding.members.setOnClickListener {
            val intent = Intent(context, MembersActivity::class.java).apply {
                // Passage des arguments nécessaires
                putExtra("ID", eventId) // Assure-toi que 'groupId' est un Int
                putExtra("TYPE", MembersType.EVENT.code) // Utilise 'code' pour passer l'enum comme un Int
            }
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun handleParticipateResponse(isParticipating: Boolean) {
        if (isParticipating) {
            event?.let {event ->
                event.member = !event.member
                //handleCreatePostButton()
                eventPresenter.getEvent(eventId.toString())
                if (event.metadata?.placeLimit != null && event.metadata.placeLimit != 0) {
                    showLimitPlacePopUp()
                } else {
                    if (shouldShowPopUp){
                        goDiscussion()
                    }else{
                        goDiscussion()
                    }
                    shouldShowPopUp = false
                }
            }
        }

    }

    private fun handleLeaveResponse(isParticipating: Boolean) {
        event?.let {event ->
            event.member = !event.member
            //handleCreatePostButton()
            eventPresenter.getEvent(eventId.toString())
        }
    }

    private fun showLimitPlacePopUp() {
        CustomAlertDialog.showOnlyOneButton(
            requireContext(),
            getString(R.string.event_limited_places_title),
            getString(R.string.event_limited_places_subtitle),
            getString(R.string.button_OK)
        ) {
            goDiscussion()
        }

    }

    private fun handleMetaData(tags: Tags?) {
        interestsList.clear()
        event?.interests?.let { groupInterests ->
            tags?.interests?.forEach { interest ->
                if (groupInterests.contains(interest.id)) interest.name?.let {
                    interestsList.add(it)
                }
            }
        }
        binding.interests.adapter?.notifyDataSetChanged()
    }

    private fun initializeMembersPhotos() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = event?.members?.let { GroupMembersPhotosAdapter(it) }
        }
    }

    private fun initializeInterests() {
        if (interestsList.isEmpty()) binding.interests.visibility = View.GONE
        else {
            binding.interests.visibility = View.VISIBLE
            binding.interests.apply {
                val layoutManagerFlex = FlexboxLayoutManager(context)
                layoutManagerFlex.flexDirection = FlexDirection.ROW
                layoutManagerFlex.justifyContent = JustifyContent.FLEX_START
                layoutManager = layoutManagerFlex
                adapter = InterestsAdapter(interestsList)
            }
        }
    }

    private fun onFragmentResult() {
        setFragmentResultListener(Const.REQUEST_KEY_SHOULD_REFRESH) { _, bundle ->
            if (bundle.getBoolean(Const.SHOULD_REFRESH)) eventPresenter.getEvent(eventId.toString())
        }
    }

    override fun onSuppressPost(id: Int) {

    }

    override fun onTranslatePost(id: Int) {

    }

    override fun onReactionClicked(postId: Post, reactionId: Int) {
        if(this.event?.member == false){
            AlertDialog.Builder(context) // Utilise 'this' si c'est dans une activité, ou 'getActivity()' si c'est dans un fragment
                .setTitle("Attention")
                .setMessage("Vous devez rejoindre le groupe pour effectuer cette action.")
                .setPositiveButton("Retour") { dialog, which ->
                    // Code à exécuter lorsque le bouton "Retour" est cliqué.
                    // Si tu ne veux rien faire, tu peux laisser ce bloc vide.
                }
                .show()
            return
        }
        eventPresenter.reactToPost(eventId,postId.id!!, reactionId)
    }

    override fun seeMemberReaction(post: Post) {
        MembersActivity.isFromReact = true
        MembersActivity.postId = post.id!!
        val intent = Intent(context, MembersActivity::class.java).apply {
            // Passage des arguments nécessaires
            putExtra("ID", eventId) // Assure-toi que 'groupId' est un Int
            putExtra("TYPE", MembersType.EVENT.code) // Utilise 'code' pour passer l'enum comme un Int
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun deleteReaction(post: Post) {
        if(this.event?.member == false){
            AlertDialog.Builder(context) // Utilise 'this' si c'est dans une activité, ou 'getActivity()' si c'est dans un fragment
                .setTitle("Attention")
                .setMessage("Vous devez rejoindre le groupe pour effectuer cette action.")
                .setPositiveButton("Retour") { dialog, which ->
                    // Code à exécuter lorsque le bouton "Retour" est cliqué.
                    // Si tu ne veux rien faire, tu peux laisser ce bloc vide.
                }
                .show()
            return
        }
        eventPresenter.deleteReactToPost(eventId,post.id!!)
    }

    override fun onSurveyOptionClicked(postId: Int, surveyResponse: MutableList<Boolean>) {
        surveyPresenter.postSurveyResponseEvent(eventId, postId, surveyResponse)
    }

    override fun onDeleteSurveyClick(postId: Int, surveyResponse: MutableList<Boolean>) {
        //Toast.makeText(requireContext(), "Survey option deleted", Toast.LENGTH_SHORT).show()
    }

    override fun showParticipantWhoVote(survey: Survey, postId: Int, question:String) {
        val intent = Intent(context, ResponseSurveyActivity::class.java).apply {
            ResponseSurveyActivity.survey = survey
            ResponseSurveyActivity.isGroup = false
            ResponseSurveyActivity.itemId = eventId
            ResponseSurveyActivity.postId = postId
            ResponseSurveyActivity.question = question

        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    companion object {
        var shouldAddToAgenda = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latitude = event?.location?.latitude ?: 0.0
        val longitude = event?.location?.longitude ?: 0.0
        val latLong = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions().position(latLong)
        )
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude)).zoom(ZOOM).build()
        googleMap.animateCamera(
            CameraUpdateFactory
                .newCameraPosition(cameraPosition)
        )
        googleMap.setOnMapClickListener {
            openMap()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}