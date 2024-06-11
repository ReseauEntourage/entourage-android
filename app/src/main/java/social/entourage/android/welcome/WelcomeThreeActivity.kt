package social.entourage.android.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.actions.detail.ActionDetailActivity
import social.entourage.android.actions.list.ActionListFragment
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeThreeBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.list.EVENTS_PER_PAGE
import social.entourage.android.language.LanguageManager
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat

class WelcomeThreeActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeThreeBinding
    private lateinit var eventsPresenter: EventsPresenter
    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()
    private var eventExampleOne:Events? = null
    private var eventExampleTwo:Events? = null
    private var eventExampleThree:Events? = null
    private var demandeExampleOne:Action? = null
    private var demandeExampleTwo:Action? = null
    private var demandeExampleThree:Action? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.updateLanguage()
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutWelcomeThreeBinding.inflate(layoutInflater)
        //INIT PRESENTER
        eventsPresenter = ViewModelProvider(this).get(EventsPresenter::class.java)
        eventsPresenter.getAllEvents.observe(this, ::handleResponseGetEvents)
        actionsPresenter.getAllActions.observe(this, ::handleResponseGetDemands)
        setOnVisibilityGone()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        eventsPresenter.getAllEvents(0, EVENTS_PER_PAGE, currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"future")
        handleEveryButtons()

    }

    fun handleEveryButtons(){
        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.eventExampleOne.layout.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5ACard")
            val intent = Intent(this, social.entourage.android.events.details.feed.FeedActivity::class.java)
            intent.putExtra(
                Const.EVENT_ID,
                eventExampleOne?.id
            )
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()

        }
        binding.eventExampleTwo.layout.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5ACard")
            val intent = Intent(this, social.entourage.android.events.details.feed.FeedActivity::class.java)
            intent.putExtra(
                Const.EVENT_ID,
                eventExampleTwo?.id
            )
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()
        }
        binding.eventExampleThree.layout.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5ACard")
            val intent = Intent(this, social.entourage.android.events.details.feed.FeedActivity::class.java)
            intent.putExtra(
                Const.EVENT_ID,
                eventExampleThree?.id
            )
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()
        }
        binding.demandExampleOne.layoutDemand.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5BCard")
            val intent = Intent(this, ActionDetailActivity::class.java)
            intent.putExtra(Const.ACTION_ID, demandeExampleOne?.id)
            intent.putExtra(Const.ACTION_TITLE, demandeExampleOne?.title)
            intent.putExtra(Const.IS_ACTION_DEMAND, true)
            intent.putExtra(Const.IS_ACTION_MINE, demandeExampleOne?.isMine())
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()
        }
        binding.demandExampleTwo.layoutDemand.setOnClickListener {
            //GO DEMAND TWO
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5BCard")
            val intent = Intent(this, ActionDetailActivity::class.java)
            intent.putExtra(Const.ACTION_ID, demandeExampleTwo?.id)
            intent.putExtra(Const.ACTION_TITLE, demandeExampleTwo?.title)
            intent.putExtra(Const.IS_ACTION_DEMAND, true)
            intent.putExtra(Const.IS_ACTION_MINE, demandeExampleTwo?.isMine())
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()
        }
        binding.demandExampleThree.layoutDemand.setOnClickListener {
            //GO DEMAND THREE
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5BCard")
            val intent = Intent(this, ActionDetailActivity::class.java)
            intent.putExtra(Const.ACTION_ID, demandeExampleThree?.id)
            intent.putExtra(Const.ACTION_TITLE,demandeExampleThree?.title)
            intent.putExtra(Const.IS_ACTION_DEMAND,true)
            intent.putExtra(Const.IS_ACTION_MINE, demandeExampleThree?.isMine())
            intent.putExtra("fromWelcomeActivity", true)
            startActivity(intent)
            finish()
        }
    }

    private fun handleMainButtonForEvent(){
        binding.mainButton.text = getString(R.string.welcome_three_main_button_title_event)
        binding.mainButton.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5A")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fromWelcomeActivityThreeEvent", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }
    }

    private fun handleMainButtonForDemand(){
        binding.mainButton.text = getString(R.string.welcome_three_main_button_title_demand)
        binding.mainButton.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5B")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fromWelcomeActivityThreeDemand", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }
    }

    private fun handleMainButtonForContrib(){
        binding.mainButton.text = getString(R.string.welcome_three_main_button_title_contrib)
        binding.mainButton.setOnClickListener {
            AnalyticsEvents.logEvent("Action_WelcomeOfferHelp_Day5C")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fromWelcomeActivityThreeContrib", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }
    }

    private fun setOnVisibilityGone(){
        binding.contribExampleOne.layoutContrib.visibility = View.GONE
        binding.contribExampleTwo.layoutContrib.visibility = View.GONE
        binding.demandExampleOne.layoutDemand.visibility = View.GONE
        binding.demandExampleTwo.layoutDemand.visibility = View.GONE
        binding.demandExampleThree.layoutDemand.visibility = View.GONE
        binding.eventExampleOne.layout.visibility = View.GONE
        binding.eventExampleTwo.layout.visibility = View.GONE
        binding.eventExampleThree.layout.visibility = View.GONE
        binding.tvTextFour.visibility = View.GONE
        binding.imagePeople.visibility = View.GONE
        binding.imgArtifice.visibility = View.GONE
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        allEvents.let {
            Log.wtf("wtf", "size" + allEvents!!.size)
            var eventCleaned:MutableList<Events> = mutableListOf()
            for(event in allEvents){
                if(event.online == false){
                    eventCleaned.add(event)
                }
            }

            if(eventCleaned.size > 0){
                binding.imgArtifice.visibility = View.VISIBLE
                handleMainButtonForEvent()
                AnalyticsEvents.logEvent("View_WelcomeOfferHelp_Day5A")
                binding.titleWelcomeTwo.text = getString(R.string.welcome_three_title_with_event)
                binding.tvTextOne.text = getString(R.string.welcome_three_text_one)

                eventExampleOne = eventCleaned[0]
                binding.eventExampleOne.eventName.text = eventCleaned[0].title
                binding.eventExampleOne.location.text = eventCleaned[0].metadata?.displayAddress
                binding.eventExampleOne.participants.text = eventCleaned[0].membersCount.toString()
                eventCleaned[0].metadata?.startsAt?.let {
                    var locale = LanguageManager.getLocaleFromPreferences(this)
                    binding.eventExampleOne.date.text = SimpleDateFormat(
                        getString(R.string.event_date_time),
                        locale
                    ).format(
                        it
                    )
                }
                eventCleaned[0].metadata?.landscapeUrl?.let {
                    Glide.with(this)
                        .load(Uri.parse(eventCleaned[0].metadata?.landscapeUrl))
                        .placeholder(R.drawable.ic_event_placeholder)
                        .error(R.drawable.ic_event_placeholder)
                        .apply(RequestOptions().override(90.px, 90.px))
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(binding.eventExampleOne.image)
                } ?: run {
                    Glide.with(this)
                        .load(R.drawable.ic_event_placeholder)
                        .apply(RequestOptions().override(90.px, 90.px))
                        .transform(CenterCrop(), RoundedCorners(20.px))
                        .into(binding.eventExampleOne.image)
                }

                //FILL EVENT THREE
                binding.eventExampleOne.layout.visibility = View.VISIBLE
                if(eventCleaned.size > 2){
                    eventExampleThree = eventCleaned[2]
                    binding.eventExampleThree.eventName.text = eventCleaned[2].title
                    binding.eventExampleThree.layout.visibility = View.VISIBLE
                    binding.eventExampleThree.location.text = eventCleaned[2].metadata?.displayAddress
                    binding.eventExampleThree.participants.text = eventCleaned[2].membersCount.toString()
                    eventCleaned[2].metadata?.startsAt?.let {
                        var locale = LanguageManager.getLocaleFromPreferences(this)
                        binding.eventExampleThree.date.text = SimpleDateFormat(
                            getString(R.string.event_date_time),
                            locale
                        ).format(
                            it
                        )
                    }
                    eventCleaned[2].metadata?.landscapeUrl?.let {
                        Glide.with(this)
                            .load(Uri.parse(eventCleaned[2].metadata?.landscapeUrl))
                            .placeholder(R.drawable.ic_event_placeholder)
                            .error(R.drawable.ic_event_placeholder)
                            .apply(RequestOptions().override(90.px, 90.px))
                            .transform(CenterCrop(), RoundedCorners(20.px))
                            .into(binding.eventExampleThree.image)
                    } ?: run {
                        Glide.with(this)
                            .load(R.drawable.ic_event_placeholder)
                            .apply(RequestOptions().override(90.px, 90.px))
                            .transform(CenterCrop(), RoundedCorners(20.px))
                            .into(binding.eventExampleThree.image)
                    }
                }
                //FILL EVENT TWO
                if(eventCleaned.size > 1){
                    eventExampleTwo = eventCleaned[1]
                    binding.eventExampleTwo.eventName.text = eventCleaned[1].title
                    binding.eventExampleTwo.layout.visibility = View.VISIBLE
                    binding.eventExampleTwo.location.text = eventCleaned[1].metadata?.displayAddress
                    binding.eventExampleTwo.participants.text = eventCleaned[1].membersCount.toString()
                    eventCleaned[1].metadata?.startsAt?.let {
                        var locale = LanguageManager.getLocaleFromPreferences(this)
                        binding.eventExampleTwo.date.text = SimpleDateFormat(
                            getString(R.string.event_date_time),
                            locale
                        ).format(
                            it
                        )
                    }
                    eventCleaned[1].metadata?.landscapeUrl?.let {
                        Glide.with(this)
                            .load(Uri.parse(eventCleaned[1].metadata?.landscapeUrl))
                            .placeholder(R.drawable.ic_event_placeholder)
                            .error(R.drawable.ic_event_placeholder)
                            .apply(RequestOptions().override(90.px, 90.px))
                            .transform(CenterCrop(), RoundedCorners(20.px))
                            .into(binding.eventExampleTwo.image)
                    } ?: run {
                        Glide.with(this)
                            .load(R.drawable.ic_event_placeholder)
                            .apply(RequestOptions().override(90.px, 90.px))
                            .transform(CenterCrop(), RoundedCorners(20.px))
                            .into(binding.eventExampleTwo.image)
                    }
                }
                //IF NO EVENT, FILL A DEMAND LIST AND CHANGE TITLE
            }else{
                actionsPresenter.getAllDemands(0,
                    ActionListFragment.EVENTS_PER_PAGE, currentFilters.travel_distance(),
                    currentFilters.latitude(),currentFilters.longitude(),
                    currentSectionsFilters.getSectionsForWS())
            }
        }
    }

    private fun handleResponseGetDemands(allDemands: MutableList<Action>?) {
        Log.wtf("wtf", "size" + allDemands!!.size)

        //FILL DEMAND ONE
        if(allDemands.size > 0 ){
            binding.imgArtifice.visibility = View.VISIBLE
            handleMainButtonForDemand()
            AnalyticsEvents.logEvent("View_WelcomeOfferHelp_Day5B")
            binding.titleWelcomeTwo.text = getString(R.string.welcome_three_title_with_demand)
            binding.tvTextOne.text = getString(R.string.welcome_three_text_one_with_demand)
            demandeExampleOne = allDemands[0]
            binding.demandExampleOne.demandTitle.text = allDemands[0].title
            binding.demandExampleOne.layoutDemand.visibility = View.VISIBLE
            binding.demandExampleOne.demandLocation.text = allDemands[0].metadata?.displayAddress
            binding.demandExampleOne.demandSectionName.text = MetaDataRepository.getActionSectionNameFromId(allDemands[0].sectionName)
            binding.demandExampleOne.demandSectionPic.setImageDrawable(this.getDrawable(ActionSection.getIconFromId(allDemands[0].sectionName)))
            binding.demandExampleOne.demandUsername.text = allDemands[0].author?.userName
            binding.demandExampleOne.demandLocation.text = allDemands[0].metadata?.displayAddress
            binding.demandExampleOne.demandDate.text = allDemands[0].dateFormattedString(this)

            allDemands[0].author?.avatarURLAsString?.let { avatarURL ->
                Glide.with(binding.demandExampleOne.demandPict.context)
                    .load(avatarURL)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demandExampleOne.demandPict)
            } ?: run {
                Glide.with(binding.demandExampleOne.demandPict.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.demandExampleOne.demandPict)
            }

            //FILL DEMAND THREE
            if(allDemands.size > 2){
                demandeExampleThree = allDemands[2]
                binding.demandExampleThree.demandTitle.text = allDemands[2].title
                binding.demandExampleThree.layoutDemand.visibility = View.VISIBLE
                binding.demandExampleThree.demandLocation.text = allDemands[2].metadata?.displayAddress
                binding.demandExampleThree.demandSectionName.text = MetaDataRepository.getActionSectionNameFromId(allDemands[2].sectionName)
                binding.demandExampleThree.demandSectionPic.setImageDrawable(this.getDrawable(ActionSection.getIconFromId(allDemands[2].sectionName)))
                binding.demandExampleThree.demandUsername.text = allDemands[2].author?.userName
                binding.demandExampleThree.demandLocation.text = allDemands[2].metadata?.displayAddress
                binding.demandExampleThree.demandDate.text = allDemands[2].dateFormattedString(this)

                allDemands[2].author?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(binding.demandExampleThree.demandPict.context)
                        .load(avatarURL)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.demandExampleThree.demandPict)
                } ?: run {
                    Glide.with(binding.demandExampleThree.demandPict.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.demandExampleThree.demandPict)
                }
            }
            //FILL DEMAND TWO
            if(allDemands.size > 1){
                demandeExampleTwo = allDemands[1]
                binding.demandExampleTwo.demandTitle.text = allDemands[1].title
                binding.demandExampleTwo.layoutDemand.visibility = View.VISIBLE
                binding.demandExampleTwo.demandLocation.text = allDemands[1].metadata?.displayAddress
                binding.demandExampleTwo.demandSectionName.text = MetaDataRepository.getActionSectionNameFromId(allDemands[1].sectionName)
                binding.demandExampleTwo.demandSectionPic.setImageDrawable(this.getDrawable(ActionSection.getIconFromId(allDemands[1].sectionName)))
                binding.demandExampleTwo.demandUsername.text = allDemands[1].author?.userName
                binding.demandExampleTwo.demandLocation.text = allDemands[1].metadata?.displayAddress
                binding.demandExampleTwo.demandDate.text = allDemands[1].dateFormattedString(this)

                allDemands[1].author?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(binding.demandExampleTwo.demandPict.context)
                        .load(avatarURL)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.demandExampleTwo.demandPict)
                } ?: run {
                    Glide.with(binding.demandExampleTwo.demandPict.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.demandExampleTwo.demandPict)
                }
            }
        }else{
            binding.imgArtifice.visibility = View.VISIBLE
            binding.imagePeople.visibility = View.VISIBLE
            binding.tvTextFour.visibility = View.VISIBLE
            handleMainButtonForContrib()
            val layoutParams = binding.layoutContentWelcomeOne.layoutParams as ViewGroup.MarginLayoutParams
            val density = resources.displayMetrics.density
            val newMarginTop = (110 * density).toInt() // Conversion de dp en pixels
            layoutParams.setMargins(layoutParams.leftMargin, newMarginTop, layoutParams.rightMargin, layoutParams.bottomMargin)
            binding.layoutContentWelcomeOne.layoutParams = layoutParams

            //IF NO DEMAND , FILL WITH CONTRIB EXAMPLE
            AnalyticsEvents.logEvent("View_WelcomeOfferHelp_Day5C")
            binding.titleWelcomeTwo.text = getString(R.string.welcome_three_title_with_contrib)
            binding.tvTextOne.text = getString(R.string.welcome_three_text_one_with_contrib)
            binding.contribExampleOne.layoutContrib.visibility = View.VISIBLE
            binding.contribExampleTwo.layoutContrib.visibility = View.VISIBLE
            //FILL CONTRIB ONE
            binding.contribExampleOne.name.text = getString(R.string.welcome_three_contrib_name)
            binding.contribExampleOne.location.text = getString(R.string.welcome_three_contrib_location)
            binding.contribExampleOne.date.text = getString(R.string.welcome_three_contrib_date)
            binding.contribExampleOne.distance.text = getString(R.string.welcome_three_contrib_distance)
            binding.contribExampleOne.image.setImageDrawable(getDrawable(R.drawable.contrib_example_one))
            //FILL CONTRIB TWO
            binding.contribExampleTwo.name.text = getString(R.string.welcome_three_contrib_name_two)
            binding.contribExampleTwo.location.text = getString(R.string.welcome_three_contrib_location)
            binding.contribExampleTwo.date.text = getString(R.string.welcome_three_contrib_date)
            binding.contribExampleTwo.distance.text = getString(R.string.welcome_three_contrib_distance)

        }
    }

    @Deprecated("Deprecated in kt 1.9.0")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        this.startActivity(intent)
    }
}