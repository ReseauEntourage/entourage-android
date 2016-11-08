package social.entourage.android.map.tour.join.received;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import javax.inject.Inject;

import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.message.push.PushNotificationService;
import social.entourage.android.user.UserFragment;
import social.entourage.android.view.HtmlTextView;

public class TourJoinRequestReceivedActivity extends EntourageSecuredActivity {

    private Message message;
    private boolean shouldDisplayMessage = true;
    private int requestsCount = 0;

    @Inject
    TourJoinRequestReceivedPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_join_request_received);

        message = (Message)getIntent().getExtras().getSerializable(PushNotificationService.PUSH_MESSAGE);
        if (message != null) {
            displayMessage();
        }
    }



    @Override
    protected void setupComponent(final EntourageComponent entourageComponent) {
        DaggerTourJoinRequestReceivedComponent.builder()
                .entourageComponent(entourageComponent)
                .tourJoinRequestReceivedModule(new TourJoinRequestReceivedModule(this))
                .build()
                .inject(this);
    }

    private void displayMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.layout_tour_join_request_received_dialog, null);
        HtmlTextView htmlTextView =  (HtmlTextView)view.findViewById(R.id.tour_join_request_received_text);
        String alertMessage = getString(R.string.message_unknown);
        PushNotificationContent content = message.getContent();
        if (content != null) {
            if (content.isEntourageRelated()) {
                alertMessage = getString(R.string.entourage_join_request_received_message_html, message.getAuthor());
            }
            else {
                alertMessage = getString(R.string.tour_join_request_received_message_html, message.getAuthor());
            }
        }
        htmlTextView.setHtmlString(alertMessage);
        htmlTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showUserProfile();
            }
        });
        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        PushNotificationContent content = message.getContent();
                        if (content != null) {
                            requestsCount++;
                            if (content.isTourRelated()) {
                                FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_ACCEPT);
                                presenter.acceptTourJoinRequest(content.getJoinableId(), content.getUserId());
                            }
                            else if (content.isEntourageRelated()) {
                                FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_ACCEPT);
                                presenter.acceptEntourageJoinRequest(content.getJoinableId(), content.getUserId());
                            }
                            else {
                                finish();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        PushNotificationContent content = message.getContent();
                        if (content != null) {
                            requestsCount++;
                            if (content.isTourRelated()) {
                                FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_REJECT);
                                presenter.rejectJoinTourRequest(content.getJoinableId(), content.getUserId());
                            }
                            else if (content.isEntourageRelated()) {
                                FlurryAgent.logEvent(Constants.EVENT_JOIN_REQUEST_REJECT);
                                presenter.rejectJoinEntourageRequest(content.getJoinableId(), content.getUserId());
                            }
                            else {
                                finish();
                            }
                        }
                    }
                })
                .setNeutralButton(R.string.user_view_profile, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                    }
                })
        ;
        if (requestsCount > 0) {
            builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    finish();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
        if (requestsCount == 0) {
            //Overriding the view profile handler immediately after show so that it doesn't close the alert
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    showUserProfile();
                }
            });
        }
    }

    protected void onUserTourStatusChanged(boolean statusChanged) {
        if (!statusChanged) {
            Toast.makeText(this, R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
            displayMessage();
        }
        else {
            Toast toast = Toast.makeText(this, R.string.tour_join_request_success, Toast.LENGTH_SHORT);
            int duration = toast.getDuration();
            toast.show();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(TourJoinRequestReceivedActivity.this, DrawerActivity.class));
                    finish();
                }
            }, duration+100);
        }
    }

    private void showUserProfile() {
        PushNotificationContent content = message.getContent();
        if (content == null) {
            return;
        }
        UserFragment fragment = UserFragment.newInstance(content.getUserId());
        fragment.show(getSupportFragmentManager(), UserFragment.TAG);
    }

}
