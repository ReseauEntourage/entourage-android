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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import social.entourage.android.R
import timber.log.Timber
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

class CountryCodePicker : RelativeLayout {
    private val localeDefaultCountry = Locale.getDefault().country

    private var countryCodeHolderRly: View? = null
    private var clickConsumerRly: View? = null
    private var selectedCountryTv: TextView? = null
    private var arrowImv: ImageView? = null
    private var flagImv: ImageView? = null
    private var flagHolderLly: LinearLayout? = null

    var mBackgroundColor: Int = defaultBackgroundColor
        private set(backgroundColor) {
            field = backgroundColor
            countryCodeHolderRly?.setBackgroundColor(backgroundColor)
        }

    private var mDefaultCountryCode = DEFAULT_COUNTRY_CODE
    private var mDefaultCountryNameCode: String? = null

    private var mPhoneNumberWatcher: PhoneNumberWatcher? = null
    private var mRegisteredPhoneNumberTextView: TextView? = null
    private var mSelectedCountry: Country? = null
    private var defaultCountry: Country? = null
    private var countryCodeHolderClickListener: OnClickListener? = null
    private var mHideNameCode = false
    private var mShowFlag = true
    private var mShowFullName = false

    var countryCodePickerListener: CountryCodePickerListener? = null
    var isSelectionDialogShowSearch = true

    var preferredCountries: List<Country>? = null
        private set

    private var mCountryPreference: String? = null
    private var customCountries: List<Country>? = null
    private var customMasterCountries: String? = null

