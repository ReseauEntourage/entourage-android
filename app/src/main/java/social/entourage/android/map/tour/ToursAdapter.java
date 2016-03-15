package social.entourage.android.map.tour;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import social.entourage.android.api.model.map.Tour;

/**
 * Created by mihaiionescu on 11/03/16.
 */
public class ToursAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Tour> tourList = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return TourViewHolder.fromParent(parent);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Tour tour = tourList.get(position);
        ((TourViewHolder)holder).populate(tour);
    }

    @Override
    public int getItemCount() {
        return tourList.size();
    }

    public void add(Tour addedTour) {
        if (addedTour.getStartTime() != null) {
            for (int i = 0; i < tourList.size(); i++) {
                Tour tour = tourList.get(i);
                Date tourStartTime = tour.getStartTime();
                if (tour.getStartTime() != null) {
                    if (addedTour.getStartTime().after(tourStartTime)) {
                        tourList.add(i, addedTour);
                        notifyItemInserted(i);
                        return;
                    }
                }
            }
        }
        tourList.add(addedTour);
        notifyItemInserted(tourList.size()-1);
    }

    public void removeAllTours() {
        tourList.clear();
        notifyDataSetChanged();
    }

    public void updateTour(Tour updatedTour) {
        for (int i = 0; i < tourList.size(); i++) {
            Tour tour = tourList.get(i);
            if (tour.getId() == updatedTour.getId()) {
                tourList.remove(i);
                tourList.add(i, updatedTour);
                notifyItemChanged(i);
                return;
            }
        }
    }
}
