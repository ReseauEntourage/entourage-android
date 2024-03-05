package social.entourage.android.tools.view.countrycodepicker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.TelephonyManager
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

import social.entourage.android.R
import social.entourage.android.databinding.LayoutCodePickerBinding
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

/**
 * Trimmed down CountryCodePicker from [https://github.com/ReseauEntourage/CountryCodePicker](https://github.com/ReseauEntourage/CountryCodePicker)<br></br>
 * It adds Guadeloupe, Iceland, Kosovo. Removed the flags and libphoneutils library
 */
class CountryCodePicker : RelativeLayout {
    private lateinit var binding: LayoutCodePickerBinding
    private val localeDefaultCountry = Locale.getDefault().country
    var mBackgroundColor: Int = defaultBackgroundColor
        private set(backgroundColor) {
            field = backgroundColor
            binding.countryCodeHolderRly?.setBackgroundColor(backgroundColor)
        }
    private var mDefaultCountryCode = DEFAULT_COUNTRY_CODE
    private var mDefaultCountryNameCode: String? = null

    //Util
    private var mPhoneNumberWatcher: PhoneNumberWatcher? = null
    private var mRegisteredPhoneNumberTextView: TextView? = null
    private var mSelectedCountry: Country? = null
    private var defaultCountry: Country? = null
    private var countryCodeHolderClickListener: OnClickListener? = null
    private var mHideNameCode = false
    private var mShowFlag = true
    private var mShowFullName = false

    var countryCodePickerListener: CountryCodePickerListener? = null
    /**
     * SelectionDialogSearch is the facility to search through the list of country while selecting.
     *
     * @param isSelectionDialogShowSearch true will allow search and false will hide search box
     */
    var isSelectionDialogShowSearch = true
    var preferredCountries: List<Country>? = null
        private set

    //this will be "AU,ID,US"
    private var mCountryPreference: String? = null
    private var customCountries: List<Country>? = null

    /**
     * To provide definite set of countries when selection dialog is opened.
     * Only custom master countries, if defined, will be there is selection dialog to select from.
     * To set any country in preference, it must be included in custom master countries, if defined
     * When not defined or null or blank is set, it will use library's default master list
     * Custom master list will only limit the visibility of irrelevant country from selection dialog.
     * But all other functions like setCountryForCodeName() or setFullNumber() will consider all the
     * countries.
     *
     * @param customMasterCountries is country name codes separated by comma. e.g. "us,in,nz"
     * if null or "" , will remove custom countries and library default will be used.
     */
    //this will be "AU,ID,US"
    private var customMasterCountries: String? = null

    /**
     * By default, keyboard is poped every time ccp is clicked and selection dialog is opened.
     *
     * @param isKeyboardAutoPopOnSearch true: to open keyboard automatically when selection dialog is
     * opened
     * false: to avoid auto pop of keyboard
     */
    var isKeyboardAutoPopOnSearch = true
    private var mIsClickable = true
    private var mCountryCodeDialog: CountryCodeDialog? = null
    private var mHidePhoneCode = false
    private var mTextColor: Int = defaultContentColor

    // Font typeface
    private var mTypeFace: Typeface? = null

    /**
     * Get status of phone number formatter.
     *
     * @return enable or not.
     */
    private var isPhoneAutoFormatterEnabled = true
    private var mSetCountryByTimeZone = true

