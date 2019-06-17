package social.entourage.android.map.entourage.my;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.InvitationList;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.LoaderCardItem;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.invite.view.InvitationListViewHolder;
import social.entourage.android.map.LoaderCardViewHolder;
import social.entourage.android.map.entourage.EntourageViewHolder;
import social.entourage.android.map.tour.TourViewHolder;
import social.entourage.android.base.ViewHolderFactory;

/**
 * Created by mihaiionescu on 09/08/16.
 */
public class MyEntouragesAdapter extends EntourageBaseAdapter {

    private InvitationList invitationList;
    private LoaderCallback loaderCallback;

    public MyEntouragesAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.INVITATION_LIST,
                new ViewHolderFactory.ViewHolderType(InvitationListViewHolder.class, InvitationListViewHolder.getLayoutResource())
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_CARD,
                new ViewHolderFactory.ViewHolderType(TourViewHolder.class, R.layout.layout_myentourages_card)
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                new ViewHolderFactory.ViewHolderType(EntourageViewHolder.class, R.layout.layout_myentourages_card)
        );

        viewHolderFactory.registerViewHolder(
                TimestampedObject.LOADER_CARD,
                new ViewHolderFactory.ViewHolderType(LoaderCardViewHolder.class, R.layout.layout_loader_card)
        );

        setHasStableIds(false);

        invitationList = new InvitationList();
        items.add(invitationList);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        TimestampedObject item = items.get(position - getPositionOffset());
        if (item.getType() == TimestampedObject.LOADER_CARD) {
            if (loaderCallback != null) {
                loaderCallback.loadMoreItems();
            }
        }
    }

    void setLoaderCallback(LoaderCallback loaderCallback) {
        this.loaderCallback = loaderCallback;
    }

    public void setInvitations(List<Invitation> invitations) {
        invitationList.setInvitationList(invitations);
        notifyItemChanged(0);
    }

    void addLoader() {
        addCardInfo(new LoaderCardItem());
    }

    void removeLoader() {
        int lastPosition = items.size() - 1;
        if (!items.isEmpty() && items.get(lastPosition).getType() == TimestampedObject.LOADER_CARD) {
            items.remove(lastPosition);
            notifyItemRemoved(lastPosition + getPositionOffset());
        }
    }

    public interface LoaderCallback {
        void loadMoreItems();
    }
}
