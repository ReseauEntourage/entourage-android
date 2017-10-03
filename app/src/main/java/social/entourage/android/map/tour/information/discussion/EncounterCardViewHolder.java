package social.entourage.android.map.tour.information.discussion;

import android.content.Context;
import android.graphics.Paint;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.Utils;

/**
 * Encounter Card View
 */
public class EncounterCardViewHolder extends BaseCardViewHolder {

    private TextView mAuthorView;
    private TextView mStreetPersonNameView;
    private TextView mMessageView;

    private Context context;

    private boolean addressRetrieved = false;
    private Encounter encounter;

    public EncounterCardViewHolder(final View view) {
        super(view);
    }

    @Override
    protected void bindFields() {

        context = itemView.getContext();

        mAuthorView = (TextView) itemView.findViewById(R.id.tic_encounter_author);
        mStreetPersonNameView = (TextView) itemView.findViewById(R.id.tic_encounter_street_name);
        mMessageView = (TextView) itemView.findViewById(R.id.tic_encounter_message);

        mAuthorView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(final View v) {
                 if (!encounter.isMyEncounter()) return;
                 BusProvider.getInstance().post(new Events.OnTourEncounterViewRequestedEvent(encounter));
             }
         });

        mStreetPersonNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!encounter.isMyEncounter()) return;
                BusProvider.getInstance().post(new Events.OnTourEncounterViewRequestedEvent(encounter));
            }
        });
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Encounter)data);
    }

    public void populate(Encounter encounter) {

        mAuthorView.setText(itemView.getContext().getString(R.string.encounter_author_format, encounter.getUserName()));
        mAuthorView.setPaintFlags(mAuthorView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        String encounterLocation = itemView.getResources().getString(
                encounter.isMyEncounter() ? R.string.tour_info_encounter_location_mine : R.string.tour_info_encounter_location,
                encounter.getStreetPersonName());
        Spanned s = Utils.fromHtml(encounterLocation);
        mStreetPersonNameView.setText(s);
        //mMessageView.setText(encounter.getMessage());

        this.encounter = encounter;
    }

    public static int getLayoutResource() {
        return R.layout.tour_information_encounter_card_view;
    }

}
