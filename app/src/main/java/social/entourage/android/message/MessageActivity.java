package social.entourage.android.message;

import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Message;
import social.entourage.android.message.push.PushNotificationService;

public class MessageActivity extends EntourageSecuredActivity {

    @Inject
    MessagePresenter presenter;

    @InjectView(R.id.message_author)
    TextView messageAuthor;

    @InjectView(R.id.message_object)
    TextView messageObject;

    @InjectView(R.id.message_content)
    TextView messageContent;

    @InjectView(R.id.message_close_button)
    Button closeButton;

    private Message message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message);
        ButterKnife.inject(this);

        message = (Message) getIntent().getExtras().getSerializable(PushNotificationService.PUSH_MESSAGE);

        if (!getAuthenticationController().isAuthenticated()) {
            throw new IllegalArgumentException("You must be logged in");
        }

        messageAuthor.setText(message.getAuthor());
        messageObject.setText(message.getObject());
        messageContent.setText(message.getContent());

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

    @OnClick(R.id.message_close_button)
    void closeMessage() {
        onBackPressed();
    }

}
