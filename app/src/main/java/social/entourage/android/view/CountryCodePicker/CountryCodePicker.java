package social.entourage.android.view.CountryCodePicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import social.entourage.android.R;

/**
 * Trimmed down CountryCodePicker from <a href="https://github.com/ReseauEntourage/CountryCodePicker">https://github.com/ReseauEntourage/CountryCodePicker</a><br/>
 * It adds Guadeloupe, Iceland, Kosovo. Removed the flags and libphoneutils library
 */

public class CountryCodePicker extends RelativeLayout {

    private static String TAG = CountryCodePicker.class.getSimpleName();

    private final String DEFAULT_COUNTRY = Locale.getDefault().getCountry();
    private static final int DEFAULT_COUNTRY_CODE = 33; // France
    private static final String DEFAULT_ISO_COUNTRY = "FR";
    private static final int DEFAULT_TEXT_COLOR = 0;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

    private int mDefaultCountryCode = DEFAULT_COUNTRY_CODE;
    private String mDefaultCountryNameCode;

    //Util
    private PhoneNumberWatcher mPhoneNumberWatcher;
    PhoneNumberInputValidityListener mPhoneNumberInputValidityListener;

    private AppCompatTextView mTvSelectedCountry;
    private TextView mRegisteredPhoneNumberTextView;
    private RelativeLayout mRlyHolder;
    private AppCompatImageView mImvArrow;
    private AppCompatImageView mImvFlag;
    private LinearLayout mLlyFlagHolder;
    private Country mSelectedCountry;
    private Country mDefaultCountry;
    private RelativeLayout mRlyClickConsumer;
    OnClickListener mCountryCodeHolderClickListener;

    private boolean mHideNameCode = false;
    private boolean mShowFlag = true;
    private boolean mShowFullName = false;
    private boolean mUseFullName = false;
    private boolean mSelectionDialogShowSearch = true;

    private List<Country> mPreferredCountries;
    //this will be "AU,ID,US"
    private String mCountryPreference;
    private List<Country> mCustomMasterCountriesList;
    //this will be "AU,ID,US"
    private String mCustomMasterCountries;
    private boolean mKeyboardAutoPopOnSearch = true;
    private boolean mIsClickable = true;
    private CountryCodeDialog mCountryCodeDialog;

    private boolean mHidePhoneCode = false;

    private int mTextColor = DEFAULT_TEXT_COLOR;

    // Font typeface
    private Typeface mTypeFace;

    private boolean mIsHintEnabled = true;
    private boolean mIsEnablePhoneNumberWatcher = true;

    private boolean mSetCountryByTimeZone = true;

    private OnCountryChangeListener mOnCountryChangeListener;

    /**
     * interface to set change listener
     */
    public interface OnCountryChangeListener {
        void onCountrySelected(Country selectedCountry);
    }

    /**
     * Interface for checking when phone number checker validity is finish.
     */
    public interface PhoneNumberInputValidityListener {
        void onFinish(CountryCodePicker ccp, boolean isValid);
    }

    public CountryCodePicker(Context context) {
        super(context);
        init(null);
    }

