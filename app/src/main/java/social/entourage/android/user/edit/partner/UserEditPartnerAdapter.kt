package social.entourage.android.user.edit.partner

import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.CompoundButton
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_edit_partner.view.*
import social.entourage.android.R
import social.entourage.android.api.model.Partner

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
            currentView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_edit_partner, viewGroup, false)
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
                    Glide.with(it.context)
                            .load(Uri.parse(partnerLogo))
                            .placeholder(R.drawable.partner_placeholder)
                            .into(it)
                }
            } ?: run  {
                currentView.partner_logo?.let {
                    Glide.with(it.context)
                            .load(R.drawable.partner_placeholder)
                            .into(it)
                }
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
        partnerList?.let { list ->
            if (position >= 0 && position < list.size) return list[position]
        }
        return null
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
                selectedPartnerPosition = when {
                    selectedPartnerPosition != position -> position
                    partner.isDefault -> position
                    else -> AdapterView.INVALID_POSITION
                }
            }

            // refresh the list view
            notifyDataSetChanged()
        }
    }
}