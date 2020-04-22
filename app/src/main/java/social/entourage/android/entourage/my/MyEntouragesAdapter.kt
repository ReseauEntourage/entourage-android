package social.entourage.android.entourage.my

import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.InvitationList
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.LoaderCardItem
import social.entourage.android.base.EntourageBaseAdapter
import social.entourage.android.base.LoaderCardViewHolder
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.entourage.EntourageViewHolder
import social.entourage.android.invite.view.InvitationListViewHolder
import social.entourage.android.tour.TourViewHolder

/**
 * Created by mihaiionescu on 09/08/16.
 */
class MyEntouragesAdapter : EntourageBaseAdapter() {
    private val invitationList: InvitationList
    private var loaderCallback: LoaderCallback? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) { //header position
            (holder as InvitationListViewHolder).populate(invitationList)
            return
        }
        super.onBindViewHolder(holder, position)
        if ((position >= positionOffset) && (items[position - positionOffset].type == TimestampedObject.LOADER_CARD)) {
            loaderCallback?.loadMoreItems()
        }
    }

    override fun getPositionOffset(): Int {
        return 1
    }

    fun setLoaderCallback(loaderCallback: LoaderCallback?) {
        this.loaderCallback = loaderCallback
    }

    fun setInvitations(invitations: List<Invitation?>) {
        invitationList.invitationList = invitations
        notifyItemChanged(0)
        EntourageApplication.get().updateStorageInvitationCount(invitations.size)
    }

    fun addLoader() {
        addCardInfo(LoaderCardItem())
    }

    fun removeLoader() {
        val lastPosition = items.size - 1
        if (items.isNotEmpty() && items[lastPosition].type == TimestampedObject.LOADER_CARD) {
            items.removeAt(lastPosition)
            notifyItemRemoved(lastPosition + positionOffset)
        }
    }

    interface LoaderCallback {
        fun loadMoreItems()
    }

    override fun getDataItemCount(): Int {
        return (if (items == null) 0 else items.size) + if (invitationList.invitationList == null) 0 else invitationList.invitationList.size
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                ViewHolderType(InvitationListViewHolder::class.java, InvitationListViewHolder.getLayoutResource())
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOUR_CARD,
                ViewHolderType(TourViewHolder::class.java, R.layout.layout_myentourages_card)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.ENTOURAGE_CARD,
                ViewHolderType(EntourageViewHolder::class.java, R.layout.layout_myentourages_card)
        )
        viewHolderFactory.registerViewHolder(
                TimestampedObject.LOADER_CARD,
                ViewHolderType(LoaderCardViewHolder::class.java, R.layout.layout_loader_card)
        )
        setHasStableIds(false)
        invitationList = InvitationList()
        //items.add(invitationList);
    }
}