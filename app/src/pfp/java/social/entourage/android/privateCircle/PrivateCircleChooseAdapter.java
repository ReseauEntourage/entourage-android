package social.entourage.android.privateCircle;

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
public class PrivateCircleChooseAdapter extends RecyclerView.Adapter {

    private static class PrivateCircleChooseViewHolder extends RecyclerView.ViewHolder {

        protected ImageView privateCircleIcon;
        protected TextView privateCircleTitle;
        protected CheckBox privateCircleCheckbox;

        public PrivateCircleChooseViewHolder(final View itemView, OnCheckedChangeListener onCheckedChangeListener) {
            super(itemView);

            privateCircleIcon = itemView.findViewById(R.id.privatecircle_icon);
            privateCircleTitle = itemView.findViewById(R.id.privatecircle_title);
            privateCircleCheckbox = itemView.findViewById(R.id.privatecircle_checkbox);

            if (privateCircleCheckbox != null) {
                privateCircleCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (privateCircleCheckbox != null) {
                        privateCircleCheckbox.setChecked(!privateCircleCheckbox.isChecked());
                    }
                }
            });
        }

    }

    private List<Entourage> privateCircleList = new ArrayList<>();
    private int selectedPrivateCircle = AdapterView.INVALID_POSITION;
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

    public void addPrivateCircleList(final List<Entourage> privateCircleList) {
        this.privateCircleList.addAll(privateCircleList);
        selectedPrivateCircle = 0;
        notifyDataSetChanged();
    }

    public int getSelectedPrivateCircle() {
        return selectedPrivateCircle;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_privatecircle_choose_item, parent, false);
        return new PrivateCircleChooseViewHolder(view, onCheckedChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        Entourage entourage = getItemAt(position);
        if (entourage != null) {
            PrivateCircleChooseViewHolder viewHolder = (PrivateCircleChooseViewHolder)holder;
            Context context = viewHolder.itemView.getContext();

            viewHolder.privateCircleIcon.setImageDrawable(entourage.getIconDrawable(context));

            if (position == selectedPrivateCircle) {
                viewHolder.privateCircleTitle.setTypeface(viewHolder.privateCircleTitle.getTypeface(), Typeface.BOLD);
            } else {
                viewHolder.privateCircleTitle.setTypeface(Typeface.create(viewHolder.privateCircleTitle.getTypeface(), Typeface.NORMAL));
            }
            viewHolder.privateCircleTitle.setText(entourage.getTitle());

            // set the tag to null so that oncheckedchangelistener exits when populating the view
            viewHolder.privateCircleCheckbox.setTag(null);
            // set the check state
            viewHolder.privateCircleCheckbox.setChecked(position == selectedPrivateCircle);
            // set the tag to the item position
            viewHolder.privateCircleCheckbox.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return privateCircleList.size();
    }

    protected Entourage getItemAt(final int position) {
        if (privateCircleList == null || position < 0 || position >= privateCircleList.size()) {
            return null;
        }
        return privateCircleList.get(position);
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
            if (position == selectedPrivateCircle) {
                selectedPrivateCircle = AdapterView.INVALID_POSITION;
            } else {
                selectedPrivateCircle = position;
            }
            PrivateCircleChooseAdapter.this.notifyDataSetChanged();
        }
    }
}
