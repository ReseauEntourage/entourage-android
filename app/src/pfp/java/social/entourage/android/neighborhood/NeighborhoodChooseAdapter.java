package social.entourage.android.neighborhood;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by Mihai Ionescu on 05/06/2018.
 */
public class NeighborhoodChooseAdapter extends RecyclerView.Adapter {

    private static class NeighborhoodChooseViewHolder extends RecyclerView.ViewHolder {

        protected ImageView neighborhoodIcon;
        protected TextView neighborhoodTitle;
        protected CheckBox neighborhoodCheckbox;

        public NeighborhoodChooseViewHolder(final View itemView, OnCheckedChangeListener onCheckedChangeListener) {
            super(itemView);

            neighborhoodIcon = itemView.findViewById(R.id.neighborhood_icon);
            neighborhoodTitle = itemView.findViewById(R.id.neighborhood_title);
            neighborhoodCheckbox = itemView.findViewById(R.id.neighborhood_checkbox);

            if (neighborhoodCheckbox != null) {
                neighborhoodCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            }
        }

    }

    private List<Entourage> neighborhoodList = new ArrayList<>();
    private int selectedNeighborhood = AdapterView.INVALID_POSITION;
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    public void addNeighborhoodList(final List<Entourage> neighborhoodList) {
        this.neighborhoodList.addAll(neighborhoodList);
        selectedNeighborhood = 0;
        notifyDataSetChanged();
    }

    public int getSelectedNeighborhood() {
        return selectedNeighborhood;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_neighborhood_choose_item, parent, false);
        return new NeighborhoodChooseViewHolder(view, onCheckedChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        Entourage entourage = getItemAt(position);
        if (entourage != null) {
            NeighborhoodChooseViewHolder viewHolder = (NeighborhoodChooseViewHolder)holder;
            Context context = viewHolder.itemView.getContext();

            viewHolder.neighborhoodIcon.setImageDrawable(entourage.getIconDrawable(context));

            if (position == selectedNeighborhood) {
                viewHolder.neighborhoodTitle.setTypeface(viewHolder.neighborhoodTitle.getTypeface(), Typeface.BOLD);
            } else {
                viewHolder.neighborhoodTitle.setTypeface(Typeface.create(viewHolder.neighborhoodTitle.getTypeface(), Typeface.NORMAL));
            }
            viewHolder.neighborhoodTitle.setText(entourage.getTitle());

            // set the tag to null so that oncheckedchangelistener exits when populating the view
            viewHolder.neighborhoodCheckbox.setTag(null);
            // set the check state
            viewHolder.neighborhoodCheckbox.setChecked(position == selectedNeighborhood);
            // set the tag to the item position
            viewHolder.neighborhoodCheckbox.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return neighborhoodList.size();
    }

    protected Entourage getItemAt(final int position) {
        if (neighborhoodList == null || position < 0 || position >= neighborhoodList.size()) {
            return null;
        }
        return neighborhoodList.get(position);
    }

    /**
     * Listener for checkboxes. At most one checkbox can be checked.
     */
    private class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            // if no tag, exit
            if (compoundButton.getTag() == null) {
                return;
            }
            // get the position
            int position = (Integer) compoundButton.getTag();
            if (position == selectedNeighborhood) {
                selectedNeighborhood = AdapterView.INVALID_POSITION;
            } else {
                selectedNeighborhood = position;
            }
            NeighborhoodChooseAdapter.this.notifyDataSetChanged();
        }
    }
}