    var isKeyboardAutoPopOnSearch = true
    private var mIsClickable = true
    private var mCountryCodeDialog: CountryCodeDialog? = null
    private var mHidePhoneCode = false
    private var mTextColor: Int = defaultContentColor
    private var mTypeFace: Typeface? = null
    private var isPhoneAutoFormatterEnabled = true
    private var mSetCountryByTimeZone = true

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)
        val isAlternative = a.getBoolean(R.styleable.CountryCodePicker_ccp_alternative_layout, false)
        val layoutRes = if (isAlternative) R.layout.layout_code_picker_alternative else R.layout.layout_code_picker
        LayoutInflater.from(context).inflate(layoutRes, this, true)

        countryCodeHolderRly = findViewById(R.id.country_code_holder_rly)
        clickConsumerRly = findViewById(R.id.click_consumer_rly)
        selectedCountryTv = findViewById(R.id.selected_country_tv)
        arrowImv = findViewById(R.id.arrow_imv)
        flagImv = findViewById(R.id.flag_imv)
        flagHolderLly = findViewById(R.id.flag_holder_lly)

        applyCustomProperty(a)

        countryCodeHolderClickListener = OnClickListener {
            if (isClickable) {
                if (mCountryCodeDialog == null) {
                    mCountryCodeDialog = CountryCodeDialog(this@CountryCodePicker)
                    mCountryCodeDialog?.show()
                } else {
                    mCountryCodeDialog?.reShow()
                }
            }
        }

        clickConsumerRly?.setOnClickListener(countryCodeHolderClickListener)
    }

    private fun applyCustomProperty(a: TypedArray) {
        try {
            mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false)
            mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false)
            mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)
            isPhoneAutoFormatterEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true)
            isKeyboardAutoPopOnSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true)

            customMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
            refreshCustomMasterList()

            mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference)
            refreshPreferredCountries()

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

            showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true))

            val newTextColor: Int = if (isInEditMode) {
                a.getColor(R.styleable.CountryCodePicker_ccp_textColor, 0)
            } else {
                a.getColor(
                    R.styleable.CountryCodePicker_ccp_textColor,
                    ContextCompat.getColor(context, R.color.accent)
                )
            }
            if (newTextColor != 0) {
                textColor = newTextColor
            }

            mBackgroundColor = a.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT)
            if (mBackgroundColor != Color.TRANSPARENT) {
                countryCodeHolderRly?.setBackgroundColor(mBackgroundColor)
            }

            val fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont)
            if (!fontPath.isNullOrEmpty()) {
                setTypeFace(fontPath)
            }

            val textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
            if (textSize > 0) {
                selectedCountryTv?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setFlagSize(textSize)
                setArrowSize(textSize)
            } else {
                val dm = context.resources.displayMetrics
                val defaultSize = (18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                setTextSize(defaultSize)
            }

            val arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
            if (arrowSize > 0) {
                setArrowSize(arrowSize)
            }

            isSelectionDialogShowSearch = a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true)
            isClickable = a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true)
            mSetCountryByTimeZone = a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true)

            if (mDefaultCountryNameCode.isNullOrEmpty()) {
                setDefaultCountryFlagAndCode()
            }
        } catch (e: Exception) {
            Timber.e(e)
            if (isInEditMode) {
                selectedCountryTv?.text = context.getString(R.string.country_france_flag)
            } else {
                selectedCountryTv?.text = e.message
            }
        } finally {
            a.recycle()
        }
    }

    private fun setDefaultCountry(defaultCountry: Country) {
        this.defaultCountry = defaultCountry
    }

    var selectedCountry: Country?
        get() = mSelectedCountry
        set(selectedCountry) {
            val newSelectedCountry = selectedCountry ?: CountryLightList.getByCode(
                context,
                preferredCountries,
                mDefaultCountryCode
            ) ?: return
            mSelectedCountry = newSelectedCountry
            countryCodePickerListener?.updatedCountry(newSelectedCountry)
            selectedCountryTv?.text = if (!mHideNameCode) {
                if (mShowFullName) {
                    if (!mHidePhoneCode) {
                        context.getString(
                            R.string.country_full_name_and_phone_code,
                            newSelectedCountry.name.uppercase(Locale.getDefault()),
                            newSelectedCountry.phoneCode
                        )
                    } else {
                        newSelectedCountry.name.uppercase(Locale.getDefault())
                    }
                } else {
                    if (!mHidePhoneCode) {
                        context.getString(
                            R.string.country_code_and_phone_code,
                            newSelectedCountry.iso.uppercase(Locale.getDefault()),
                            newSelectedCountry.phoneCode
                        )
                    } else {
                        newSelectedCountry.iso.uppercase(Locale.getDefault())
                    }
                }
            } else {
                newSelectedCountry.flagTxt
            }
        }

    fun refreshPreferredCountries() {
        if (mCountryPreference.isNullOrBlank()) {
            preferredCountries = null
        } else {
            val localCountryList: MutableList<Country> = ArrayList()
            mCountryPreference?.let {
                for (nameCode in it.split(",".toRegex()).toTypedArray()) {
                    val country: Country? = CountryLightList.getByNameCodeFromCustomCountries(
                        context, customCountries, nameCode
                    )
                    if (country != null) {
                        if (!isAlreadyInList(country, localCountryList)) {
                            localCountryList.add(country)
                        }
                    }
                }
            }
            preferredCountries = if (localCountryList.isEmpty()) null else localCountryList
        }
    }

    fun refreshCustomMasterList() {
        if (customMasterCountries.isNullOrEmpty()) {
            customCountries = null
        } else {
            val localCountryList: MutableList<Country> = ArrayList()
            customMasterCountries?.let {
                for (nameCode in it.split(",".toRegex()).toTypedArray()) {
                    val country: Country? = CountryLightList.getByNameCodeFromAllCountries(context, nameCode)
                    if (country != null) {
                        if (!isAlreadyInList(country, localCountryList)) {
                            localCountryList.add(country)
                        }
                    }
                }
            }
            customCountries = if (localCountryList.isEmpty()) null else localCountryList
        }
    }

    fun getCustomCountries(): List<Country>? {
        refreshCustomMasterList()
        return if (!customCountries.isNullOrEmpty()) customCountries else CountryLightList.getAllCountries(context)
    }

    private fun isAlreadyInList(country: Country, countryList: List<Country>?): Boolean {
        countryList?.forEach { iterationCountry ->
            if (iterationCountry.iso.equals(country.iso, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun setDefaultCountryUsingNameCode(countryIso: String?) {
        val defaultCountry: Country = CountryLightList.getByNameCodeFromAllCountries(context, countryIso) ?: return
        mDefaultCountryNameCode = defaultCountry.iso
        setDefaultCountry(defaultCountry)
    }

    val selectedCountryCodeWithPlus: String
        get() = context.getString(
            R.string.phone_code,
            mSelectedCountry?.phoneCode ?: defaultCountry?.phoneCode ?: ""
        )

    var textColor: Int
        get() = mTextColor
        set(contentColor) {
            mTextColor = contentColor
            selectedCountryTv?.setTextColor(mTextColor)
            arrowImv?.setColorFilter(mTextColor, PorterDuff.Mode.SRC_IN)
        }

    private fun setTextSize(textSize: Int) {
        if (textSize > 0) {
            selectedCountryTv?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            setArrowSize(textSize)
            setFlagSize(textSize)
        }
    }

    private fun setArrowSize(arrowSize: Int) {
        if (arrowSize > 0) {
            arrowImv?.let {
                val params = it.layoutParams as LayoutParams
                params.width = arrowSize
                params.height = arrowSize
                it.layoutParams = params
            }
        }
    }

    private fun setTypeFace(fontAssetPath: String?) {
        try {
            val typeFace = Typeface.createFromAsset(context.assets, fontAssetPath)
            mTypeFace = typeFace
            selectedCountryTv?.typeface = typeFace
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    var typeFace: Typeface?
        get() = mTypeFace
        set(typeFace) {
            mTypeFace = typeFace
            try {
                selectedCountryTv?.typeface = typeFace
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

    private fun setFlagSize(flagSize: Int) {
        flagImv?.layoutParams?.height = flagSize
        flagImv?.requestLayout()
    }

    private fun showFlag(showFlag: Boolean) {
        mShowFlag = showFlag
        flagHolderLly?.visibility = if (showFlag) View.VISIBLE else View.GONE
    }

    override fun isClickable(): Boolean {
        return mIsClickable
    }

    override fun setClickable(isClickable: Boolean) {
        mIsClickable = isClickable
        clickConsumerRly?.let {
            it.isClickable = isClickable
            it.isEnabled = isClickable
            it.setOnClickListener(if (isClickable) countryCodeHolderClickListener else null)
        }
    }

    private inner class PhoneNumberWatcher : PhoneNumberFormattingTextWatcher {
        constructor() : super()
        constructor(countryCode: String?) : super(countryCode)
    }

    private fun setDefaultCountryFlagAndCode() {
        val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountryIso = telManager.simCountryIso
        if (!simCountryIso.isNullOrEmpty()) {
            setEmptyDefault(simCountryIso)
        } else {
            val iso = telManager.networkCountryIso
            if (!iso.isNullOrEmpty()) {
                setEmptyDefault(iso)
            } else {
                enableSetCountryByTimeZone(true)
            }
        }
    }

    private fun setEmptyDefault() {
        setEmptyDefault(null)
    }

    private fun setEmptyDefault(countryCode: String?) {
        val newCountryCode = if (countryCode.isNullOrEmpty()) {
            if (mDefaultCountryNameCode.isNullOrEmpty()) {
                if (localeDefaultCountry.isNotEmpty()) localeDefaultCountry else DEFAULT_ISO_COUNTRY
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

    private fun enableSetCountryByTimeZone(isEnabled: Boolean) {
        if (isEnabled) {
            if (!mDefaultCountryNameCode.isNullOrEmpty()) return
            if (mRegisteredPhoneNumberTextView != null) return
            if (mSetCountryByTimeZone) {
                val tz = TimeZone.getDefault()
                CountryLightList.getCountryIsoByTimeZone(context, tz.id)?.let { countryIsos ->
                    setDefaultCountryUsingNameCode(countryIsos[0])
                    selectedCountry = defaultCountry
                } ?: run {
                    setEmptyDefault()
                }
            }
        }
        mSetCountryByTimeZone = isEnabled
    }

    companion object {
        private const val DEFAULT_COUNTRY_CODE = 33
        private const val DEFAULT_ISO_COUNTRY = "FR"
        const val defaultContentColor = 0
        const val defaultBackgroundColor = Color.TRANSPARENT
    }
}

interface CountryCodePickerListener {
    fun updatedCountry(country: Country)
}
