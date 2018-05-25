package social.entourage.android.user.membership;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import social.entourage.android.R;
import social.entourage.android.api.model.map.UserMembership;

/**
 * Created by Mihai Ionescu on 25/05/2018.
 */
public class UserMembershipsAdapter extends RecyclerView.Adapter {

    public static class UserMembershipViewHolder extends RecyclerView.ViewHolder {

        private ImageView membershipIcon;
        private TextView membershipTitle;
        private TextView membershipCount;

        public UserMembershipViewHolder(final View itemView) {
            super(itemView);
            membershipIcon = itemView.findViewById(R.id.membership_icon);
            membershipTitle = itemView.findViewById(R.id.membership_title);
            membershipCount = itemView.findViewById(R.id.membership_count);
        }

        void populate(UserMembership membership) {
            if (membership == null) {
                membershipIcon.setImageDrawable(null);
                membershipTitle.setText("");
                membershipCount.setText("");
                return;
            }
            //No icon at this point
            //Title
            membershipTitle.setText(membership.getMembershipTitle());
            //Count
            membershipCount.setText(itemView.getResources().getString(R.string.tour_cell_numberOfPeople, membership.getNumberOfPeople()));
        }
    }


    private ArrayList<UserMembership> circleList;

    public UserMembershipsAdapter(ArrayList<UserMembership> circleList) {
        this.circleList = circleList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_membership_item, parent, false);
        return new UserMembershipViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        UserMembership userMembership = circleList.get(position);
        UserMembershipViewHolder userMembershipViewHolder = (UserMembershipViewHolder) holder;
        if (userMembershipViewHolder != null) userMembershipViewHolder.populate(userMembership);
    }

    @Override
    public int getItemCount() {
        if (circleList == null) return 0;
        return circleList.size();
    }

    public void setPrivateCircleList(ArrayList<UserMembership> circleList) {
        this.circleList = circleList;
        notifyDataSetChanged();
    }
}
