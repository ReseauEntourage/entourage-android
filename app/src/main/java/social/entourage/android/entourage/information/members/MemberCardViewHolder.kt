package social.entourage.android.entourage.information.members

import android.net.Uri
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.findFragment
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_entourage_information_member_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.tape.Events.OnUserViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.entourage.information.EntourageInformationFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.UtilsV7
import social.entourage.android.user.role.UserRoleView
import social.entourage.android.user.role.UserRolesFactory

/**
 * Created by mihaiionescu on 23/05/16.
 */
class MemberCardViewHolder(view: View) : BaseCardViewHolder(view) {
    private var userId = 0
    override fun bindFields() {
        itemView.setOnClickListener {
            if (userId != 0) EntBus.post(OnUserViewRequestedEvent(userId))
        }
    }

    override fun populate(data: TimestampedObject) {
        this.populate(data as EntourageUser)
    }

    fun populate(entourageUser: EntourageUser) {
        userId = entourageUser.userId
        itemView.tic_member_photo?.let {
            entourageUser.avatarURLAsString?.let {avatarURL ->
            Glide.with(it.context)
                    .load(Uri.parse(avatarURL))
                    .placeholder(R.drawable.ic_user_photo_small)
                    .circleCrop()
                    .into(it)
            } ?: run {
                Glide.with(it.context)
                        .load(R.drawable.ic_user_photo_small)
                        .into(it)
            }
        }
        // Partner logo
        itemView.tic_member_partner_logo?.let {
            entourageUser.partner?.smallLogoUrl?.let {partnerLogoURL->
                Glide.with(it.context)
                        .load(Uri.parse(partnerLogoURL))
                        .placeholder(R.drawable.partner_placeholder)
                        .circleCrop()
                        .into(it)
            } ?: run {
                it.setImageDrawable(null)
            }
        }
        itemView.tic_member_name?.text = entourageUser.displayName
        val roles = ArrayList<String>()
        entourageUser.groupRole?.let {
            roles.add(it)
        }
        entourageUser.communityRoles?.forEach { role->
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
            val userRoleView = UserRoleView(itemView.context)
            userRoleView.backgroundCornerRadius = 8f
            userRoleView.textSize = 12f
            userRoleView.gravity = Gravity.CENTER_VERTICAL
            userRoleView.setRole(userRole)
            itemView.tic_member_tags?.addView(userRoleView)
        }

        val partner = entourageUser.partner
        val role = entourageUser.partner_role_title

        if (partner != null || !role.isNullOrEmpty()) {
            itemView.ui_layout_bottom?.visibility = View.VISIBLE
            if (role != null && role.isNotEmpty()) {
                itemView.ui_tv_role?.text = "$role -"
                itemView.ui_tv_role?.visibility = View.VISIBLE
            }
            else {
                itemView.ui_tv_role?.visibility = View.GONE
            }

            var assoInfo = itemView.context.getText(R.string.info_asso_abo)
            if (entourageUser.partner?.isFollowing == true) {
                assoInfo = itemView.context.getText(R.string.info_asso_joined)
            }

            val assoStr = partner?.name + " " + assoInfo
            val colorId = ContextCompat.getColor(itemView.context, R.color.accent)
            val assoSpanner = UtilsV7.formatTextWithBoldSpanAndColor(colorId,true,assoStr, assoInfo.toString())

            itemView.ui_tv_bt_asso?.text = assoSpanner
            itemView.ui_tv_bt_asso?.setOnClickListener {
                entourageUser.partner?.id?.toInt()?.let { partnerId ->
                    (it.findFragment() as? EntourageInformationFragment)?.onShowDetailAssociation(partnerId)
                }
            }
        }
        else {
            itemView.ui_layout_bottom?.visibility = View.GONE
        }
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_entourage_information_member_card
    }
}