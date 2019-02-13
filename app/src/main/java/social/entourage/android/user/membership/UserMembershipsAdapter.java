package social.entourage.android.user.membership;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
            //Icon
            membershipIcon.setImageDrawable(membership.getIconDrawable(itemView.getContext()));
            //Title
            membershipTitle.setText(membership.getMembershipTitle());
            //Count
            membershipCount.setText(itemView.getResources().getString(R.string.tour_cell_numberOfPeople, membership.getNumberOfPeople()));
        }
    }

    private ArrayList<UserMembership> membershipList;
    private String membershipType;

    public UserMembershipsAdapter(ArrayList<UserMembership> membershipList, String type) {
        this.membershipList = membershipList;
        this.membershipType = type;
        for (UserMembership userMembership:this.membershipList) {
            userMembership.setType(type);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_membership_item, parent, false);
        return new UserMembershipViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        UserMembership userMembership = membershipList.get(position);
        UserMembershipViewHolder userMembershipViewHolder = (UserMembershipViewHolder) holder;
        if (userMembershipViewHolder != null) userMembershipViewHolder.populate(userMembership);
    }

    @Override
    public int getItemCount() {
        if (membershipList == null) return 0;
        return membershipList.size();
    }

    public void setMembershipList(ArrayList<UserMembership> membershipList) {
        this.membershipList = membershipList;
        for (UserMembership userMembership:this.membershipList) {
            userMembership.setType(membershipType);
        }
        notifyDataSetChanged();
    }

    public UserMembership getItemAt(int position) {
        if (position < 0 || position >= membershipList.size()) return null;
        return membershipList.get(position);
    }
}
