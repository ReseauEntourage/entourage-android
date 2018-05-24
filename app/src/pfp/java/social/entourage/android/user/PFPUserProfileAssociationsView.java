package social.entourage.android.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.user.role.UserRole;
import social.entourage.android.user.role.UserRolesFactory;

/**
 * PFP Circles in user profile view
 * Created by Mihai Ionescu on 24/05/2018.
 */
public class PFPUserProfileAssociationsView extends RelativeLayout implements UserAssociations {

    TextView userAssociationsTitle;
    RecyclerView userAssociationsView;

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
        inflate(getContext(), R.layout.layout_user_pfp_circles, this);

        userAssociationsTitle = findViewById(R.id.user_associations_title);
        userAssociationsView = findViewById(R.id.user_associations_view);
    }

    @Override
    public void initUserAssociations(final User user, final UserFragment userFragment) {
        ArrayList<String> circleList = new ArrayList();
        if (user != null) {
            ArrayList<String> userRoles = user.getRoles();
            if (userRoles != null && userRoles.size() > 0) {
                String role = userRoles.get(0);
                userAssociationsTitle.setText( UserRolesFactory.getInstance().isVisited(role) ? R.string.user_circles_title_visited: R.string.user_circles_title_visitor );
            }
        }
//        if (organizationsAdapter == null) {
//            userAssociationsView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//            organizationsAdapter = new UserOrganizationsAdapter(organizationList);
//            userAssociationsView.setAdapter(organizationsAdapter);
//
//            ItemClickSupport.addTo(userAssociationsView)
//                    .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
//                        @Override
//                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                            userFragment.onEditProfileClicked();
//                        }
//                    });
//        } else {
//            organizationsAdapter.setOrganizationList(organizationList);
//        }

        userAssociationsTitle.setVisibility( circleList.size() > 0 ? View.VISIBLE : View.GONE );
        userAssociationsView.setVisibility( circleList.size() > 0 ? View.VISIBLE : View.GONE );

    }
}
