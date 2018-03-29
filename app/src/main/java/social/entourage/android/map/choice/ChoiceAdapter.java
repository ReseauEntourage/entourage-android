package social.entourage.android.map.choice;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Tour;

/**
 * Items Adapter for the ChoiceFragment RecyclerView
 * @see ChoiceFragment
 */
public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder> {

    private RecyclerViewClickListener itemListener;
    List<Tour> tours;

    public ChoiceAdapter(RecyclerViewClickListener itemListener, List<Tour> tours) {
        this.itemListener = itemListener;
        this.tours = tours;
    }

    @Override
    public int getItemCount() {
        return tours.size();
    }

    @Override
    public ChoiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choice_tour_card, parent, false);
        return new ChoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChoiceViewHolder holder, int position) {
        Tour tour = tours.get(position);

        if (tour.getTourType() != null) {
            if (tour.getTourType().equals(TourType.MEDICAL.getName())) {
                holder.cardImage.setImageResource(R.drawable.ic_medical_active);
                holder.cardTextType.setText(R.string.tour_type_medical);
            }
            else if (tour.getTourType().equals(TourType.ALIMENTARY.getName())) {
                holder.cardImage.setImageResource(R.drawable.ic_distributive_active);
                holder.cardTextType.setText(R.string.tour_type_alimentary);
            }
            else if (tour.getTourType().equals(TourType.BARE_HANDS.getName())) {
                holder.cardImage.setImageResource(R.drawable.ic_social_active);
                holder.cardTextType.setText(R.string.tour_type_bare_hands);
            }
        }

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date = dateFormat.format(tour.getStartTime());
        holder.cardTextDate.setText(date);

        holder.cardTextOrganization.setText(tour.getOrganizationName());
    }

    public class ChoiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // TODO : implement ButterKnife with @Bind and ButterKnife.bind(this, view) - Doesn't work yet

        CardView cardView;
        ImageView cardImage;
        TextView cardTextOrganization;
        TextView cardTextDate;
        TextView cardTextType;

        ChoiceViewHolder(View itemView) {
            super(itemView);
            cardView = ((CardView)itemView.findViewById(R.id.choice_card_view));
            cardImage = ((ImageView)itemView.findViewById(R.id.choice_card_image));
            cardTextOrganization = ((TextView)itemView.findViewById(R.id.choice_card_organization));
            cardTextDate = ((TextView)itemView.findViewById(R.id.choice_card_date));
            cardTextType = ((TextView)itemView.findViewById(R.id.choice_card_type));
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(tours.get(this.getLayoutPosition()));
        }
    }

    public interface RecyclerViewClickListener {
        void recyclerViewListClicked(Tour tour);
    }
}