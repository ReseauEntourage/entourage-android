package social.entourage.android.privateCircle;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.PrivateCircleRequest;
import social.entourage.android.api.model.ChatMessage;
import social.entourage.android.api.model.VisitChatMessage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * A {@link EntourageDialogFragment} subclass, to select the date of a neighborhood visit
 * Use the {@link PrivateCircleDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrivateCircleDateFragment extends EntourageDialogFragment implements CalendarView.OnDateChangeListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = PrivateCircleDateFragment.class.getSimpleName();

    private static final int ROW_TODAY = 0;
    private static final int ROW_YESTERDAY = 1;
    private static final int ROW_OTHER = 2;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @BindView(R.id.title_action_button)
    TextView actionButton;

    @BindView(R.id.privatecircle_date_today)
    TextView dateTodayTextView;

    @BindView(R.id.privatecircle_date_today_checkBox)
    CheckBox dateTodayCB;

    @BindView(R.id.privatecircle_date_yesterday)
    TextView dateYesterdayTextView;

    @BindView(R.id.privatecircle_date_yesterday_checkBox)
    CheckBox dateYesterdayCB;

    @BindView(R.id.privatecircle_date_other)
    TextView dateOtherTextView;

    @BindView(R.id.privatecircle_date_other_checkBox)
    CheckBox dateOtherCB;

    @BindView(R.id.privatecircle_calendarView)
    CalendarView calendarView;

    private int selectedRow = ROW_TODAY;

    private long entourageId = 0;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public PrivateCircleDateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param entourageId Entourage ID.
     * @return A new instance of fragment PrivateCircleDateFragment.
     */
    public static PrivateCircleDateFragment newInstance(long entourageId) {
        PrivateCircleDateFragment fragment = new PrivateCircleDateFragment();
        Bundle args = new Bundle();
        args.putLong(FeedItem.KEY_FEEDITEM_ID, entourageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entourageId = getArguments().getLong(FeedItem.KEY_FEEDITEM_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_privatecircle_date, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureView();
    }

    private void configureView() {
        //initialise the calendar with current day - 2 days
        long now = System.currentTimeMillis();
        long other = now - 2 * 24 * 60 * 60 * 1000;
        calendarView.setDate(other);
        calendarView.setOnDateChangeListener(this);
        calendarView.setVisibility(View.GONE);
        //select the today by default
        selectRow(selectedRow, true);
    }

    // ----------------------------------
    // Buttons Handling
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onCloseButtonClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onNextButtonClicked() {
        Date visitDate = null;
        switch (selectedRow) {
            case ROW_TODAY:
                visitDate = new Date();
                break;
            case ROW_YESTERDAY:
                visitDate = new Date(System.currentTimeMillis() - 24*60*60*1000);
                break;
            case ROW_OTHER:
                visitDate = new Date(calendarView.getDate());
                break;
        }
        if (visitDate != null) {
            sendVisitDate(visitDate);
        }
    }

    @OnClick({
            R.id.privatecircle_date_today_holder, R.id.privatecircle_date_yesterday_holder, R.id.privatecircle_date_other_holder,
            R.id.privatecircle_date_today_checkBox, R.id.privatecircle_date_yesterday_checkBox, R.id.privatecircle_date_other_checkBox
    })
    protected void onRowClicked(View view) {
        String tag = (String) view.getTag();
        if (tag == null) return;
        int position = Integer.parseInt(tag);
        if (position != selectedRow) {
            selectRow(selectedRow, false);
            selectedRow = position;
            selectRow(selectedRow, true);

            calendarView.setVisibility(position == ROW_OTHER ? View.VISIBLE : View.GONE);
        } else {
            if (view instanceof CheckBox) {
                ((CheckBox)view).setChecked(true);
            }
        }
    }

    // ----------------------------------
    // Rows Handling
    // ----------------------------------

    private void selectRow(int row, boolean select) {
        switch (row) {
            case ROW_TODAY:
                if (select) {
                    dateTodayTextView.setTypeface(dateTodayTextView.getTypeface(), Typeface.BOLD);
                } else {
                    dateTodayTextView.setTypeface(Typeface.create(dateTodayTextView.getTypeface(), Typeface.NORMAL));
                }
                dateTodayCB.setChecked(select);
                break;
            case ROW_YESTERDAY:
                if (select) {
                    dateYesterdayTextView.setTypeface(dateYesterdayTextView.getTypeface(), Typeface.BOLD);
                } else {
                    dateYesterdayTextView.setTypeface(Typeface.create(dateYesterdayTextView.getTypeface(), Typeface.NORMAL));
                }
                dateYesterdayCB.setChecked(select);
                break;
            case ROW_OTHER:
                if (select) {
                    dateOtherTextView.setTypeface(dateOtherTextView.getTypeface(), Typeface.BOLD);
                } else {
                    dateOtherTextView.setTypeface(Typeface.create(dateOtherTextView.getTypeface(), Typeface.NORMAL));
                }
                dateOtherCB.setChecked(select);
                break;
        }
    }

    // ----------------------------------
    // CalendarView.OnDateChangeListener
    // ----------------------------------

    @Override
    public void onSelectedDayChange(@NonNull final CalendarView view, final int year, final int month, final int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, dayOfMonth);
        view.setDate(calendar.getTimeInMillis());
        dateOtherTextView.setText(DateFormat.getDateInstance().format(calendar.getTime()));
    }

    // ----------------------------------
    // API Calls
    // ----------------------------------

    private void sendVisitDate(Date visitDate) {
        actionButton.setEnabled(false);
        PrivateCircleRequest request = EntourageApplication.get().getEntourageComponent().getPrivateCircleRequest();
        VisitChatMessage visitChatMessage = new VisitChatMessage(VisitChatMessage.TYPE_VISIT, visitDate);
        Call<ChatMessage.ChatMessageWrapper> call = request.visitMessage(entourageId, new VisitChatMessage.VisitChatMessageWrapper(visitChatMessage));
        call.enqueue(new Callback<ChatMessage.ChatMessageWrapper>() {
            @Override
            public void onResponse(final Call<ChatMessage.ChatMessageWrapper> call, final Response<ChatMessage.ChatMessageWrapper> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EntourageApplication.get(), R.string.privatecircle_visit_sent_ok, Toast.LENGTH_SHORT).show();
                    if (!isStateSaved() && isAdded()) {
                        // pop to the menu, if the screen it is still displayed
                        dismiss();
                        PrivateCircleChooseFragment privateCircleChooseFragment = (PrivateCircleChooseFragment)getFragmentManager().findFragmentByTag(PrivateCircleChooseFragment.TAG);
                        if (privateCircleChooseFragment != null) {
                            privateCircleChooseFragment.dismiss();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), R.string.privatecircle_visit_sent_error, Toast.LENGTH_SHORT).show();
                    }
                }
                actionButton.setEnabled(true);
            }

            @Override
            public void onFailure(final Call<ChatMessage.ChatMessageWrapper> call, final Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), R.string.privatecircle_visit_sent_error, Toast.LENGTH_SHORT).show();
                }
                actionButton.setEnabled(true);
            }
        });
    }

}
