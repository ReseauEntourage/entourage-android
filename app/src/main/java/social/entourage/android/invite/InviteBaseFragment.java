package social.entourage.android.invite;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.api.model.feed.FeedItem;
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

    protected String feedItemUUID;
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

    protected void setFeedData(String feedUUID, int feedItemType) {
        Bundle args = new Bundle();
        args.putString(FeedItem.KEY_FEEDITEM_UUID, feedUUID);
        args.putInt(FeedItem.KEY_FEEDITEM_TYPE, feedItemType);
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feedItemUUID = getArguments().getString(FeedItem.KEY_FEEDITEM_UUID);
            feedItemType = getArguments().getInt(FeedItem.KEY_FEEDITEM_TYPE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        inviteFriendsListener = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
