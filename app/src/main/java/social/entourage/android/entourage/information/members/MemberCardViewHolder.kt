package social.entourage.android.entourage.information.members

import android.net.Uri
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.entourage_information_member_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.tools.CropCircleTransformation
import social.entourage.android.user.role.RoleView
import social.entourage.android.user.role.UserRolesFactory
import java.util.*

/**
 * Created by mihaiionescu on 23/05/16.
 */
class MemberCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    override fun bindFields() {
        itemView.setOnClickListener {
            if (userId != 0) instance.post(OnUserViewRequestedEvent(userId))
        }
    }

    override fun populate(data: TimestampedObject) {
        this.populate(data as EntourageUser)
    }

    fun populate(entourageUser: EntourageUser) {
        userId = entourageUser.userId
        itemView.tic_member_photo?.let {
            entourageUser.avatarURLAsString?.let {avatarURL ->
            Picasso.get().load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .transform(CropCircleTransformation())
                    .into(it)
            } ?: run {
                it.setImageResource(R.drawable.ic_user_photo_small)
            }
        }
        // Partner logo
        itemView.tic_member_partner_logo?.let {
            entourageUser.partner?.smallLogoUrl?.let {partnerLogoURL->
                Picasso.get()
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .transform(CropCircleTransformation())
                        .into(it)
            } ?: run {
                it.setImageDrawable(null)
            }
        }
        itemView.tic_member_name?.text = entourageUser.displayName
        val roles = ArrayList<String>()
        if (entourageUser.groupRole != null) {
            roles.add(entourageUser.groupRole)
        }
        for (role in entourageUser.communityRoles) {
            if (role != entourageUser.groupRole) {
                roles.add(role)
            }
        }
        itemView.tic_member_tags?.removeAllViews()
        for (role in roles) {
            val userRole = UserRolesFactory.findByName(role)
            if (userRole == null || !userRole.isVisible) {
                continue
            }
            val roleView = RoleView(itemView.context)
            roleView.setRole(userRole)
            itemView.tic_member_tags?.addView(roleView)
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.entourage_information_member_card
    }
}