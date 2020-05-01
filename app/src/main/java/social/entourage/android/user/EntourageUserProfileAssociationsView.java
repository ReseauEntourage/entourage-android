package social.entourage.android.user;

import android.content.Context;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.BaseOrganization;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.UserMembership;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.ItemClickSupport;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.membership.UserMembershipsAdapter;

/**
 * Created by Mihai Ionescu on 24/05/2018.
 */
public class EntourageUserProfileAssociationsView extends RelativeLayout implements UserAssociations {

    TextView userAssociationsTitle;
    RecyclerView userAssociationsView;
    UserOrganizationsAdapter organizationsAdapter;

    TextView userNeighborhoodsTitle;
    RecyclerView userNeighborhoodsView;
    UserMembershipsAdapter userNeighborhoodsAdapter;

    public EntourageUserProfileAssociationsView(final Context context) {
        super(context);
        init(null, 0);
    }

    public EntourageUserProfileAssociationsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EntourageUserProfileAssociationsView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @RequiresApi(21)
    public EntourageUserProfileAssociationsView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(final AttributeSet attrs, final int defStyleAttr) {
        inflate(getContext(), R.layout.layout_user_entourage_associations, this);

        userAssociationsTitle = findViewById(R.id.user_associations_title);
        userAssociationsView = findViewById(R.id.user_associations_view);

        userNeighborhoodsTitle = findViewById(R.id.user_neighborhoods_title);
        userNeighborhoodsView = findViewById(R.id.user_neighborhoods_view);
    }

    @Override
    public void initUserAssociations(final User user, final UserFragment userFragment) {
        List<BaseOrganization> organizationList = new ArrayList<>();
        if (user.getPartner() != null) {
            organizationList.add(user.getPartner());
        }
        if (user.getOrganization() != null) {
            organizationList.add(user.getOrganization());
        }
        if (organizationsAdapter == null) {
            userAssociationsView.setLayoutManager(new LinearLayoutManager(getContext()));

            organizationsAdapter = new UserOrganizationsAdapter(organizationList);
            userAssociationsView.setAdapter(organizationsAdapter);

            ItemClickSupport.addTo(userAssociationsView)
                    .setOnItemClickListener((recyclerView, position, v) -> userFragment.onEditProfileClicked());
        } else {
            organizationsAdapter.setOrganizationList(organizationList);
        }

        userAssociationsTitle.setVisibility( organizationList.size() > 0 ? View.VISIBLE : View.GONE );
        userAssociationsView.setVisibility( organizationList.size() > 0 ? View.VISIBLE : View.GONE );

        ArrayList<UserMembership> userMembershipList = user.getMemberships(BaseEntourage.GROUPTYPE_NEIGHBORHOOD);
        if (userNeighborhoodsAdapter == null) {
            userNeighborhoodsView.setLayoutManager(new LinearLayoutManager(getContext()));

            userNeighborhoodsAdapter = new UserMembershipsAdapter(userMembershipList, BaseEntourage.GROUPTYPE_NEIGHBORHOOD);

            ItemClickSupport.addTo(userNeighborhoodsView)
                    .setOnItemClickListener((recyclerView, position, v) -> {
                        UserMembership userMembership = userNeighborhoodsAdapter.getItemAt(position);
                        if (userMembership != null) {
                            BusProvider.INSTANCE.getInstance().post(new Events.OnFeedItemInfoViewRequestedEvent(BaseEntourage.ENTOURAGE_CARD, userMembership.getMembershipUUID(), null));
                        }
                    });
        } else {
            userNeighborhoodsAdapter.setMembershipList(userMembershipList);
        }

        userNeighborhoodsTitle.setVisibility( userMembershipList.size() > 0 ? View.VISIBLE : View.GONE );
        userNeighborhoodsView.setVisibility( userMembershipList.size() > 0 ? View.VISIBLE : View.GONE );

    }
}
