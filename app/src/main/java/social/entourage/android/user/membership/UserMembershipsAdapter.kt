package social.entourage.android.user.membership

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_membership_item.view.*
import social.entourage.android.R
import social.entourage.android.api.model.UserMembership
import java.util.*

/**
 * Created by Mihai Ionescu on 25/05/2018.
 */
class UserMembershipsAdapter(private var membershipList: ArrayList<UserMembership>?, private val membershipType: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class UserMembershipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun populate(membership: UserMembership) {
            itemView.membership_icon?.setImageDrawable(membership.getIconDrawable(itemView.context))
            itemView.membership_title?.text = membership.membershipTitle
            itemView.membership_count?.text = itemView.resources.getString(R.string.tour_cell_numberOfPeople, membership.numberOfPeople)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_membership_item, parent, false)
        return UserMembershipViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        membershipList?.get(position)?.let {
            (holder as UserMembershipViewHolder).populate(it)
        }
    }

    override fun getItemCount(): Int {
        return membershipList?.size ?: 0
    }

    fun setMembershipList(membershipList: ArrayList<UserMembership>) {
        this.membershipList = membershipList
        this.membershipList?.forEach { userMembership ->
            userMembership.type = membershipType
        }
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): UserMembership? {
        return membershipList?.let { return if (position < 0 || position >= it.size) null else it[position]}
    }

    init {
        membershipList?.forEach { userMembership ->
            userMembership.type = membershipType
        }
    }
}