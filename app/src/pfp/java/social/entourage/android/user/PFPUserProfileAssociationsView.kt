package social.entourage.android.user

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.map.BaseEntourage
import social.entourage.android.api.model.map.UserMembership
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.base.ItemClickSupport
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.user.membership.UserMembershipsAdapter
import social.entourage.android.user.role.UserRolesFactory
import java.util.*

/**
 * PFP Circles in user profile view
 * Created by Mihai Ionescu on 24/05/2018.
 */
class PFPUserProfileAssociationsView : RelativeLayout, UserAssociations {
    private var userNeighborhoodsTitle: TextView? = null
    private var userNeighborhoodsView: RecyclerView? = null
    private var userNeighborhoodsAdapter: UserMembershipsAdapter? = null
    private var userPrivateCirclesTitle: TextView? = null
    private var userPrivateCirclesView: RecyclerView? = null
    private var privateCircleAdapter: UserMembershipsAdapter? = null

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.layout_user_pfp_membership, this)
        userNeighborhoodsTitle = findViewById(R.id.user_neighborhoods_title)
        userNeighborhoodsView = findViewById(R.id.user_neighborhoods_view)
        userPrivateCirclesTitle = findViewById(R.id.user_private_circles_title)
        userPrivateCirclesView = findViewById(R.id.user_private_circles_view)
    }

    override fun initUserAssociations(user: User, userFragment: UserFragment) {
        val neighborhoodList: ArrayList<UserMembership?> = user.getMemberships(BaseEntourage.TYPE_NEIGHBORHOOD)
        val circleList: ArrayList<UserMembership?> = user.getMemberships(BaseEntourage.TYPE_PRIVATE_CIRCLE)
        val userRoles = user.roles
        if (userRoles != null && userRoles.size > 0) {
            val role = userRoles[0]
            userPrivateCirclesTitle!!.setText(if (UserRolesFactory.isVisited(role)) R.string.user_circles_title_visited else R.string.user_circles_title_visitor)
        }
        if (userNeighborhoodsAdapter == null) {
            userNeighborhoodsView!!.layoutManager = LinearLayoutManager(context)
            userNeighborhoodsAdapter = UserMembershipsAdapter(neighborhoodList, BaseEntourage.TYPE_NEIGHBORHOOD)
            userNeighborhoodsView!!.adapter = userNeighborhoodsAdapter
            ItemClickSupport.addTo(userNeighborhoodsView)
                    .setOnItemClickListener { recyclerView, position, v ->
                        val userMembership = userNeighborhoodsAdapter!!.getItemAt(position)
                        if (userMembership != null) {
                            instance.post(OnFeedItemInfoViewRequestedEvent(BaseEntourage.ENTOURAGE_CARD, userMembership.membershipUUID, null))
                        }
                    }
        } else {
            userNeighborhoodsAdapter!!.setMembershipList(neighborhoodList)
        }
        userNeighborhoodsTitle!!.visibility = if (neighborhoodList.size > 0) View.VISIBLE else View.GONE
        userNeighborhoodsView!!.visibility = if (neighborhoodList.size > 0) View.VISIBLE else View.GONE
        if (privateCircleAdapter == null) {
            userPrivateCirclesView!!.layoutManager = LinearLayoutManager(context)
            privateCircleAdapter = UserMembershipsAdapter(circleList, BaseEntourage.TYPE_PRIVATE_CIRCLE)
            userPrivateCirclesView!!.adapter = privateCircleAdapter
            ItemClickSupport.addTo(userPrivateCirclesView)
                    .setOnItemClickListener { recyclerView, position, v ->
                        val userMembership = privateCircleAdapter!!.getItemAt(position)
                        if (userMembership != null) {
                            instance.post(OnFeedItemInfoViewRequestedEvent(BaseEntourage.ENTOURAGE_CARD, userMembership.membershipUUID, null))
                        }
                    }
        } else {
            privateCircleAdapter!!.setMembershipList(circleList)
        }
        userPrivateCirclesTitle!!.visibility = if (circleList.size > 0) View.VISIBLE else View.GONE
        userPrivateCirclesView!!.visibility = if (circleList.size > 0) View.VISIBLE else View.GONE
    }
}