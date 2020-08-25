package social.entourage.android.entourage.information.discussion

import android.graphics.Paint
import android.view.View
import kotlinx.android.synthetic.main.layout_tour_information_encounter_card_view.view.*
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
        itemView.tic_encounter_author?.setOnClickListener {
            encounter?.let {
            if (it.isMyEncounter==true)
                BusProvider.instance.post(OnTourEncounterViewRequestedEvent(it))
            }
        }
        itemView.tic_encounter_street_name?.setOnClickListener {
            encounter?.let {
                if (it.isMyEncounter)
                    BusProvider.instance.post(OnTourEncounterViewRequestedEvent(it))
            }
        }
    }

    override fun populate(data: TimestampedObject) {
        (data as? Encounter)?.let {encounter ->
            itemView.tic_encounter_author?.let {
                it.text = itemView.context.getString(R.string.encounter_author_format, encounter.userName)
                it.paintFlags = it.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
            val encounterLocation = itemView.resources.getString(
                    if (encounter.isMyEncounter) R.string.tour_info_encounter_location_mine else R.string.tour_info_encounter_location,
                    encounter.streetPersonName)
            itemView.tic_encounter_street_name?.text = Utils.fromHtml(encounterLocation)
            //itemView.tic_encounter_message.setText(encounter.getMessage());
            this.encounter = encounter
        }
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_tour_information_encounter_card_view
    }
}