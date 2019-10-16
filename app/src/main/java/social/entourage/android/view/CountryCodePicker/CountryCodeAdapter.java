package social.entourage.android.view.CountryCodePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import social.entourage.android.R;

class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> {

    private List<Country> mCountries;
    private CountryCodePicker mCountryCodePicker;
    private Callback mCallback;

    interface Callback {
	    void onItemCountrySelected(Country country);
    }

    CountryCodeAdapter(List<Country> countries, CountryCodePicker codePicker, Callback callback) {
	    this.mCountries = countries;
	    this.mCountryCodePicker = codePicker;
	    this.mCallback = callback;
    }

    @NonNull
    @Override public CountryCodeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
	    LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
	    View rootView = inflater.inflate(R.layout.layout_code_picker_tile, viewGroup, false);
        return new CountryCodeViewHolder(rootView);
    }

    @Override public void onBindViewHolder(@NonNull CountryCodeViewHolder viewHolder, final int i) {
	    final int position = viewHolder.getAdapterPosition();
	    viewHolder.setCountry(mCountries.get(position));
	    viewHolder.rlyMain.setOnClickListener(new View.OnClickListener() {
	        @Override public void onClick(View view) {
		        mCallback.onItemCountrySelected(mCountries.get(position));
	        }
	    });
    }

    @Override public int getItemCount() {
	return mCountries.size();
  }

    class CountryCodeViewHolder extends RecyclerView.ViewHolder {
	    RelativeLayout rlyMain;
	    AppCompatTextView tvName, tvCode;
	    View viewDivider;

        CountryCodeViewHolder(View itemView) {
            super(itemView);
            rlyMain = (RelativeLayout) itemView;
            tvName = rlyMain.findViewById(R.id.country_name_tv);
            tvCode = rlyMain.findViewById(R.id.code_tv);
            viewDivider = rlyMain.findViewById(R.id.preference_divider_view);
        }

        private void setCountry(Country country) {
            if (country != null) {
                viewDivider.setVisibility(View.GONE);
                tvName.setVisibility(View.VISIBLE);
                tvCode.setVisibility(View.VISIBLE);
                String countryNameAndCode = tvName.getContext()
                    .getString(R.string.country_name_and_code, country.getName(),
                        country.getIso().toUpperCase());
                tvName.setText(countryNameAndCode);
                /*
                if (!mCountryCodePicker.isHidePhoneCode()) {
                  tvCode.setText(
                      tvCode.getContext().getString(R.string.phone_code, country.getPhoneCode()));
                } else {
                  tvCode.setVisibility(View.GONE);
                }
                */
                tvCode.setText(
                        tvCode.getContext().getString(R.string.phone_code, country.getPhoneCode()));
                if (mCountryCodePicker.getTypeFace() != null) {
                  tvCode.setTypeface(mCountryCodePicker.getTypeFace());
                  tvName.setTypeface(mCountryCodePicker.getTypeFace());
                }

                if (mCountryCodePicker.getTextColor() != mCountryCodePicker.getDefaultContentColor()) {
                  int color = mCountryCodePicker.getTextColor();
                  tvCode.setTextColor(color);
                  tvName.setTextColor(color);
                }
            } else {
                viewDivider.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.GONE);
                tvCode.setVisibility(View.GONE);
            }
        }
    }
}

