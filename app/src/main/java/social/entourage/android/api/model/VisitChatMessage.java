package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Mihai Ionescu on 06/06/2018.
 */

public class VisitChatMessage {

    public static final String TYPE_VISIT = "visit";

    @SerializedName("message_type")
    String messageType;

    VisitChatMetadata metadata;

    public VisitChatMessage(String messageType, Date visitedAt) {
        this.messageType = messageType;
        this.metadata = new VisitChatMetadata(visitedAt);
    }

    public static class VisitChatMetadata {

        @SerializedName("visited_at")
        Date visitedAt;

        public VisitChatMetadata(Date visitedAt) {
            this.visitedAt = visitedAt;
        }

    }

    public static class VisitChatMessageWrapper {

        @SerializedName("chat_message")
        VisitChatMessage visitChatMessage;

        public VisitChatMessageWrapper(VisitChatMessage visitChatMessage) {
            this.visitChatMessage = visitChatMessage;
        }

    }

}
