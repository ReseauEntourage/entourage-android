package social.entourage.android.user.report;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.UserReport;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * User Report Fragment
 */
public class UserReportFragment extends EntourageDialogFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = UserReportFragment.class.getSimpleName();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private int userId;

    @BindView(R.id.user_report_reason_edittext)
    EditText reasonEditText;

    private boolean sending = false;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public UserReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId The id of the reported user.
     * @return A new instance of fragment UserReportFragment.
     */
    public static UserReportFragment newInstance(int userId) {
        UserReportFragment fragment = new UserReportFragment();
        Bundle args = new Bundle();
        args.putInt(User.KEY_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(User.KEY_USER_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_report, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showKeyboard();
    }

    @Override
    protected int getSlideStyle() {
        return R.style.CustomDialogFragmentSlide;
    }

    // ----------------------------------
    // ONCLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.user_report_close_button)
    protected void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.user_report_send_button)
    protected void onSendClicked() {
        if (sending) return;
        if (isValid()) {
            sendReport();
        }
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------

    private boolean isValid() {
        String reason = reasonEditText.getText().toString();
        if (reason == null || reason.trim().length() == 0) {
            // The reason cannot be empty
            Toast.makeText(getContext(), R.string.user_report_error_reason_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendReport() {
        if (EntourageApplication.get().getEntourageComponent() == null) return;
        UserRequest userRequest = EntourageApplication.get().getEntourageComponent().getUserRequest();
        if (userRequest == null) return;
        sending = true;
        String reason = reasonEditText.getText().toString();
        Call<ResponseBody> call = userRequest.reportUser(userId, new UserReport.UserReportWrapper(new UserReport(reason)));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull final Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), R.string.user_report_success, Toast.LENGTH_SHORT).show();
                    if (!isStopped()) {
                        dismiss();
                    }
                }
                else {
                    Toast.makeText(getActivity(), R.string.user_report_error_send_failed, Toast.LENGTH_SHORT).show();
                    sending = false;
                }
            }

            @Override
            public void onFailure(@NonNull final Call<ResponseBody> call, @NonNull final Throwable t) {
                Toast.makeText(getActivity(), R.string.user_report_error_send_failed, Toast.LENGTH_SHORT).show();
                sending = false;
            }
        });
    }

}
