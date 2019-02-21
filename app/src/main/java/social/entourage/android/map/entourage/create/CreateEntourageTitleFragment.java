package social.entourage.android.map.entourage.create;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import social.entourage.android.api.model.map.BaseEntourage;
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

    @BindView(R.id.title_entourage_info_text)
    TextView infoTextView;

    @BindView(R.id.title_entourage_error)
    View errorView;

    private String entourageTitle;
    private EntourageCategory entourageCategory;
    private String entourageGroupType;

    private CreateEntourageListener mListener;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public CreateEntourageTitleFragment() {
        // Required empty public constructor
    }

    public static CreateEntourageTitleFragment newInstance(String title, EntourageCategory entourageCategory, String groupType) {
        CreateEntourageTitleFragment fragment = new CreateEntourageTitleFragment();
        Bundle args = new Bundle();
        args.putString(KEY_ENTOURAGE_TITLE, title);
        args.putSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY, entourageCategory);
        args.putString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE, groupType);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            entourageTitle = args.getString(KEY_ENTOURAGE_TITLE);
            entourageCategory = (EntourageCategory)args.getSerializable(EntourageCategoryFragment.KEY_ENTOURAGE_CATEGORY);
            entourageGroupType = args.getString(BaseCreateEntourageFragment.KEY_ENTOURAGE_GROUP_TYPE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_entourage_title, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
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

    @OnClick(R.id.title_close_button)
    void onCloseClicked() {
        mListener = null;
        dismiss();
    }

    @OnClick(R.id.title_action_button)
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
            titleEditText.setSelection(Math.min(entourageTitle.length(), TITLE_MAX_CHAR_COUNT));
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
                if (s.length() >= TITLE_MAX_CHAR_COUNT) {
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

        if (BaseEntourage.TYPE_OUTING.equalsIgnoreCase(entourageGroupType)) {
            titleEditText.setHint(R.string.entourage_title_fragment_hint_outing);
            infoTextView.setText(R.string.entourage_title_fragment_info_outing);
        }

    }
}
