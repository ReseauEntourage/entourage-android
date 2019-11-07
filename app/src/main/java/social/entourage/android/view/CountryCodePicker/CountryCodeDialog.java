package social.entourage.android.view.countrycodepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import social.entourage.android.R;

/**
 * Dialog for selecting Country.
 */

class CountryCodeDialog extends Dialog {
    private AppCompatEditText mEdtSearch;
    private AppCompatTextView mTvNoResult;
    private AppCompatTextView mTvTitle;
    private RecyclerView mRvCountryDialog;
    private CountryCodePicker mCountryCodePicker;
    private RelativeLayout mRlyDialog;

    private List<Country> masterCountries;
    private List<Country> mFilteredCountries;
    private InputMethodManager mInputMethodManager;
    private CountryCodeAdapter mAdapter;
    private List<Country> mTempCountries;

    CountryCodeDialog(CountryCodePicker countryCodePicker) {
        super(countryCodePicker.getContext());
        this.mCountryCodePicker = countryCodePicker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_code_picker_dialog);
        setupUI();
        setupData();
    }

    private void setupUI() {
        mRlyDialog = this.findViewById(R.id.dialog_rly);
        mRvCountryDialog = this.findViewById(R.id.country_dialog_rv);
        mTvTitle = this.findViewById(R.id.title_tv);
        mEdtSearch = this.findViewById(R.id.search_edt);
        mTvNoResult = this.findViewById(R.id.no_result_tv);
    }

    private void setupData() {
        if (mCountryCodePicker.getTypeFace() != null) {
            Typeface typeface = mCountryCodePicker.getTypeFace();
            mTvTitle.setTypeface(typeface);
            mEdtSearch.setTypeface(typeface);
            mTvNoResult.setTypeface(typeface);
        }
        if (mCountryCodePicker.getBackgroundColor() != mCountryCodePicker.getDefaultBackgroundColor()) {
            mRlyDialog.setBackgroundColor(mCountryCodePicker.getBackgroundColor());
        }

        if (mCountryCodePicker.getTextColor() != mCountryCodePicker.getDefaultContentColor()) {
            int color = mCountryCodePicker.getTextColor();
            mTvTitle.setTextColor(color);
            mTvNoResult.setTextColor(color);
            mEdtSearch.setTextColor(color);
            mEdtSearch.setHintTextColor(adjustAlpha(color, 0.7f));
        }

        mCountryCodePicker.refreshCustomMasterList();
        mCountryCodePicker.refreshPreferredCountries();
        masterCountries = mCountryCodePicker.getCustomCountries(mCountryCodePicker);

        CountryCodeAdapter.Callback callback = new CountryCodeAdapter.Callback() {
            @Override
            public void onItemCountrySelected(Country country) {
                mCountryCodePicker.setSelectedCountry(country);
                //if (view != null && mCountries.get(position) != null) {
                mInputMethodManager.hideSoftInputFromWindow(mEdtSearch.getWindowToken(), 0);
                CountryCodeDialog.this.dismiss();
            }
        };

        this.mFilteredCountries = getFilteredCountries();

        mAdapter = new CountryCodeAdapter(mFilteredCountries, mCountryCodePicker, callback);
        if (!mCountryCodePicker.isSelectionDialogShowSearch()) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) mRvCountryDialog.getLayoutParams();
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT;
            mRvCountryDialog.setLayoutParams(params);
        }
        mRvCountryDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvCountryDialog.setAdapter(mAdapter);

        mInputMethodManager = (InputMethodManager) mCountryCodePicker.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        setSearchBar();
    }

    void reShow() {
        setupData();
        show();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void setSearchBar() {
        if (mCountryCodePicker.isSelectionDialogShowSearch()) {
            setTextWatcher();
        } else {
            mEdtSearch.setVisibility(View.GONE);
        }
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private void setTextWatcher() {
        if (mEdtSearch != null) {
            mEdtSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyQuery(s.toString());
                }
            });

            if (mCountryCodePicker.isKeyboardAutoPopOnSearch()) {
                if (mInputMethodManager != null) {
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private void applyQuery(String query) {
        mTvNoResult.setVisibility(View.GONE);
        query = query.toLowerCase();

        //if query started from "+" ignore it
        if (query.length() > 0 && query.charAt(0) == '+') {
            query = query.substring(1);
        }

        mFilteredCountries = getFilteredCountries(query);

        if (mFilteredCountries.size() == 0) {
            mTvNoResult.setVisibility(View.VISIBLE);
        }

        mAdapter.notifyDataSetChanged();
    }

    private List<Country> getFilteredCountries() {
        return getFilteredCountries("");
    }

    private List<Country> getFilteredCountries(String query) {
        if (mTempCountries == null) {
            mTempCountries = new ArrayList<>();
        } else {
            mTempCountries.clear();
        }

        List<Country> preferredCountries = mCountryCodePicker.getPreferredCountries();
        if (preferredCountries != null && preferredCountries.size() > 0) {
            for (Country country : preferredCountries) {
                if (country.isEligibleForQuery(query)) {
                    mTempCountries.add(country);
                }
            }

            if (mTempCountries.size() > 0) { //means at least one preferred country is added.
                mTempCountries.add(null); // this will add separator for preference countries.
            }
        }

        for (Country country : masterCountries) {
            if (country.isEligibleForQuery(query)) {
                mTempCountries.add(country);
            }
        }
        return mTempCountries;
    }
}
