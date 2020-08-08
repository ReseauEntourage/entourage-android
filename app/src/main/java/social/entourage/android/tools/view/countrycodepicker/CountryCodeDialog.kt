package social.entourage.android.tools.view.countrycodepicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_code_picker_dialog.*
import social.entourage.android.R
import java.util.*
import kotlin.math.roundToInt

/**
 * Dialog for selecting Country.
 */
internal class CountryCodeDialog(private val mCountryCodePicker: CountryCodePicker) : Dialog(mCountryCodePicker.context) {
    private var mFilteredCountries: List<Country?>? = null
    private var mInputMethodManager: InputMethodManager? = null
    private var mAdapter: CountryCodeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_code_picker_dialog)
        setupData()
    }

    private fun setupData() {
        mCountryCodePicker.typeFace?.let { typeface->
            title_tv?.typeface = typeface
            search_edt?.typeface = typeface
            no_result_tv?.typeface = typeface
        }
        if (mCountryCodePicker.mBackgroundColor != CountryCodePicker.defaultBackgroundColor) {
            dialog_rly?.setBackgroundColor(mCountryCodePicker.mBackgroundColor)
        }
        if (mCountryCodePicker.textColor != CountryCodePicker.defaultContentColor) {
            val color = mCountryCodePicker.textColor
            title_tv?.setTextColor(color)
            no_result_tv?.setTextColor(color)
            search_edt?.setTextColor(color)
            search_edt?.setHintTextColor(adjustAlpha(color, 0.7f))
        }
        mCountryCodePicker.refreshCustomMasterList()
        mCountryCodePicker.refreshPreferredCountries()
        val callback: CountryCodeAdapter.Callback = object : CountryCodeAdapter.Callback {
            override fun onItemCountrySelected(country: Country?) {
                mCountryCodePicker.selectedCountry = country
                //if (view != null && mCountries.get(position) != null) {
                search_edt?.windowToken?.let {
                    mInputMethodManager?.hideSoftInputFromWindow(it , 0)
                }
                dismiss()
            }
        }
        mFilteredCountries = getFilteredCountries("").also {
            mAdapter = CountryCodeAdapter(it, mCountryCodePicker, callback)
        }
        if (!mCountryCodePicker.isSelectionDialogShowSearch) {
            country_dialog_rv?.let {
                val params = it.layoutParams as RelativeLayout.LayoutParams
                params.height = RecyclerView.LayoutParams.WRAP_CONTENT
                it.layoutParams = params
            }
        }
        country_dialog_rv?.layoutManager = LinearLayoutManager(context)
        country_dialog_rv?.adapter = mAdapter
        mInputMethodManager = mCountryCodePicker.context
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setSearchBar()
    }

    fun reShow() {
        setupData()
        show()
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setSearchBar() {
        if (mCountryCodePicker.isSelectionDialogShowSearch) {
            setTextWatcher()
        } else {
            search_edt?.visibility = View.GONE
        }
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private fun setTextWatcher() {
        search_edt?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                applyQuery(s.toString())
            }
        })
        if (mCountryCodePicker.isKeyboardAutoPopOnSearch) {
            mInputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private fun applyQuery(query: String) {
        var newQuery = query.toLowerCase()
        no_result_tv?.visibility = View.GONE

        //if query started from "+" ignore it
        if (newQuery.isNotEmpty() && newQuery[0] == '+') {
            newQuery = newQuery.substring(1)
        }
        mFilteredCountries = getFilteredCountries(newQuery)
        if (mFilteredCountries.isNullOrEmpty()) {
            no_result_tv?.visibility = View.VISIBLE
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun getFilteredCountries(query: String): List<Country?> {
        val tempCountries: MutableList<Country?> = ArrayList()
        mCountryCodePicker.preferredCountries?.forEach { country ->
            if (country.isEligibleForQuery(query)) {
                tempCountries.add(country)
            }
        }
        if (tempCountries.size > 0) { //means at least one preferred country is added.
            tempCountries.add(null) // this will add separator for preference countries.
        }
        mCountryCodePicker.getCustomCountries()?.forEach { country->
            if (country.isEligibleForQuery(query)) {
                tempCountries.add(country)
            }
        }
        return tempCountries
    }

}