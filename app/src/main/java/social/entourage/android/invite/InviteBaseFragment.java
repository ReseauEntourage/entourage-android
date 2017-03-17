package social.entourage.android.invite;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteBaseFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    protected long feedItemId;
    protected int feedItemType;

    @Inject
    protected InvitePresenter presenter;

    protected InviteFriendsListener inviteFriendsListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public InviteBaseFragment() {
        // Required empty public constructor
    }

    protected void setFeedData(long feedId, int feedItemType) {
        Bundle args = new Bundle();
        args.putLong(FeedItem.KEY_FEEDITEM_ID, feedId);
        args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType);
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItemId = getArguments().getLong(FeedItem.KEY_FEEDITEM_ID);
            feedItemType = getArguments().getInt(FeedItem.KEY_FEEDITEM_TYPE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        inviteFriendsListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerInviteComponent.builder()
                .entourageComponent(entourageComponent)
                .inviteModule(new InviteModule(this))
                .build()
                .inject(this);
    }

    // ----------------------------------
    // LISTENER
    // ----------------------------------

    public void setInviteFriendsListener(final InviteFriendsListener inviteFriendsListener) {
        this.inviteFriendsListener = inviteFriendsListener;
    }

    // ----------------------------------
    // PRESENTER CALLBACKS
    // ----------------------------------

    protected void onInviteSent(boolean success) {

    }

}
