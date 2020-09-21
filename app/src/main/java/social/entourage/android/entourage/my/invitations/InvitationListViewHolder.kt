package social.entourage.android.entourage.my.invitations

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_invitation_list_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.InvitationList
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import java.util.*

/**
 * Created by Mihai Ionescu on 19/10/2017.
 */
class InvitationListViewHolder(view: View) : BaseCardViewHolder(view) {
    private lateinit var invitationsAdapter: InvitationsAdapter

    override fun bindFields() {
        //invitationsView = itemView.invitation_list_recycler_view)
        itemView.invitation_list_recycler_view?.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        invitationsAdapter = InvitationsAdapter()
        itemView.invitation_list_recycler_view?.adapter = invitationsAdapter
    }

    override fun populate(data: TimestampedObject) {
        val invitationList: InvitationList = data as InvitationList
        val list = ArrayList(invitationList.invitationList)
        invitationsAdapter.removeAll()
        invitationsAdapter.addItems(list)
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_invitation_list_card
    }
}