package social.entourage.android.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.UserMembership;
import social.entourage.android.user.membership.UserMembershipsAdapter;
import social.entourage.android.user.role.UserRolesFactory;

/**
 * PFP Circles in user profile view
 * Created by Mihai Ionescu on 24/05/2018.
 */
public class PFPUserProfileAssociationsView extends RelativeLayout implements UserAssociations {

    private static final String PRIVATE_CIRCLE = "private_circle";
    private static final String NEIGHBORHOOD = "neighborhood";

    TextView userNeighborhoodsTitle;
    RecyclerView userNeighborhoodsView;
    UserMembershipsAdapter userNeighborhoodsAdapter;

    TextView userPrivateCirclesTitle;
    RecyclerView userPrivateCirclesView;
    UserMembershipsAdapter privateCircleAdapter;

    public PFPUserProfileAssociationsView(final Context context) {
        super(context);
        init(null, 0);
    }

    public PFPUserProfileAssociationsView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PFPUserProfileAssociationsView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(21)
    public PFPUserProfileAssociationsView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(final AttributeSet attrs, final int defStyleAttr) {
        inflate(getContext(), R.layout.layout_user_pfp_membership, this);

        userNeighborhoodsTitle = findViewById(R.id.user_associations_title);
        userNeighborhoodsView = findViewById(R.id.user_associations_view);
        userPrivateCirclesTitle = findViewById(R.id.user_private_circles_title);
        userPrivateCirclesView = findViewById(R.id.user_private_circles_view);
    }

    @Override
    public void initUserAssociations(final User user, final UserFragment userFragment) {
        ArrayList<UserMembership> neighborhoodList = new ArrayList();
        ArrayList<UserMembership> circleList = new ArrayList();
        if (user != null) {
            neighborhoodList = user.getMemberships(NEIGHBORHOOD);
            circleList = user.getMemberships(PRIVATE_CIRCLE);
            ArrayList<String> userRoles = user.getRoles();
            if (userRoles != null && userRoles.size() > 0) {
                String role = userRoles.get(0);
                userPrivateCirclesTitle.setText( UserRolesFactory.getInstance().isVisited(role) ? R.string.user_circles_title_visited: R.string.user_circles_title_visitor );
            }
        }

        if (userNeighborhoodsAdapter == null) {
            userNeighborhoodsView.setLayoutManager(new LinearLayoutManager(getContext()));

            userNeighborhoodsAdapter = new UserMembershipsAdapter(neighborhoodList);
            userNeighborhoodsView.setAdapter(userNeighborhoodsAdapter);

//            ItemClickSupport.addTo(userAssociationsView)
//                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//                        @Override
//                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                            userFragment.onEditProfileClicked();
//                        }
//                    });
        } else {
            userNeighborhoodsAdapter.setPrivateCircleList(neighborhoodList);
        }
        userNeighborhoodsTitle.setVisibility( neighborhoodList.size() > 0 ? View.VISIBLE : View.GONE );
        userNeighborhoodsView.setVisibility( neighborhoodList.size() > 0 ? View.VISIBLE : View.GONE );

        if (privateCircleAdapter == null) {
            userPrivateCirclesView.setLayoutManager(new LinearLayoutManager(getContext()));

            privateCircleAdapter = new UserMembershipsAdapter(circleList);
            userPrivateCirclesView.setAdapter(privateCircleAdapter);

//            ItemClickSupport.addTo(userAssociationsView)
//                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//                        @Override
//                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                            userFragment.onEditProfileClicked();
//                        }
//                    });
        } else {
            privateCircleAdapter.setPrivateCircleList(circleList);
        }
        userPrivateCirclesTitle.setVisibility( circleList.size() > 0 ? View.VISIBLE : View.GONE );
        userPrivateCirclesView.setVisibility( circleList.size() > 0 ? View.VISIBLE : View.GONE );

    }
}
