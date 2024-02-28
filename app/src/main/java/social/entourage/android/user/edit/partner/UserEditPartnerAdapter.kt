package social.entourage.android.user.edit.partner

import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.CompoundButton
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.Partner
import social.entourage.android.databinding.LayoutEditPartnerBinding // Assurez-vous que ceci correspond à votre nom de fichier de layout.

class UserEditPartnerAdapter : BaseAdapter() {
    var partnerList: List<Partner>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var selectedPartnerPosition = AdapterView.INVALID_POSITION
    private val onCheckedChangeListener = OnCheckedChangeListener()

    override fun getCount() = partnerList?.size ?: 0

    override fun getItem(position: Int) = partnerList?.get(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: LayoutEditPartnerBinding
        if (convertView == null) {
            binding = LayoutEditPartnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.root.tag = PartnerViewHolder(binding, onCheckedChangeListener)
        } else {
            binding = LayoutEditPartnerBinding.bind(convertView)
        }

        val viewHolder = binding.root.tag as PartnerViewHolder
        viewHolder.bind(getItem(position))

        return binding.root
    }

    inner class PartnerViewHolder(private val binding: LayoutEditPartnerBinding, checkboxListener: OnCheckedChangeListener?) {
        var partner: Partner? = null

        init {
            binding.partnerCheckbox.setOnCheckedChangeListener(checkboxListener)
            binding.partnerName.setOnClickListener {
                binding.partnerCheckbox.let {
                    it.isChecked = !it.isChecked
                    checkboxListener?.onCheckedChanged(it, it.isChecked)
                }
            }
        }

        fun bind(partner: Partner?) {
            this.partner = partner
            partner?.let {
                binding.partnerName.text = it.name
                binding.partnerName.setTypeface(null, if (it.isDefault) Typeface.BOLD else Typeface.NORMAL)
                it.largeLogoUrl?.let { logoUrl ->
                    Glide.with(binding.partnerLogo.context).load(Uri.parse(logoUrl))
                        .placeholder(R.drawable.partner_placeholder).into(binding.partnerLogo)
                } ?: run {
                    binding.partnerLogo.setImageDrawable(ResourcesCompat.getDrawable(binding.root.resources, R.drawable.partner_placeholder, null))
                }
                binding.partnerCheckbox.tag = null // Préparer pour la réutilisation
                binding.partnerCheckbox.isChecked = it.isDefault
                binding.partnerCheckbox.tag = partner // Restaurer le tag
            }
        }
    }

    inner class OnCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val position = buttonView.tag as? Int ?: return
            if (selectedPartnerPosition != position) {
                getItem(selectedPartnerPosition)?.isDefault = false
            }
            getItem(position)?.let {
                it.isDefault = isChecked
                selectedPartnerPosition = if (selectedPartnerPosition != position || it.isDefault) position else AdapterView.INVALID_POSITION
                notifyDataSetChanged()
            }
        }
    }
}
