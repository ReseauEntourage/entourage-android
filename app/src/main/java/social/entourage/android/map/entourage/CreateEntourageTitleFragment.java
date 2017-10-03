package social.entourage.android.map.entourage;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.map.entourage.category.EntourageCategory;
import social.entourage.android.map.entourage.category.EntourageCategoryFragment;

/**
 * Dialog Fragment for editing the entourage title
 */
public class CreateEntourageTitleFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = CreateEntourageTitleFragment.class.getSimpleName();

    private static final String KEY_ENTOURAGE_TITLE = "KEY_ENTOURAGE_TITLE";

    private static final int TITLE_MAX_CHAR_COUNT = 100;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    @BindView(R.id.title_entourage_edittext)
    EditText titleEditText;

    @BindView(R.id.title_entourage_count)
    TextView titleCharCountTextView;

    @BindView(R.id.title_entourage_info)
    View infoView;

    @BindView(R.id.title_entourage_error)
    View errorView;

    private String entourageTitle;
    private EntourageCategory entourageCategory;

    private CreateEntourageListener mListener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageTitleFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageTitleFragment newInstance(String title, EntourageCategory entourageCategory) {
        CreateEntourageTitleFragment fragment = new CreateEntourageTitleFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ENTOURAGE_TITLE, title);
        args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, entourageCategory);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entourageTitle = getArguments().getString(KEY_ENTOURAGE_TITLE);
            entourageCategory = (EntourageCategory)getArguments().getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage_title, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showKeyboard();
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentFromRight;
    }

    public void setListener(final CreateEntourageListener mListener) {
        this.mListener = mListener;
    }

    // ----------------------------------
    // Interactions handling
    // ----------------------------------

    @OnClick(R.id.title_entourage_close)
    void onCloseClicked() {
        mListener = null;
        dismiss();
    }

    @OnClick(R.id.title_entourage_validate_button)
    void onValidateClicked() {
        if (mListener != null) {
            mListener.onTitleChanged(titleEditText.getText().toString());
        }
        mListener = null;
        dismiss();
    }

    // ----------------------------------
    // Private Methods
    // ----------------------------------

    private void initializeView() {

        if (entourageTitle != null) {
            titleEditText.setText(entourageTitle);
            titleEditText.setSelection(entourageTitle.length());
        }

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, s.length(), TITLE_MAX_CHAR_COUNT);
                titleCharCountTextView.setText(charCountString);
                if (s.length() == TITLE_MAX_CHAR_COUNT) {
                    titleCharCountTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.entourage_error, null));
                    infoView.setVisibility(View.GONE);
                    errorView.setVisibility(View.VISIBLE);
                } else {
                    titleCharCountTextView.setTextColor(ResourcesCompat.getColor(getResources(), R.color.entourage_ok, null));
                    infoView.setVisibility(View.VISIBLE);
                    errorView.setVisibility(View.GONE);
                }
            }
        });

        String charCountString = getContext().getString(R.string.entourage_create_title_char_count_format, titleEditText.length(), TITLE_MAX_CHAR_COUNT);
        titleCharCountTextView.setText(charCountString);

        if (entourageCategory != null) {
            String titleExample = entourageCategory.getTitleExample();
            if (titleExample != null && titleExample.length() > 0) {
                titleEditText.setHint(getString(R.string.entourage_create_title_hint, titleExample));
            }
        }

    }
}