    public CountryCodePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.layout_code_picker, this);

        mTvSelectedCountry = (AppCompatTextView) findViewById(R.id.selected_country_tv);
        mRlyHolder = (RelativeLayout) findViewById(R.id.country_code_holder_rly);
        mImvArrow = (AppCompatImageView) findViewById(R.id.arrow_imv);
        mImvFlag = (AppCompatImageView) findViewById(R.id.flag_imv);
        mLlyFlagHolder = (LinearLayout) findViewById(R.id.flag_holder_lly);
        mRlyClickConsumer = (RelativeLayout) findViewById(R.id.click_consumer_rly);

        applyCustomProperty(attrs);

        mCountryCodeHolderClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isClickable()) {
                    if (mCountryCodeDialog == null) {
                        mCountryCodeDialog = new CountryCodeDialog(CountryCodePicker.this);
                        mCountryCodeDialog.show();
                    } else {
                        mCountryCodeDialog.reShow();
                    }
                }
            }
        };

        mRlyClickConsumer.setOnClickListener(mCountryCodeHolderClickListener);
    }

    private void applyCustomProperty(AttributeSet attrs) {

        TypedArray a =
                getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0);

        try {
            // Hiding phone code
            mHidePhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hidePhoneCode, false);

            //hide nameCode. If someone wants only phone code to avoid name collision for same
            // country phone code.
            mHideNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_hideNameCode, false);

            //show full name
            mShowFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false);

            // show hint for phone number
            mIsHintEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_enableHint, true);

            // enable auto formatter for phone number input
            mIsEnablePhoneNumberWatcher =
                    a.getBoolean(R.styleable.CountryCodePicker_ccp_enablePhoneAutoFormatter, true);

            //auto pop keyboard
            setKeyboardAutoPopOnSearch(
                    a.getBoolean(R.styleable.CountryCodePicker_ccp_keyboardAutoPopOnSearch, true));

            //custom master list
            mCustomMasterCountries = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries);
            refreshCustomMasterList();

            //preference
            mCountryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference);
            refreshPreferredCountries();

            //default country
            mDefaultCountryNameCode = a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode);
            Log.d(TAG, "mDefaultCountryNameCode from attribute = " + mDefaultCountryNameCode);
            boolean setUsingNameCode = false;
            if (mDefaultCountryNameCode != null && !mDefaultCountryNameCode.isEmpty()) {
                String temp = mDefaultCountryNameCode.trim();
                if (!temp.isEmpty()) {
                    setDefaultCountryUsingNameCode(mDefaultCountryNameCode);
                    if (mDefaultCountryNameCode != null) {
                        setSelectedCountry(mDefaultCountry);
                    }
                } else {
                    mDefaultCountryNameCode = null;
                }
                //if (CountryUtils.getByNameCodeFromAllCountries(getContext(), mDefaultCountryNameCode)
                //    != null) {
                //  setUsingNameCode = true;
                //  setDefaultCountry(CountryUtils.getByNameCodeFromAllCountries(getContext(), mDefaultCountryNameCode));
                //  setSelectedCountry(mDefaultCountry);
                //}
            }

            //else {
            //  setDefaultCountry(CountryUtils.getByNameCodeFromAllCountries(getContext(), DEFAULT_COUNTRY));
            //  setSelectedCountry(mDefaultCountry);
            //}

            //if default country is not set using name code.
            //if (!setUsingNameCode) {
            //  int defaultCountryCode = a.getInteger(R.styleable.CountryCodePicker_ccp_defaultCode, -1);
            //
            //  //if invalid country is set using xml, it will be replaced with DEFAULT_COUNTRY_CODE
            //  if (CountryUtils.getByCode(getContext(), mPreferredCountries, defaultCountryCode) == null) {
            //    defaultCountryCode = DEFAULT_COUNTRY_CODE;
            //  }
            //  setDefaultCountryUsingPhoneCode(defaultCountryCode);
            //  setSelectedCountry(mDefaultCountry);
            //}

            //show flag
            showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true));

            //text color
            int textColor;
            if (isInEditMode()) {
                textColor = a.getColor(R.styleable.CountryCodePicker_ccp_textColor, 0);
            } else {
                textColor = a.getColor(R.styleable.CountryCodePicker_ccp_textColor,
                        ContextCompat.getColor(getContext(), R.color.accent));
            }
            if (textColor != 0) {
                setTextColor(textColor);
            }

            // background color of view.
            mBackgroundColor =
                    a.getColor(R.styleable.CountryCodePicker_ccp_backgroundColor, Color.TRANSPARENT);

            if (mBackgroundColor != Color.TRANSPARENT) {
                mRlyHolder.setBackgroundColor(mBackgroundColor);
            }

            // text font
            String fontPath = a.getString(R.styleable.CountryCodePicker_ccp_textFont);
            if (fontPath != null && !fontPath.isEmpty()) {
                setTypeFace(fontPath);
            }

            //text size
            int textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0);
            if (textSize > 0) {
                mTvSelectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                setFlagSize(textSize);
                setArrowSize(textSize);
            } else { //no text size specified
                DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
                int defaultSize = Math.round(18 * (dm.xdpi / DisplayMetrics.DENSITY_DEFAULT));
                setTextSize(defaultSize);
            }

            //if arrow arrow size is explicitly defined
            int arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0);
            if (arrowSize > 0) {
                setArrowSize(arrowSize);
            }

            mSelectionDialogShowSearch =
                    a.getBoolean(R.styleable.CountryCodePicker_ccp_selectionDialogShowSearch, true);
            setClickable(a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true));

            mSetCountryByTimeZone =
                    a.getBoolean(R.styleable.CountryCodePicker_ccp_setCountryByTimeZone, true);

            // Set to default phone code if no country name code set in attribute.
            if (mDefaultCountryNameCode == null || mDefaultCountryNameCode.isEmpty()) {
                setDefaultCountryFlagAndCode();
            }
        } catch (Exception e) {
            Log.d(TAG, "exception = " + e.toString());
            if (isInEditMode()) {
                mTvSelectedCountry.setText(getContext().getString(R.string.phone_code,
                        getContext().getString(R.string.country_france_number)));
            } else {
                mTvSelectedCountry.setText(e.getMessage());
            }
        } finally {
            a.recycle();
        }
    }

    private Country getDefaultCountry() {
        return mDefaultCountry;
    }

    private void setDefaultCountry(Country defaultCountry) {
        this.mDefaultCountry = defaultCountry;
    }

    @SuppressWarnings("unused")
    private Country getSelectedCountry() {
        return mSelectedCountry;
    }

    protected void setSelectedCountry(Country selectedCountry) {
        //as soon as country is selected, textView should be updated
        if (selectedCountry == null) {
            selectedCountry =
                    CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
            if (selectedCountry == null) return;
        }
        this.mSelectedCountry = selectedCountry;

        if (!mHideNameCode) {
            if (mShowFullName) {
                if (!mHidePhoneCode) {
                    mTvSelectedCountry.setText(
                            getContext().getString(R.string.country_full_name_and_phone_code,
                                    selectedCountry.getName().toUpperCase(), selectedCountry.getPhoneCode()));
                } else {
                    mTvSelectedCountry.setText(selectedCountry.getName().toUpperCase());
                }
            } else {
                if (!mHidePhoneCode) {
                    mTvSelectedCountry.setText(getContext().getString(R.string.country_code_and_phone_code,
                            selectedCountry.getIso().toUpperCase(), selectedCountry.getPhoneCode()));
                } else {
                    mTvSelectedCountry.setText(selectedCountry.getIso().toUpperCase());
                }
            }
        } else {
            mTvSelectedCountry.setText(
                    getContext().getString(R.string.phone_code, selectedCountry.getPhoneCode()));
        }

        if (mOnCountryChangeListener != null) {
            mOnCountryChangeListener.onCountrySelected(selectedCountry);
        }

        if (mIsHintEnabled) {
            setPhoneNumberHint();
        }
    }

    boolean isKeyboardAutoPopOnSearch() {
        return mKeyboardAutoPopOnSearch;
    }

    /**
     * By default, keyboard is poped every time ccp is clicked and selection dialog is opened.
     *
     * @param keyboardAutoPopOnSearch true: to open keyboard automatically when selection dialog is
     *                                opened
     *                                false: to avoid auto pop of keyboard
     */
    public void setKeyboardAutoPopOnSearch(boolean keyboardAutoPopOnSearch) {
        this.mKeyboardAutoPopOnSearch = keyboardAutoPopOnSearch;
    }

    /**
     * Get status of phone number formatter.
     *
     * @return enable or not.
     */
    @SuppressWarnings("unused")
    public boolean isPhoneAutoFormatterEnabled() {
        return mIsEnablePhoneNumberWatcher;
    }

    /**
     * Enable or disable auto formatter for phone number inserted to TextView.
     * You need to set an EditText for phone number with `registerPhoneNumberTextView()`
     * to make use of this.
     *
     * @param isEnable return if phone auto formatter enabled or not.
     */
    @SuppressWarnings("unused")
    public void enablePhoneAutoFormatter(boolean isEnable) {
        this.mIsEnablePhoneNumberWatcher = isEnable;
        if (isEnable) {
            if (mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = new PhoneNumberWatcher(getSelectedCountryNameCode());
            }
        } else {
            mPhoneNumberWatcher = null;
        }
    }

    @SuppressWarnings("unused")
    private OnClickListener getCountryCodeHolderClickListener() {
        return mCountryCodeHolderClickListener;
    }

    /**
     * this will load mPreferredCountries based on mCountryPreference
     */
    void refreshPreferredCountries() {
        if (mCountryPreference == null || mCountryPreference.length() == 0) {
            mPreferredCountries = null;
        } else {
            List<Country> localCountryList = new ArrayList<>();
            for (String nameCode : mCountryPreference.split(",")) {
                Country country =
                        CountryUtils.getByNameCodeFromCustomCountries(getContext(), mCustomMasterCountriesList,
                                nameCode);
                if (country != null) {
                    if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
                        localCountryList.add(country);
                    }
                }
            }

            if (localCountryList.size() == 0) {
                mPreferredCountries = null;
            } else {
                mPreferredCountries = localCountryList;
            }
        }
    }

    /**
     * this will load mPreferredCountries based on mCountryPreference
     */
    void refreshCustomMasterList() {
        if (mCustomMasterCountries == null || mCustomMasterCountries.length() == 0) {
            mCustomMasterCountriesList = null;
        } else {
            List<Country> localCountryList = new ArrayList<>();
            for (String nameCode : mCustomMasterCountries.split(",")) {
                Country country = CountryUtils.getByNameCodeFromAllCountries(getContext(), nameCode);
                if (country != null) {
                    if (!isAlreadyInList(country, localCountryList)) { //to avoid duplicate entry of country
                        localCountryList.add(country);
                    }
                }
            }

            if (localCountryList.size() == 0) {
                mCustomMasterCountriesList = null;
            } else {
                mCustomMasterCountriesList = localCountryList;
            }
        }
    }

    List<Country> getCustomCountries() {
        return mCustomMasterCountriesList;
    }

    /**
     * Get custom country by preference
     *
     * @param codePicker picker for the source of country
     * @return List of country
     */
    List<Country> getCustomCountries(CountryCodePicker codePicker) {
        codePicker.refreshCustomMasterList();
        if (codePicker.getCustomCountries() != null && codePicker.getCustomCountries().size() > 0) {
            return codePicker.getCustomCountries();
        } else {
            return CountryUtils.getAllCountries(codePicker.getContext());
        }
    }

    @SuppressWarnings("unused")
    public void setCustomMasterCountriesList(List<Country> mCustomMasterCountriesList) {
        this.mCustomMasterCountriesList = mCustomMasterCountriesList;
    }

    @SuppressWarnings("unused")
    public String getCustomMasterCountries() {
        return mCustomMasterCountries;
    }

    public List<Country> getPreferredCountries() {
        return mPreferredCountries;
    }

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
     *                              if null or "" , will remove custom countries and library default will be used.
     */
    public void setCustomMasterCountries(String customMasterCountries) {
        this.mCustomMasterCountries = customMasterCountries;
    }

    /**
     * This will match name code of all countries of list against the country's name code.
     *
     * @param countryList list of countries against which country will be checked.
     * @return if country name code is found in list, returns true else return false
     */
    private boolean isAlreadyInList(Country country, List<Country> countryList) {
        if (country != null && countryList != null) {
            for (Country iterationCountry : countryList) {
                if (iterationCountry.getIso().equalsIgnoreCase(country.getIso())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This function removes possible country code from fullNumber and set rest of the number as
     * carrier number.
     *
     * @param fullNumber combination of country code and carrier number.
     * @param country    selected country in CCP to detect country code part.
     */
    private String detectCarrierNumber(String fullNumber, Country country) {
        String carrierNumber;
        if (country == null || fullNumber == null) {
            carrierNumber = fullNumber;
        } else {
            int indexOfCode = fullNumber.indexOf(country.getPhoneCode());
            if (indexOfCode == -1) {
                carrierNumber = fullNumber;
            } else {
                carrierNumber = fullNumber.substring(indexOfCode + country.getPhoneCode().length());
            }
        }
        return carrierNumber;
    }

    /**
     * This method is not encouraged because this might set some other country which have same country
     * code as of yours. e.g 1 is common for US and canada.
     * If you are trying to set US ( and mCountryPreference is not set) and you pass 1 as @param
     * mDefaultCountryCode, it will set canada (prior in list due to alphabetical order)
     * Rather use setDefaultCountryUsingNameCode("us"); or setDefaultCountryUsingNameCode("US");
     * <p>
     * Default country code defines your default country.
     * Whenever invalid / improper number is found in setCountryForPhoneCode() /  setFullNumber(), it
     * CCP will set to default country.
     * This function will not set default country as selected in CCP. To set default country in CCP
     * call resetToDefaultCountry() right after this call.
     * If invalid mDefaultCountryCode is applied, it won't be changed.
     *
     * @param defaultCountryCode code of your default country
     *                           if you want to set IN +91(India) as default country, mDefaultCountryCode =  91
     *                           if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  81
     */
    @Deprecated
    public void setDefaultCountryUsingPhoneCode(int defaultCountryCode) {
        Country defaultCountry =
                CountryUtils.getByCode(getContext(), mPreferredCountries, defaultCountryCode);
        //if correct country is found, set the country
        if (defaultCountry != null) {
            this.mDefaultCountryCode = defaultCountryCode;
            setDefaultCountry(defaultCountry);
        }
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
     *                   if you want to set IN +91(India) as default country, mDefaultCountryCode =  "IN" or "in"
     *                   if you want to set JP +81(Japan) as default country, mDefaultCountryCode =  "JP" or "jp"
     */
    public void setDefaultCountryUsingNameCode(String countryIso) {
        Country defaultCountry = CountryUtils.getByNameCodeFromAllCountries(getContext(), countryIso);
        //if correct country is found, set the country
        if (defaultCountry != null) {
            this.mDefaultCountryNameCode = defaultCountry.getIso();
            setDefaultCountry(defaultCountry);
        }
    }

    /**
     * Get Country Code of default country
     * i.e if default country is IN +91(India)  returns: "91"
     * if default country is JP +81(Japan) returns: "81"
     */
    public String getDefaultCountryCode() {
        return mDefaultCountry.getPhoneCode();
    }

    /**
     * * To get code of default country as Integer.
     *
     * @return integer value of default country code in CCP
     * i.e if default country is IN +91(India)  returns: 91
     * if default country is JP +81(Japan) returns: 81
     */
    @SuppressWarnings("unused")
    public int getDefaultCountryCodeAsInt() {
        int code = 0;
        try {
            code = Integer.parseInt(getDefaultCountryCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * To get code of default country with prefix "+".
     *
     * @return String value of default country code in CCP with prefix "+"
     * i.e if default country is IN +91(India)  returns: "+91"
     * if default country is JP +81(Japan) returns: "+81"
     */
    @SuppressWarnings("unused")
    public String getDefaultCountryCodeWithPlus() {
        return getContext().getString(R.string.phone_code, getDefaultCountryCode());
    }

    /**
     * To get name of default country.
     *
     * @return String value of country name, default in CCP
     * i.e if default country is IN +91(India)  returns: "India"
     * if default country is JP +81(Japan) returns: "Japan"
     */
    public String getDefaultCountryName() {
        return mDefaultCountry.getName();
    }

    /**
     * To get name code of default country.
     *
     * @return String value of country name, default in CCP
     * i.e if default country is IN +91(India)  returns: "IN"
     * if default country is JP +81(Japan) returns: "JP"
     */
    public String getDefaultCountryNameCode() {
        return mDefaultCountry.getIso().toUpperCase();
    }

    /**
     * reset the default country as selected country.
     */
    @SuppressWarnings("unused")
    public void resetToDefaultCountry() {
        setEmptyDefault();
    }

    /**
     * To get code of selected country.
     *
     * @return String value of selected country code in CCP
     * i.e if selected country is IN +91(India)  returns: "91"
     * if selected country is JP +81(Japan) returns: "81"
     */
    public String getSelectedCountryCode() {
        return mSelectedCountry.getPhoneCode();
    }

    /**
     * To get code of selected country with prefix "+".
     *
     * @return String value of selected country code in CCP with prefix "+"
     * i.e if selected country is IN +91(India)  returns: "+91"
     * if selected country is JP +81(Japan) returns: "+81"
     */
    public String getSelectedCountryCodeWithPlus() {
        return getContext().getString(R.string.phone_code, getSelectedCountryCode());
    }

    /**
     * * To get code of selected country as Integer.
     *
     * @return integer value of selected country code in CCP
     * i.e if selected country is IN +91(India)  returns: 91
     * if selected country is JP +81(Japan) returns: 81
     */
    @SuppressWarnings("unused")
    public int getSelectedCountryCodeAsInt() {
        int code = 0;
        try {
            code = Integer.parseInt(getSelectedCountryCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * To get name of selected country.
     *
     * @return String value of country name, selected in CCP
     * i.e if selected country is IN +91(India)  returns: "India"
     * if selected country is JP +81(Japan) returns: "Japan"
     */
    public String getSelectedCountryName() {
        return mSelectedCountry.getName();
    }

    /**
     * To get name code of selected country.
     *
     * @return String value of country name, selected in CCP
     * i.e if selected country is IN +91(India)  returns: "IN"
     * if selected country is JP +81(Japan) returns: "JP"
     */
    public String getSelectedCountryNameCode() {
        return mSelectedCountry.getIso().toUpperCase();
    }

    /**
     * This will set country with @param countryCode as country code, in CCP
     *
     * @param countryCode a valid country code.
     *                    If you want to set IN +91(India), countryCode= 91
     *                    If you want to set JP +81(Japan), countryCode= 81
     */
    public void setCountryForPhoneCode(int countryCode) {
        Country country = CountryUtils.getByCode(getContext(), mPreferredCountries, countryCode);
        if (country == null) {
            if (mDefaultCountry == null) {
                mDefaultCountry =
                        CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
            }
            setSelectedCountry(mDefaultCountry);
        } else {
            setSelectedCountry(country);
        }
    }

    /**
     * This will set country with @param countryNameCode as country name code, in CCP
     *
     * @param countryNameCode a valid country name code.
     *                        If you want to set IN +91(India), countryCode= IN
     *                        If you want to set JP +81(Japan), countryCode= JP
     */
    public void setCountryForNameCode(String countryNameCode) {
        Country country = CountryUtils.getByNameCodeFromAllCountries(getContext(), countryNameCode);
        if (country == null) {
            if (mDefaultCountry == null) {
                mDefaultCountry =
                        CountryUtils.getByCode(getContext(), mPreferredCountries, mDefaultCountryCode);
            }
            setSelectedCountry(mDefaultCountry);
        } else {
            setSelectedCountry(country);
        }
    }

    /**
     * All functions that work with fullNumber need an editText to write and read carrier number of
     * full number.
     * An editText for carrier number must be registered in order to use functions like
     * setFullNumber() and getFullNumber().
     *
     * @param textView - an editText where user types carrier number ( the part of full
     *                 number other than country code).
     */
    public void registerPhoneNumberTextView(TextView textView) {
        setRegisteredPhoneNumberTextView(textView);
    }

    @SuppressWarnings("unused")
    public TextView getRegisteredPhoneNumberTextView() {
        return mRegisteredPhoneNumberTextView;
    }

    void setRegisteredPhoneNumberTextView(TextView phoneNumberTextView) {
        this.mRegisteredPhoneNumberTextView = phoneNumberTextView;
        if (mIsEnablePhoneNumberWatcher) {
            if (mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = new PhoneNumberWatcher(getDefaultCountryNameCode());
            }
            this.mRegisteredPhoneNumberTextView.addTextChangedListener(mPhoneNumberWatcher);
        }
        if (mIsHintEnabled) {
            setPhoneNumberHint();
        }
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param
     * editTextCarrierNumber
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
     * 8866667722, this will return "918866667722"
     */
    public String getFullNumber() {
        String fullNumber;
        if (mRegisteredPhoneNumberTextView != null) {
            fullNumber =
                    mSelectedCountry.getPhoneCode() + mRegisteredPhoneNumberTextView.getText().toString();
        } else {
            fullNumber = mSelectedCountry.getPhoneCode();
            Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
        }
        return fullNumber;
    }

    /**
     * Separate out country code and carrier number from fullNumber.
     * Sets country of separated country code in CCP and carrier number as text of
     * editTextCarrierNumber
     * If no valid country code is found from full number, CCP will be set to default country code and
     * full number will be set as carrier number to editTextCarrierNumber.
     *
     * @param fullNumber is combination of country code and carrier number,
     *                   (country_code+carrier_number) for example if country is India (+91) and carrier/mobile number
     *                   is 8866667722 then full number will be 9188666667722 or +918866667722. "+" in starting of
     *                   number is optional.
     */
    public void setFullNumber(String fullNumber) {
        Country country = CountryUtils.getByNumber(getContext(), mPreferredCountries, fullNumber);
        setSelectedCountry(country);
        String carrierNumber = detectCarrierNumber(fullNumber, country);
        if (mRegisteredPhoneNumberTextView != null) {
            mRegisteredPhoneNumberTextView.setText(carrierNumber);
        } else {
            Log.w(TAG, getContext().getString(R.string.error_unregister_carrier_number));
        }
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param
     * editTextCarrierNumber and prefix "+"
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number=
     * 8866667722, this will return "+918866667722"
     */
    public String getFullNumberWithPlus() {
        return getContext().getString(R.string.phone_code, getFullNumber());
    }

    /**
     * @return content color of CCP's text and small downward arrow.
     */
    public int getTextColor() {
        return mTextColor;
    }

    public int getDefaultContentColor() {
        return DEFAULT_TEXT_COLOR;
    }

    /**
     * Sets text and small down arrow color of CCP.
     *
     * @param contentColor color to apply to text and down arrow
     */
    public void setTextColor(int contentColor) {
        this.mTextColor = contentColor;
        mTvSelectedCountry.setTextColor(this.mTextColor);
        mImvArrow.setColorFilter(this.mTextColor, PorterDuff.Mode.SRC_IN);
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        mRlyHolder.setBackgroundColor(backgroundColor);
    }

    public int getDefaultBackgroundColor() {
        return DEFAULT_BACKGROUND_COLOR;
    }

    /**
     * Modifies size of text in side CCP view.
     *
     * @param textSize size of text in pixels
     */
    public void setTextSize(int textSize) {
        if (textSize > 0) {
            mTvSelectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            setArrowSize(textSize);
            setFlagSize(textSize);
        }
    }

    /**
     * Modifies size of downArrow in CCP view
     *
     * @param arrowSize size in pixels
     */
    private void setArrowSize(int arrowSize) {
        if (arrowSize > 0) {
            LayoutParams params = (LayoutParams) mImvArrow.getLayoutParams();
            params.width = arrowSize;
            params.height = arrowSize;
            mImvArrow.setLayoutParams(params);
        }
    }

    /**
     * If nameCode of country in CCP view is not required use this to show/hide country name code of
     * ccp view.
     *
     * @param hideNameCode true will remove country name code from ccp view, it will result  " +91 "
     *                     false will show country name code in ccp view, it will result " (IN) +91 "
     */
    @SuppressWarnings("unused")
    public void hideNameCode(boolean hideNameCode) {
        this.mHideNameCode = hideNameCode;
        setSelectedCountry(mSelectedCountry);
    }

    /**
     * This will set preferred countries using their name code. Prior preferred countries will be
     * replaced by these countries.
     * Preferred countries will be at top of country selection box.
     * If more than one countries have same country code, country in preferred list will have higher
     * priory than others. e.g. Canada and US have +1 as their country code. If "us" is set as
     * preferred country then US will be selected whenever setCountryForPhoneCode(1); or
     * setFullNumber("+1xxxxxxxxx"); is called.
     *
     * @param countryPreference is country name codes separated by comma. e.g. "us,in,nz"
     */
    public void setCountryPreference(String countryPreference) {
        this.mCountryPreference = countryPreference;
    }

    /**
     * Set TypeFace for all the text in CCP
     *
     * @param typeFace TypeFace generated from assets.
     */
    @SuppressWarnings("unused")
    public void setTypeFace(Typeface typeFace) {
        mTypeFace = typeFace;
        try {
            mTvSelectedCountry.setTypeface(typeFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set TypeFace for all the text in CCP
     *
     * @param fontAssetPath font path in asset folder.
     */
    public void setTypeFace(String fontAssetPath) {
        try {
            Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), fontAssetPath);
            mTypeFace = typeFace;
            mTvSelectedCountry.setTypeface(typeFace);
        } catch (Exception e) {
            Log.d(TAG, "Invalid fontPath. " + e.toString());
        }
    }

    /**
     * To change font of ccp views along with style.
     */
    @SuppressWarnings("unused")
    public void setTypeFace(Typeface typeFace, int style) {
        try {
            mTvSelectedCountry.setTypeface(typeFace, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Typeface getTypeFace() {
        return mTypeFace;
    }

    /**
     * To get call back on country selection a mOnCountryChangeListener must be registered.
     */
    public void setOnCountryChangeListener(OnCountryChangeListener onCountryChangeListener) {
        this.mOnCountryChangeListener = onCountryChangeListener;
    }

    /**
     * Modifies size of flag in CCP view
     *
     * @param flagSize size in pixels
     */
    public void setFlagSize(int flagSize) {
        mImvFlag.getLayoutParams().height = flagSize;
        mImvFlag.requestLayout();
    }

    public void showFlag(boolean showFlag) {
        this.mShowFlag = showFlag;
        if (showFlag) {
            mLlyFlagHolder.setVisibility(VISIBLE);
        } else {
            mLlyFlagHolder.setVisibility(GONE);
        }
    }

    /**
     * Show full country name instead only iso name.
     *
     * @param showFullName show or not.
     */
    @SuppressWarnings("unused")
    public void showFullName(boolean showFullName) {
        this.mShowFullName = showFullName;
        setSelectedCountry(mSelectedCountry);
    }

    /**
     * SelectionDialogSearch is the facility to search through the list of country while selecting.
     *
     * @return true if search is set allowed
     */
    public boolean isSelectionDialogShowSearch() {
        return mSelectionDialogShowSearch;
    }

    /**
     * SelectionDialogSearch is the facility to search through the list of country while selecting.
     *
     * @param selectionDialogShowSearch true will allow search and false will hide search box
     */
    @SuppressWarnings("unused")
    public void setSelectionDialogShowSearch(
            boolean selectionDialogShowSearch) {
        this.mSelectionDialogShowSearch = selectionDialogShowSearch;
    }

    @Override
    public boolean isClickable() {
        return mIsClickable;
    }

    /**
     * Allow click and open dialog
     */
    public void setClickable(boolean isClickable) {
        this.mIsClickable = isClickable;
        if (!isClickable) {
            mRlyClickConsumer.setOnClickListener(null);
            mRlyClickConsumer.setClickable(false);
            mRlyClickConsumer.setEnabled(false);
        } else {
            mRlyClickConsumer.setOnClickListener(mCountryCodeHolderClickListener);
            mRlyClickConsumer.setClickable(true);
            mRlyClickConsumer.setEnabled(true);
        }
    }

    public boolean isHidePhoneCode() {
        return mHidePhoneCode;
    }

    /**
     * Check whether phone text sample hint is enabled or not.
     *
     * @return is hint enabled or not.
     */
    @SuppressWarnings("unused")
    public boolean isHintEnabled() {
        return mIsHintEnabled;
    }

    /**
     * Enable hint for phone number sample in registered TextView with registerPhoneNumberTextView()
     *
     * @param hintEnabled disable or enable hint.
     */
    @SuppressWarnings("unused")
    public void enableHint(boolean hintEnabled) {
        this.mIsHintEnabled = hintEnabled;
        if (mIsHintEnabled) {
            setPhoneNumberHint();
        }
    }

    /**
     * Hide or show phone code
     *
     * @param hidePhoneCode show or not show the phone code.
     */
    //TODO: Check this
    @SuppressWarnings("unused")
    public void setHidePhoneCode(boolean hidePhoneCode) {
        this.mHidePhoneCode = hidePhoneCode;

        // Reset the view
        if (!mHideNameCode) {
            if (mShowFullName) {
                if (!mHidePhoneCode) {
                    mTvSelectedCountry.setText(
                            getContext().getString(R.string.country_full_name_and_phone_code,
                                    mSelectedCountry.getName().toUpperCase(), mSelectedCountry.getPhoneCode()));
                } else {
                    mTvSelectedCountry.setText(mSelectedCountry.getName().toUpperCase());
                }
            } else {
                if (!mHidePhoneCode) {
                    mTvSelectedCountry.setText(getContext().getString(R.string.country_code_and_phone_code,
                            mSelectedCountry.getIso().toUpperCase(), mSelectedCountry.getPhoneCode()));
                } else {
                    mTvSelectedCountry.setText(mSelectedCountry.getIso().toUpperCase());
                }
            }
        } else {
            mTvSelectedCountry.setText(
                    getContext().getString(R.string.phone_code, mSelectedCountry.getPhoneCode()));
        }
    }

    private void setPhoneNumberHint() {
    /*
    if (mRegisteredPhoneNumberTextView != null
        && mSelectedCountry != null
        && mSelectedCountry.getIso() != null) {
      Phonenumber.PhoneNumber phoneNumber =
          mPhoneUtil.getExampleNumberForType(mSelectedCountry.getIso().toUpperCase(),
              PhoneNumberUtil.PhoneNumberType.MOBILE);
      if (phoneNumber != null) {
        mRegisteredPhoneNumberTextView.setHint(
            mPhoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
      }
    }
    */
    }

    /**
     * Phone number watcher
     */
    private class PhoneNumberWatcher extends PhoneNumberFormattingTextWatcher {
        private boolean lastValidity;

        @SuppressWarnings("unused")
        public PhoneNumberWatcher() {
            super();
        }

        //TODO solve it! support for android kitkat
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public PhoneNumberWatcher(String countryCode) {
            super(countryCode);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            super.onTextChanged(s, start, before, count);
        }
    }

    public void setPhoneNumberInputValidityListener(PhoneNumberInputValidityListener listener) {
        this.mPhoneNumberInputValidityListener = listener;
    }

    /**
     * Set default value
     * Will try to retrieve phone number from device
     */
    private void setDefaultCountryFlagAndCode() {
        TelephonyManager telManager =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String simCountryIso = telManager.getSimCountryIso();
        if (simCountryIso != null && !simCountryIso.isEmpty()) {
            setEmptyDefault(simCountryIso);
            Log.d(TAG, "simCountryIso = " + simCountryIso);
        } else {
            String iso = telManager.getNetworkCountryIso();
            if (iso != null && !iso.isEmpty()) {
                setEmptyDefault(iso);
                Log.d(TAG, "isoNetwork = " + iso);
            } else {
                enableSetCountryByTimeZone(true);
            }
        }
    }

    /**
     * Alias for setting empty string of default settings from the device (using locale)
     */
    private void setEmptyDefault() {
        setEmptyDefault(null);
    }

    /**
     * Set default value with default locale
     *
     * @param countryCode ISO2 of country
     */
    private void setEmptyDefault(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            if (mDefaultCountryNameCode != null && !mDefaultCountryNameCode.isEmpty()) {
                countryCode = mDefaultCountryNameCode;
            } else {
                if (DEFAULT_COUNTRY != null && !DEFAULT_COUNTRY.isEmpty()) {
                    countryCode = DEFAULT_COUNTRY;
                } else {
                    countryCode = DEFAULT_ISO_COUNTRY;
                }
            }
        }

        if (mIsEnablePhoneNumberWatcher) {
            if (mPhoneNumberWatcher == null) {
                mPhoneNumberWatcher = new PhoneNumberWatcher(countryCode);
            }
        }

        setDefaultCountryUsingNameCode(countryCode);
        setSelectedCountry(getDefaultCountry());
    }

    /**
     * Set checking for country from time zone. This is used to set country whenever CCP can't
     * detect country from phone setting.
     *
     * @param isEnabled set enable or not.
     */
    public void enableSetCountryByTimeZone(boolean isEnabled) {
        if (isEnabled) {
            if (mDefaultCountryNameCode != null && !mDefaultCountryNameCode.isEmpty()) return;
            if (mRegisteredPhoneNumberTextView != null) return;
            if (mSetCountryByTimeZone) {
                TimeZone tz = TimeZone.getDefault();

                Log.d(TAG, "tz.getID() = " + tz.getID());
                List<String> countryIsos = CountryUtils.getCountryIsoByTimeZone(getContext(), tz.getID());
                if (countryIsos != null) {
                    setDefaultCountryUsingNameCode(countryIsos.get(0));
                    setSelectedCountry(getDefaultCountry());
                } else {
                    // If no iso country found, fallback to device locale.
                    setEmptyDefault();
                }
            }
        }
        mSetCountryByTimeZone = isEnabled;
    }
}
