package social.entourage.android.tools.view.countrycodepicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_code_picker_tile.view.*
import social.entourage.android.R
import social.entourage.android.tools.view.countrycodepicker.CountryCodeAdapter.CountryCodeViewHolder
import java.util.*

internal class CountryCodeAdapter(private val mCountries: List<Country?>, private val mCountryCodePicker: CountryCodePicker, private val mCallback: Callback) : RecyclerView.Adapter<CountryCodeViewHolder>() {

    internal interface Callback {
        fun onItemCountrySelected(country: Country?)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): CountryCodeViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val rootView = inflater.inflate(R.layout.layout_code_picker_tile, viewGroup, false)
        return CountryCodeViewHolder(rootView)
    }

    override fun onBindViewHolder(viewHolder: CountryCodeViewHolder, i: Int) {
        val position = viewHolder.adapterPosition
        viewHolder.setCountry(mCountries[position])
        viewHolder.itemView.setOnClickListener { mCallback.onItemCountrySelected(mCountries[position]) }
    }

    override fun getItemCount(): Int {
        return mCountries.size
    }

    internal inner class CountryCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal fun setCountry(country: Country?) {
            if (country != null) {
                itemView.preference_divider_view?.visibility = View.GONE
                itemView.country_name_tv?.visibility = View.VISIBLE
                itemView.code_tv?.visibility = View.VISIBLE
                itemView.country_name_tv?.text = itemView.context
                        .getString(R.string.country_name_and_code, country.name,
                            country.iso.uppercase(Locale.getDefault())
                        )
                itemView.code_tv?.text = itemView.context.getString(R.string.phone_code, country.phoneCode)
                if (mCountryCodePicker.typeFace != null) {
                    itemView.code_tv?.typeface = mCountryCodePicker.typeFace
                    itemView.country_name_tv?.typeface = mCountryCodePicker.typeFace
                }
                if (mCountryCodePicker.textColor != CountryCodePicker.defaultContentColor) {
                    val color = mCountryCodePicker.textColor
                    itemView.code_tv?.setTextColor(color)
                    itemView.country_name_tv?.setTextColor(color)
                }
            } else {
                itemView.preference_divider_view?.visibility = View.VISIBLE
                itemView.country_name_tv?.visibility = View.GONE
                itemView.code_tv?.visibility = View.GONE
            }
        }
    }
}