    constructor(context: Context?) : super(context) {
        binding = LayoutCodePickerBinding.inflate(LayoutInflater.from(context), this, true)
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        binding = LayoutCodePickerBinding.inflate(LayoutInflater.from(context), this, true)
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        binding = LayoutCodePickerBinding.inflate(LayoutInflater.from(context), this, true)
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        binding = LayoutCodePickerBinding.inflate(LayoutInflater.from(context), this, true)
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)
        val isAlternative = a.getBoolean(R.styleable.CountryCodePicker_ccp_alternative_layout, false)
        if (isAlternative) {
            View.inflate(context, R.layout.layout_code_picker_alternative, this)
        } else {
            View.inflate(context, R.layout.layout_code_picker, this)
        }
        applyCustomProperty(a)
        countryCodeHolderClickListener = OnClickListener { v: View? ->
            if (isClickable) {
                if (mCountryCodeDialog == null) {
                    mCountryCodeDialog = CountryCodeDialog(this@CountryCodePicker)
                    mCountryCodeDialog?.show()
                } else {
                    mCountryCodeDialog?.reShow()
                }
            }
        }
        binding.clickConsumerRly?.setOnClickListener(countryCodeHolderClickListener)
    }

    private fun applyCustomProperty(a: TypedArray) {
        try {
            // Hiding phone code
            mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false)

            //hide nameCode. If someone wants only phone code to avoid name collision for same
            // country phone code.
            mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false)

            //show full name
            mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)

            // enable auto formatter for phone number input
            isPhoneAutoFormatterEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true)

            //auto pop keyboard
            isKeyboardAutoPopOnSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true)

            //custom master list
            customMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
            refreshCustomMasterList()

            //preference
            mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference)
            refreshPreferredCountries()

            //default country
            mDefaultCountryNameCode = a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode)
            mDefaultCountryNameCode?.let { nameCode ->
                if (nameCode.isNotEmpty()) {
                    val temp = nameCode.trim { it <= ' ' }
                    if (temp.isNotEmpty()) {
                        setDefaultCountryUsingNameCode(nameCode)
                        selectedCountry = defaultCountry
                    } else {
                        mDefaultCountryNameCode = null
                    }
                }
            }

            //show flag
            showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true))

            //text color
            val newTextColor: Int = if (isInEditMode) {
                a.getColor(R.styleable.CountryCodePicker_ccp_textColor, 0)
            } else {
                a.getColor(R.styleable.CountryCodePicker_ccp_textColor,
                        ContextCompat.getColor(context, R.color.accent))
            }
            if (newTextColor != 0) {
                textColor = newTextColor
            }

            // background color of view.
            mBackgroundColor = a.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT)
            if (mBackgroundColor != Color.TRANSPARENT) {
                binding.countryCodeHolderRly?.setBackgroundColor(mBackgroundColor)
            }

            // text font
            val fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont)
            if (fontPath != null && fontPath.isNotEmpty()) {
                setTypeFace(fontPath)
            }

            //text size
            val textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
            if (textSize > 0) {
                binding.selectedCountryTv?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setFlagSize(textSize)
                setArrowSize(textSize)
            } else { //no text size specified
                val dm = context.resources.displayMetrics
                val defaultSize = (18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                setTextSize(defaultSize)
            }

            //if arrow arrow size is explicitly defined
            val arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
            if (arrowSize > 0) {
                setArrowSize(arrowSize)
            }
            isSelectionDialogShowSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true)
            isClickable = a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true)
            mSetCountryByTimeZone = a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true)

            // Set to default phone code if no country name code set in attribute.
            if (mDefaultCountryNameCode.isNullOrEmpty()) {
                setDefaultCountryFlagAndCode()
            }
        } catch (e: Exception) {
            Timber.e(e)
            if (isInEditMode) {
                binding.selectedCountryTv?.text = context.getString(R.string.country_france_flag)
            } else {
                binding.selectedCountryTv?.text = e.message
            }
        } finally {
            a.recycle()
        }
    }

        private fun setDefaultCountry(defaultCountry: Country) {
        this.defaultCountry = defaultCountry
    }

    //as soon as country is selected, textView should be updated
    var selectedCountry: Country?
        get() = mSelectedCountry
        set(selectedCountry) {
            //as soon as country is selected, textView should be updated
            val newSelectedCountry = selectedCountry ?: CountryLightList.getByCode(
                context,
                preferredCountries,
                mDefaultCountryCode
            ) ?: return
            mSelectedCountry = newSelectedCountry
            countryCodePickerListener?.updatedCountry(newSelectedCountry)
            binding.selectedCountryTv.text = if (!mHideNameCode) {
                if (mShowFullName) {
                    if (!mHidePhoneCode) {
                        context.getString(R.string.country_full_name_and_phone_code,
                            newSelectedCountry.name.uppercase(Locale.getDefault()), newSelectedCountry.phoneCode)
                    } else {
                        newSelectedCountry.name.uppercase(Locale.getDefault())
                    }
                } else {
                    if (!mHidePhoneCode) {
                        context.getString(R.string.country_code_and_phone_code,
                            newSelectedCountry.iso.uppercase(Locale.getDefault()), newSelectedCountry.phoneCode)
                    } else {
                        newSelectedCountry.iso.uppercase(Locale.getDefault())
                    }
                }
            } else {
                newSelectedCountry.flagTxt
            }
        }

    /**
     * this will load mPreferredCountries based on mCountryPreference
     */
    fun refreshPreferredCountries() {
        if (mCountryPreference.isNullOrBlank()) {
            preferredCountries = null
        } else {
            val localCountryList: MutableList<Country> = ArrayList()
            mCountryPreference?.let {
                for (nameCode in it.split(",".toRegex()).toTypedArray()) {
                    val country: Country? = CountryLightList.getByNameCodeFromCustomCountries(
                        context, customCountries,
                        nameCode
                    )
                    if (country != null) {
                        if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
                            localCountryList.add(country)
                        }
                    }
                }
            }
            preferredCountries = if (localCountryList.size == 0) {
                null
            } else {
                localCountryList
            }
        }
    }

    /**
     * this will load mPreferredCountries based on mCountryPreference
     */
    fun refreshCustomMasterList() {
        if (customMasterCountries.isNullOrEmpty()) {
            customCountries = null
        } else {
            val localCountryList: MutableList<Country> = ArrayList()
            customMasterCountries?.let {
                for (nameCode in it.split(",".toRegex()).toTypedArray()) {
                    val country: Country? =
                        CountryLightList.getByNameCodeFromAllCountries(context, nameCode)
                    if (country != null) {
                        if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
                            localCountryList.add(country)
                        }
                    }
                }
            }
            customCountries = if (localCountryList.size == 0) {
                null
            } else {
                localCountryList
            }
        }
    }

    /**
     * Get custom country by preference
     *
     * @param codePicker picker for the source of country
     * @return List of country
     */
    fun getCustomCountries(): List<Country>? {
        refreshCustomMasterList()
        return if (!customCountries.isNullOrEmpty()) {
            customCountries
        } else {
            CountryLightList.getAllCountries(context)
        }
    }

    /**
     * This will match name code of all countries of list against the country's name code.
     *
     * @param countryList list of countries against which country will be checked.
     * @return if country name code is found in list, returns true else return false
     */
    private fun isAlreadyInList(country: Country, countryList: List<Country>?): Boolean {
        countryList?.forEach { iterationCountry->
            if (iterationCountry.iso.equals(country.iso, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Default country name code defines your default country.
     * Whenever invalid / improper name code is found in setCountryForNameCode(), CCP will set to
     * default country.
     * This function will not set default country as selected in CCP. To set default country in CCP
     * call resetToDefaultCountry() right after this call.
     * If invalid mDefaultCountryCode is applied, it won't be changed.
     *
     * @param countryIso code of your default country
     * if you want to set IN +91(India) as default country, mDefaultCountryCode =  "IN" or "in"
     * if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  "JP" or "jp"
     */
    private fun setDefaultCountryUsingNameCode(countryIso: String?) {
        val defaultCountry: Country = CountryLightList.getByNameCodeFromAllCountries(
            context,
            countryIso
        ) ?: return
        //if correct country is found, set the country
        mDefaultCountryNameCode = defaultCountry.iso
        setDefaultCountry(defaultCountry)
    }

    /**
     * To get code of selected country with prefix "+".
     *
     * @return String value of selected country code in CCP with prefix "+"
     * i.e if selected country is IN +91(India)  returns: "+91"
     * if selected country is JP +81(Japan) returns: "+81"
     */
    val selectedCountryCodeWithPlus: String
        get() = context.getString(R.string.phone_code, mSelectedCountry?.phoneCode
                ?: defaultCountry?.phoneCode ?:"")

    var textColor: Int
        get() = mTextColor
        set(contentColor) {
            mTextColor = contentColor
            binding.selectedCountryTv?.setTextColor(mTextColor)
            binding.arrowImv?.setColorFilter(mTextColor, PorterDuff.Mode.SRC_IN)
        }

    /**
     * Modifies size of text in side CCP view.
     *
     * @param textSize size of text in pixels
     */
    private fun setTextSize(textSize: Int) {
        if (textSize > 0) {
            binding.selectedCountryTv?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            setArrowSize(textSize)
            setFlagSize(textSize)
        }
    }

    /**
     * Modifies size of downArrow in CCP view
     *
     * @param arrowSize size in pixels
     */
    private fun setArrowSize(arrowSize: Int) {
        if (arrowSize > 0) {
            binding.arrowImv?.let {
                val params = it.layoutParams as LayoutParams
                params.width = arrowSize
                params.height = arrowSize
                it.layoutParams = params
            }
        }
    }

    /**
     * set TypeFace for all the text in CCP
     *
     * @param fontAssetPath font path in asset folder.
     */
    private fun setTypeFace(fontAssetPath: String?) {
        try {
            val typeFace = Typeface.createFromAsset(context.assets, fontAssetPath)
            mTypeFace = typeFace
            binding.selectedCountryTv?.typeface = typeFace
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Set TypeFace for all the text in CCP
     *
     * @param typeFace TypeFace generated from assets.
     */
    var typeFace: Typeface?
        get() = mTypeFace
        set(typeFace) {
            mTypeFace = typeFace
            try {
                binding.selectedCountryTv?.typeface = typeFace
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    /**
     * Modifies size of flag in CCP view
     *
     * @param flagSize size in pixels
     */
    private fun setFlagSize(flagSize: Int) {
        binding.flagImv?.layoutParams?.height = flagSize
        binding.flagImv?.requestLayout()
    }

    private fun showFlag(showFlag: Boolean) {
        mShowFlag = showFlag
        binding.flagHolderLly?.visibility = if (showFlag) View.VISIBLE else View.GONE
    }

    override fun isClickable(): Boolean {
        return mIsClickable
    }

    /**
     * Allow click and open dialog
     */
    override fun setClickable(isClickable: Boolean) {
        mIsClickable = isClickable
        binding.clickConsumerRly?.let {
            it.isClickable = isClickable
            it.isEnabled = isClickable
            it.setOnClickListener(if(isClickable) countryCodeHolderClickListener else null)
        }
    }

    /**
     * Phone number watcher
     */
    private inner class PhoneNumberWatcher : PhoneNumberFormattingTextWatcher {
        private val lastValidity = false

        constructor() : super()

        constructor(countryCode: String?) : super(countryCode)
    }

    /**
     * Set default value
     * Will try to retrieve phone number from device
     */
    private fun setDefaultCountryFlagAndCode() {
        val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountryIso = telManager.simCountryIso
        if (!simCountryIso.isNullOrEmpty()) {
            setEmptyDefault(simCountryIso)
            Timber.d("simCountryIso = %s", simCountryIso)
        } else {
            val iso = telManager.networkCountryIso
            if (!iso.isNullOrEmpty()) {
                setEmptyDefault(iso)
                Timber.d("isoNetwork = %s", iso)
            } else {
                enableSetCountryByTimeZone(true)
            }
        }
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private fun setEmptyDefault() {
        setEmptyDefault(null)
    }

    /**
     * Set default value with default locale
     *
     * @param countryCode ISO2 of country
     */
    private fun setEmptyDefault(countryCode: String?) {
        val newCountryCode = if (countryCode.isNullOrEmpty()) {
            if (mDefaultCountryNameCode.isNullOrEmpty()) {
                if (localeDefaultCountry.isNotEmpty()) {
                    localeDefaultCountry
                } else {
                    DEFAULT_ISO_COUNTRY
                }
            } else {
                mDefaultCountryNameCode
            }
        } else countryCode

        if (isPhoneAutoFormatterEnabled) {
            if (mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = PhoneNumberWatcher(newCountryCode)
            }
        }
        setDefaultCountryUsingNameCode(newCountryCode)
        selectedCountry = defaultCountry
    }

    /**
     * Set checking for country from time zone. This is used to set country whenever CCP can't
     * detect country from phone setting.
     *
     * @param isEnabled set enable or not.
     */
    private fun enableSetCountryByTimeZone(isEnabled: Boolean) {
        if (isEnabled) {
            if (!mDefaultCountryNameCode.isNullOrEmpty()) return
            if (mRegisteredPhoneNumberTextView != null) return
            if (mSetCountryByTimeZone) {
                val tz = TimeZone.getDefault()
                Timber.d("tz.getID() = %s", tz.id)
                CountryLightList.getCountryIsoByTimeZone(context, tz.id)?.let { countryIsos ->
                    setDefaultCountryUsingNameCode(countryIsos[0])
                    selectedCountry = defaultCountry
                } ?: run  {
                    // If no iso country found, fallback to device locale.
                    setEmptyDefault()
                }
            }
        }
        mSetCountryByTimeZone = isEnabled
    }

    companion object {
        private const val DEFAULT_COUNTRY_CODE = 33 // France
        private const val DEFAULT_ISO_COUNTRY = "FR"
        const val defaultContentColor = 0
        const val defaultBackgroundColor = Color.TRANSPARENT
    }
}

interface CountryCodePickerListener {
    fun updatedCountry(country: Country)
}