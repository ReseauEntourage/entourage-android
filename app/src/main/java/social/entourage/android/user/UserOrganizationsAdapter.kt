package social.entourage.android.user

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_user_profile_organization.view.*
import social.entourage.android.R
import social.entourage.android.api.model.BaseOrganization
import social.entourage.android.api.model.Partner
import social.entourage.android.api.tape.Events.OnPartnerViewRequestedEvent
import social.entourage.android.tools.BusProvider

/**
 * Created by mihaiionescu on 24/03/16.
 */
class UserOrganizationsAdapter(private var organizationList: List<BaseOrganization>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class OrganizationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var partner: Partner? = null

        init {
            itemView.ui_layout_click?.setOnClickListener {
                partner?.let {
                    BusProvider.instance.post(OnPartnerViewRequestedEvent(it))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.layout_user_profile_organization, parent, false)
        return OrganizationViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        organizationList?.get(position)?.let { organization ->
            val organizationViewHolder = holder as OrganizationViewHolder
            organizationViewHolder.itemView.organization_name.text = organization.name
            organizationViewHolder.itemView.organization_type.text = organization.getTypeAsString(organizationViewHolder.itemView.context)
            organization.largeLogoUrl?.let {organizationLogo ->
                Picasso.get()
                        .load(Uri.parse(organizationLogo))
                        .into(organizationViewHolder.itemView.organization_logo!!)
            } ?: run {
                organizationViewHolder.itemView.organization_logo?.setImageDrawable(null)
            }
            organizationViewHolder.partner = (organization as? Partner)
            organizationViewHolder.itemView.organization_separator.visibility = if (position == organizationList!!.size - 1) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return organizationList?.size ?: 0
    }

    fun setOrganizationList(organizationList: List<BaseOrganization>?) {
        this.organizationList = organizationList
        notifyDataSetChanged()
    }

}