package social.entourage.android.user

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_user_entourage_associations.view.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseOrganization
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.User
import social.entourage.android.api.tape.Events.OnFeedItemInfoViewRequestedEvent
import social.entourage.android.tools.ItemClickSupport
import social.entourage.android.tools.EntBus
import social.entourage.android.user.membership.UserMembershipsAdapter
import java.util.*

/**
 * Created by Mihai Ionescu on 24/05/2018.
 */
class UserAssociationsLayout : RelativeLayout {
    private var organizationsAdapter: UserOrganizationsAdapter? = null
    private var userNeighborhoodsAdapter: UserMembershipsAdapter? = null

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    @RequiresApi(21)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        View.inflate(context, R.layout.layout_user_entourage_associations, this)
    }

    fun initUserAssociations(user: User, userFragment: UserFragment) {
        val organizationList: MutableList<BaseOrganization> = ArrayList()
        user.partner?.let {
            organizationList.add(it)
        }
        user.organization?.let {
            organizationList.add(it)
        }
        organizationsAdapter?.setOrganizationList(organizationList) ?: run  {
            user_associations_view?.let {
                it.layoutManager = LinearLayoutManager(context)
                organizationsAdapter = UserOrganizationsAdapter(organizationList)
                it.adapter = organizationsAdapter
                ItemClickSupport.addTo(it).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                    override fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?) {
                        userFragment.onEditProfileClicked()
                    }
                })
            }
        }
        user_associations_title?.visibility = if (organizationList.size > 0) View.VISIBLE else View.GONE
        user_associations_view?.visibility = if (organizationList.size > 0) View.VISIBLE else View.GONE
        val userMembershipList = user.getMemberships(BaseEntourage.GROUPTYPE_NEIGHBORHOOD)
        userNeighborhoodsAdapter?.setMembershipList(userMembershipList) ?: run {
            user_neighborhoods_view?.let { view ->
                view.layoutManager = LinearLayoutManager(context)
                userNeighborhoodsAdapter = UserMembershipsAdapter(userMembershipList, BaseEntourage.GROUPTYPE_NEIGHBORHOOD)
                ItemClickSupport.addTo(view)
                        .setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
                            override fun onItemClicked(recyclerView: RecyclerView?, position: Int, v: View?) {
                                userNeighborhoodsAdapter?.getItemAt(position)?.let { userMembership ->
                                    EntBus.post(OnFeedItemInfoViewRequestedEvent(TimestampedObject.ENTOURAGE_CARD, userMembership.membershipUUID, null))
                                }
                            }
                        })
            }
        }
        user_neighborhoods_title?.visibility = if (userMembershipList.size > 0) View.VISIBLE else View.GONE
        user_neighborhoods_view?.visibility = if (userMembershipList.size > 0) View.VISIBLE else View.GONE
    }
}