package social.entourage.android.guide.poi;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.BaseCardViewHolder;
import social.entourage.android.guide.PoiRenderer;
import social.entourage.android.tools.BusProvider;

/**
 * Point of interest card view holder
 *
 * Created by mihaiionescu on 26/04/2017.
 */

public class PoiViewHolder extends BaseCardViewHolder {

    private TextView poiTitle;
    private TextView poiType;
    private TextView poiAddress;
    private TextView poiDistance;
    private Button poiCallButton;

    private Poi poi;
    private String phone;

    public PoiViewHolder(final View itemView) {
        super(itemView);
    }

    protected void bindFields() {
        poiTitle = (TextView) itemView.findViewById(R.id.poi_card_title);
        poiType = (TextView) itemView.findViewById(R.id.poi_card_type);
        poiAddress = (TextView) itemView.findViewById(R.id.poi_card_address);
        poiDistance = (TextView) itemView.findViewById(R.id.poi_card_distance);
        poiCallButton = (Button) itemView.findViewById(R.id.poi_card_call_button);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (poi == null) return;
                BusProvider.getInstance().post(new Events.OnPoiViewRequestedEvent(poi));
            }
        });

        poiCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phone));
                if (itemView.getContext() != null) {
                    if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                        itemView.getContext().startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void populate(final TimestampedObject data) {
        populate((Poi)data);
    }

    public void populate(Poi poi) {
        if (poi == null) return;

        this.poi = poi;
        poiTitle.setText(poi.getName() != null ? poi.getName() : "");
        PoiRenderer.CategoryType categoryType = PoiRenderer.CategoryType.findCategoryTypeById(poi.getCategoryId());
        poiType.setText(categoryType != null ? categoryType.getName() : "");
        poiAddress.setText(poi.getAddress() != null ? poi.getAddress() : "");

        String distanceAsString = "";
        TourPoint poiLocation = new TourPoint(poi.getLatitude(), poi.getLongitude());
        if (poiLocation != null) {
            distanceAsString = poiLocation.distanceToCurrentLocation();
        }
        poiDistance.setText(distanceAsString);
        phone = poi.getPhone();
        poiCallButton.setVisibility( (phone == null || phone.length() == 0) ? View.GONE : View.VISIBLE);
    }

    public static int getLayoutResource() {
        return R.layout.layout_poi_card;
    }
}
