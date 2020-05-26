package social.entourage.android.entourage.information.discussion

import android.graphics.Paint
import android.view.View
import kotlinx.android.synthetic.main.tour_information_encounter_card_view.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.tape.Events.OnTourEncounterViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils

/**
 * Encounter Card View
 */
class EncounterCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var encounter: Encounter? = null

    override fun bindFields() {
        itemView.tic_encounter_author?.setOnClickListener(View.OnClickListener {
            if (encounter?.isMyEncounter==true)
                BusProvider.instance.post(OnTourEncounterViewRequestedEvent(encounter))
        })
        itemView.tic_encounter_street_name?.setOnClickListener(View.OnClickListener {
            if (encounter?.isMyEncounter==true)
                BusProvider.instance.post(OnTourEncounterViewRequestedEvent(encounter))
        })
    }

    override fun populate(encounter: TimestampedObject) {
        if(encounter !is Encounter) return
        itemView.tic_encounter_author?.text = itemView.context.getString(R.string.encounter_author_format, encounter.userName)
        itemView.tic_encounter_author.paintFlags = itemView.tic_encounter_author.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        val encounterLocation = itemView.resources.getString(
                if (encounter.isMyEncounter) R.string.tour_info_encounter_location_mine else R.string.tour_info_encounter_location,
                encounter.streetPersonName)
        val s = Utils.fromHtml(encounterLocation)
        itemView.tic_encounter_street_name?.text = s
        //itemView.tic_encounter_message.setText(encounter.getMessage());
        this.encounter = encounter
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.tour_information_encounter_card_view
    }
}