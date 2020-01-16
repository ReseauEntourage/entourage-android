package social.entourage.android.entourage.my;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.InvitationList;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.LoaderCardItem;
import social.entourage.android.base.EntourageBaseAdapter;
import social.entourage.android.invite.view.InvitationListViewHolder;
import social.entourage.android.map.LoaderCardViewHolder;
import social.entourage.android.entourage.EntourageViewHolder;
import social.entourage.android.tour.TourViewHolder;
import social.entourage.android.base.ViewHolderFactory;

/**
 * Created by mihaiionescu on 09/08/16.
 */
public class MyEntouragesAdapter extends EntourageBaseAdapter {

    private InvitationList invitationList;
    private LoaderCallback loaderCallback;

    public MyEntouragesAdapter() {

        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
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
        //items.add(invitationList);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {//header position
            ((InvitationListViewHolder)holder).populate(invitationList);
            return;
        }
        super.onBindViewHolder(holder, position);
        if(position>=getPositionOffset()) {
            TimestampedObject item = items.get(position - getPositionOffset());
            if (item.getType() == TimestampedObject.LOADER_CARD) {
                if (loaderCallback != null) {
                    loaderCallback.loadMoreItems();
                }
            }
        }
    }

    @Override
    protected int getPositionOffset() {
        return 1;
    }

    void setLoaderCallback(LoaderCallback loaderCallback) {
        this.loaderCallback = loaderCallback;
    }

    public void setInvitations(List<Invitation> invitations) {
        invitationList.setInvitationList(invitations);
        notifyItemChanged(0);
        EntourageApplication.get().updateStorageInvitationCount(invitations.size());
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


    @Override
    public int getDataItemCount() {
        return (items == null?0:items.size()) + (invitationList.getInvitationList()==null?0:invitationList.getInvitationList().size());
    }
}
