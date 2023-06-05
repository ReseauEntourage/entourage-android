package social.entourage.android.welcome

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.new_contrib_item.view.*
import kotlinx.android.synthetic.main.new_demand_item.view.*
import kotlinx.android.synthetic.main.new_event_item.view.*
import kotlinx.android.synthetic.main.new_event_item.view.information
import social.entourage.android.R
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.*
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityLayoutWelcomeThreeBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.feed.FeedActivity
import social.entourage.android.events.list.EVENTS_PER_PAGE
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.*

class WelcomeThreeActivity: BaseActivity() {

    private lateinit var binding: ActivityLayoutWelcomeThreeBinding
    private lateinit var eventsPresenter: EventsPresenter
    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()


    override fun onCreate(savedInstanceState: Bundle?) {
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


    }

    fun handleEveryButtons(){
        binding.eventExampleOne.layout.setOnClickListener {
            var intent = Intent()
            intent.put
            (view.context as? Activity)?.startActivityForResult(
                Intent(
                    view.context,
                    FeedActivity::class.java
                ).putExtra(
                    Const.EVENT_ID,
                    child.id
                ), 0
            )
        }
        binding.eventExampleTwo.layout.setOnClickListener {
            //GO EVENT TWO
        }
        binding.eventExampleThree.layout.setOnClickListener {
            //GO EVENT THREE
        }
        binding.demandExampleOne.layoutDemand.setOnClickListener {
            //GO DEMAND ONE
        }
        binding.demandExampleTwo.layoutDemand.setOnClickListener {
            //GO DEMAND TWO
        }
        binding.demandExampleThree.layoutDemand.setOnClickListener {
            //GO DEMAND THREE
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
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        allEvents.let {
            Log.wtf("wtf", "action 1.name" + allEvents!!.size)
            if(allEvents.size == 0 ){
                binding.eventExampleOne.eventName.text = allEvents[0].title
                binding.eventExampleOne.location.text = allEvents[0].metadata?.displayAddress
                binding.eventExampleOne.participants.text = allEvents[0].membersCount.toString()
                allEvents[0].metadata?.startsAt?.let {
                    binding.eventExampleOne.date.text = SimpleDateFormat(
                        getString(R.string.event_date_time),
                        Locale.FRANCE
                    ).format(
                        it
                    )
                }
                allEvents[0].metadata?.landscapeUrl?.let {
                    Glide.with(this)
                        .load(Uri.parse(allEvents[0].metadata?.landscapeUrl))
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
                if(allEvents.size > 2){
                    binding.eventExampleThree.eventName.text = allEvents[2].title
                    binding.eventExampleThree.layout.visibility = View.VISIBLE
                    binding.eventExampleThree.location.text = allEvents[2].metadata?.displayAddress
                    binding.eventExampleThree.participants.text = allEvents[2].membersCount.toString()
                    allEvents[2].metadata?.startsAt?.let {
                        binding.eventExampleThree.date.text = SimpleDateFormat(
                            getString(R.string.event_date_time),
                            Locale.FRANCE
                        ).format(
                            it
                        )
                    }
                    allEvents[2].metadata?.landscapeUrl?.let {
                        Glide.with(this)
                            .load(Uri.parse(allEvents[2].metadata?.landscapeUrl))
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
                if(allEvents.size > 1){
                    binding.eventExampleTwo.eventName.text = allEvents[1].title
                    binding.eventExampleTwo.layout.visibility = View.VISIBLE
                    binding.eventExampleTwo.location.text = allEvents[1].metadata?.displayAddress
                    binding.eventExampleTwo.participants.text = allEvents[1].membersCount.toString()
                    allEvents[1].metadata?.startsAt?.let {
                        binding.eventExampleTwo.date.text = SimpleDateFormat(
                            getString(R.string.event_date_time),
                            Locale.FRANCE
                        ).format(
                            it
                        )
                    }
                    allEvents[2].metadata?.landscapeUrl?.let {
                        Glide.with(this)
                            .load(Uri.parse(allEvents[1].metadata?.landscapeUrl))
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
                    social.entourage.android.actions.list.EVENTS_PER_PAGE, currentFilters.travel_distance(),
                    currentFilters.latitude(),currentFilters.longitude(),
                    currentSectionsFilters.getSectionsForWS())
            }
        }
    }

    private fun handleResponseGetDemands(allDemands: MutableList<Action>?) {
        Log.wtf("wtf", "action 1.name" + allDemands!!.size)

        //FILL DEMAND ONE
        if(allDemands.size == 0 ){
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
                binding.demandExampleThree.demandTitle.text = allDemands[1].title
                binding.demandExampleThree.layoutDemand.visibility = View.VISIBLE
                binding.demandExampleThree.demandLocation.text = allDemands[1].metadata?.displayAddress
                binding.demandExampleThree.demandSectionName.text = MetaDataRepository.getActionSectionNameFromId(allDemands[1].sectionName)
                binding.demandExampleThree.demandSectionPic.setImageDrawable(this.getDrawable(ActionSection.getIconFromId(allDemands[1].sectionName)))
                binding.demandExampleThree.demandUsername.text = allDemands[1].author?.userName
                binding.demandExampleThree.demandLocation.text = allDemands[1].metadata?.displayAddress
                binding.demandExampleThree.demandDate.text = allDemands[1].dateFormattedString(this)

                allDemands[1].author?.avatarURLAsString?.let { avatarURL ->
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
        }else{
            //IF NO DEMAND , FILL WITH CONTRIB EXAMPLE
            binding.contribExampleOne.layoutContrib.visibility = View.VISIBLE
            binding.contribExampleTwo.layoutContrib.visibility = View.VISIBLE
            binding.tvTextFour.visibility = View.GONE
            //FILL CONTRIB ONE
            binding.contribExampleOne.location.text = action.metadata?.displayAddress
            binding.contribExampleOne.date.text = action.dateFormattedString(binding.context)
            binding.contribExampleOne.image.setImageDrawable(getDrawable(R.drawable.placeholder_user))
            //FILL CONTRIB TWO
            binding.contribExampleTwo.location.text = action.metadata?.displayAddress
            binding.contribExampleTwo.date.text = action.dateFormattedString(this)
            binding.contribExampleTwo.image.setImageDrawable(getDrawable(R.drawable.placeholder_user))
        }
    }

}