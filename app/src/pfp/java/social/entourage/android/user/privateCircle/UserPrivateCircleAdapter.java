package social.entourage.android.user.privateCircle;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;

/**
 * Created by Mihai Ionescu on 25/05/2018.
 */
public class UserPrivateCircleAdapter extends RecyclerView.Adapter {

    public static class PrivateCircleViewHolder extends RecyclerView.ViewHolder {

        public PrivateCircleViewHolder(final View itemView) {
            super(itemView);
        }
    }


    private List<BaseOrganization> circleList;

    public UserPrivateCircleAdapter(List<BaseOrganization> circleList) {
        this.circleList = circleList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_private_circle_item, parent, false);
        return new PrivateCircleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        BaseOrganization organization = circleList.get(position);
        PrivateCircleViewHolder privateCircleViewHolder = (PrivateCircleViewHolder) holder;
        //TODO Populate the view
    }

    @Override
    public int getItemCount() {
        if (circleList == null) return 0;
        return circleList.size();
    }

    public void setPrivateCircleList(List<BaseOrganization> circleList) {
        this.circleList = circleList;
        notifyDataSetChanged();
    }
}
