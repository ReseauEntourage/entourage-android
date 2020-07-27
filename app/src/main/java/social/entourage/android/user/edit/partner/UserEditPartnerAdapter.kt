package social.entourage.android.user.edit.partner

import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_edit_partner.view.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.api.tape.Events.OnPartnerViewRequestedEvent
import social.entourage.android.tools.BusProvider.instance

/**
 * Created by mihaiionescu on 16/01/2017.
 */
class UserEditPartnerAdapter : BaseAdapter() {
    class PartnerViewHolder(v: View, checkboxListener: OnCheckedChangeListener?) {
        var partner: Partner? = null

        init {
            v.partner_checkbox?.setOnCheckedChangeListener(checkboxListener)
            v.partner_name?.setOnClickListener {
                if (checkboxListener != null) {
                    v.partner_checkbox?.let {
                        it.isChecked = !it.isChecked
                        checkboxListener.onCheckedChanged(it, it.isChecked)
                    }
                }
            }
            v.partner_logo?.setOnClickListener {
                partner?.let {
                    instance.post(OnPartnerViewRequestedEvent(it))
                }
            }
        }
    }

    var selectedPartnerPosition = AdapterView.INVALID_POSITION
    private val onCheckedChangeListener = OnCheckedChangeListener()

    var partnerList: List<Partner>? = null
        set(partnerList) {
            field = partnerList
            notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return partnerList?.size ?: 0
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val currentView: View
        val viewHolder: PartnerViewHolder
        if (view == null) {
            currentView = LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_edit_partner, viewGroup, false)
            viewHolder = PartnerViewHolder(currentView, onCheckedChangeListener)
            currentView.tag = viewHolder
        } else {
            currentView = view
            viewHolder = currentView.tag as PartnerViewHolder
        }

        // Populate the view
        val partner = getItem(position)
        if (partner != null) {
            currentView.partner_name?.text = partner.name
            if (partner.isDefault) {
                currentView.partner_name?.setTypeface(null, Typeface.BOLD)
            } else {
                currentView.partner_name?.setTypeface(null, Typeface.NORMAL)
            }
            partner.largeLogoUrl?.let { partnerLogo ->
                currentView.partner_logo?.let {
                    Picasso.get()
                            .load(Uri.parse(partnerLogo))
                            .placeholder(R.drawable.partner_placeholder)
                            .into(it)
                }
            } ?: run  {
                currentView.partner_logo?.setImageResource(R.drawable.partner_placeholder)
            }

            // set the tag to null so that oncheckedchangelistener exits when populating the view
            currentView.partner_checkbox?.tag = null
            // set the check state
            currentView.partner_checkbox?.isChecked = partner.isDefault
            // set the tag to the item position
            currentView.partner_checkbox?.tag = position

            // set the partner id
            viewHolder.partner = partner
        }
        return currentView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Partner? {
        return if (partnerList != null && position >= 0 && position < partnerList!!.size) partnerList!![position] else null
    }

    inner class OnCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
            // if no tag, exit
            if (compoundButton.tag == null) {
                return
            }
            // get the position
            val position = compoundButton.tag as Int
            // unset the previously selected partner, if different than the current
            if (selectedPartnerPosition != position) {
                getItem(selectedPartnerPosition)?.let { oldPartner->
                    oldPartner.isDefault = false
                }
            }

            // save the state
            getItem(position)?.let { partner->
                partner.isDefault = isChecked
                if (selectedPartnerPosition != position) {
                    selectedPartnerPosition = position
                } else {
                    selectedPartnerPosition = if (partner.isDefault) position else AdapterView.INVALID_POSITION
                }
            }

            // refresh the list view
            notifyDataSetChanged()
        }
    }
}