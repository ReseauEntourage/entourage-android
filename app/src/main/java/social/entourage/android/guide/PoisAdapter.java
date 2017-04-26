package social.entourage.android.guide;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.guide.poi.PoiViewHolder;

/**
 * Point of interest adapter
 *
 * Created by mihaiionescu on 26/04/2017.
 */

public class PoisAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected List<Poi> items = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_poi_card, parent, false);
        return new PoiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ((PoiViewHolder)holder).populate(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItems(Collection<Poi> poiList) {
        items.clear();
        items.addAll(poiList);
        notifyDataSetChanged();
    }

    public void removeAll() {
        items.clear();
        notifyDataSetChanged();
    }
}
