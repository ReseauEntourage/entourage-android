package social.entourage.android.entourage.my

import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.InvitationList
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.LoaderCardItem
import social.entourage.android.base.EntourageBaseAdapter
import social.entourage.android.base.LoaderCardViewHolder
import social.entourage.android.base.ViewHolderFactory.ViewHolderType
import social.entourage.android.entourage.EntourageViewHolder
import social.entourage.android.entourage.my.invitations.InvitationListViewHolder
import social.entourage.android.tour.TourViewHolder

/**
 * Created by mihaiionescu on 09/08/16.
 */
class MyEntouragesAdapter : EntourageBaseAdapter() {
    private val invitationListObject: InvitationList
    private var loaderCallback: LoaderCallback? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) { //header position
            (holder as InvitationListViewHolder).populate(invitationListObject)
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

    fun removeOldInvitations(invitationList: List<Invitation?>) {
        val tempList:List<Invitation>  = invitationListObject.invitationList.toList()
        tempList.forEach {
            if(!invitationList.contains(it)) {
                invitationListObject.removeFromInvitationList(it)
                notifyItemChanged(0)
                EntourageApplication.get().updateStorageInvitationCount(invitationListObject.invitationList.size)
            }
        }
    }

    fun addInvitation(invitation: Invitation) {
        var shouldAddInvitation = true
        invitationListObject.invitationList.forEach {
            if(it.id == invitation.id) {
                shouldAddInvitation = false
                return@forEach
            }
        }
        if(shouldAddInvitation) {
            invitationListObject.addToInvitationList(invitation)
            notifyItemChanged(0)
            EntourageApplication.get().updateStorageInvitationCount(invitationListObject.invitationList.size)
        }
    }

    fun updateInvitation(entourage: BaseEntourage, status: String) {
        var shouldAddInvitation = (status == Invitation.STATUS_PENDING)
        val tempList: List<Invitation> = invitationListObject.invitationList.toList()
        tempList.forEach {
           if(it.entourageId.toLong() == entourage.id) {
               if(shouldAddInvitation) {
                   shouldAddInvitation = false
               } else {
                   invitationListObject.removeFromInvitationList(it)
                   notifyItemChanged(0)
                   EntourageApplication.get().updateStorageInvitationCount(invitationListObject.invitationList.size)
               }
           }
        }
        if(shouldAddInvitation) {
            //TODO
        }
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
        return (if (items == null) 0 else items.size) + invitationListObject.invitationList.size
    }

    init {
        viewHolderFactory.registerViewHolder(
                TimestampedObject.TOP_VIEW,
                ViewHolderType(InvitationListViewHolder::class.java, InvitationListViewHolder.layoutResource)
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
        invitationListObject = InvitationList()
        //items.add(invitationList);
    }
}