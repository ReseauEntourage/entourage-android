package social.entourage.android.tools.view.countrycodepicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutCodePickerTileBinding
import social.entourage.android.tools.view.countrycodepicker.CountryCodeAdapter.CountryCodeViewHolder

internal class CountryCodeAdapter(
    private val mCountries: List<Country?>,
    private val mCountryCodePicker: CountryCodePicker,
    private val mCallback: Callback
) : RecyclerView.Adapter<CountryCodeViewHolder>() {

    internal interface Callback {
        fun onItemCountrySelected(country: Country?)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CountryCodeViewHolder {
        val binding = LayoutCodePickerTileBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return CountryCodeViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: CountryCodeViewHolder, i: Int) {
        val country = mCountries[viewHolder.adapterPosition]
        viewHolder.setCountry(country)
        viewHolder.binding.root.setOnClickListener { mCallback.onItemCountrySelected(country) }
    }

    override fun getItemCount(): Int = mCountries.size

    internal inner class CountryCodeViewHolder(val binding: LayoutCodePickerTileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setCountry(country: Country?) {
            with(binding) {
                if (country != null) {
                    preferenceDividerView.visibility = View.GONE
                    countryNameTv.visibility = View.VISIBLE
                    codeTv.visibility = View.VISIBLE
                    countryNameTv.text = itemView.context.getString(R.string.country_name_and_code, country.name, country.flagTxt)
                    codeTv.text = itemView.context.getString(R.string.phone_code, country.phoneCode)
                    mCountryCodePicker.typeFace?.let {
                        codeTv.typeface = it
                        countryNameTv.typeface = it
                    }
                    if (mCountryCodePicker.textColor != CountryCodePicker.defaultContentColor) {
                        val color = mCountryCodePicker.textColor
                        codeTv.setTextColor(color)
                        countryNameTv.setTextColor(color)
                    }
                } else {
                    preferenceDividerView.visibility = View.VISIBLE
                    countryNameTv.visibility = View.GONE
                    codeTv.visibility = View.GONE
                }
            }
        }
    }
}
