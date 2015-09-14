package social.entourage.android.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.authentication.login.LoginActivity;
import social.entourage.android.message.push.PushNotificationService;

public class MessageActivity extends EntourageSecuredActivity {

    @Inject
    MessagePresenter presenter;

    @Bind(R.id.message_author)
    TextView messageAuthor;

    @Bind(R.id.message_object)
    TextView messageObject;

    @Bind(R.id.message_content)
    TextView messageContent;

    @Bind(R.id.message_close_button)
    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        if (!getAuthenticationController().isAuthenticated()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        Message message = (Message) getIntent().getExtras().getSerializable(PushNotificationService.PUSH_MESSAGE);
        if (message != null) {
            displayMessage(message);
        }
    }

    @Override
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMessageComponent.builder()
                .entourageComponent(entourageComponent)
                .messageModule(new MessageModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Message message = (Message) intent.getExtras().getSerializable(PushNotificationService.PUSH_MESSAGE);
        if (message != null) {
            Log.d("notification:", message.getAuthor());
            displayMessage(message);
        }
    }

    @OnClick(R.id.message_close_button)
    void closeMessage() {
        onBackPressed();
    }

    private void displayMessage(Message message) {
        messageAuthor.setText(message.getAuthor());
        messageObject.setText(message.getObject());
        messageContent.setText(message.getContent());
    }

}
