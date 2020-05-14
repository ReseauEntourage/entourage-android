package social.entourage.android.api.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by mihaiionescu on 25/02/16.
 */
public class ChatMessage extends TimestampedObject implements Serializable {

    private final static String HASH_STRING_HEAD = "ChatMessage-";
    private static final long serialVersionUID = 2171009008739523540L;

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VISIT = "visit";
    public static final String TYPE_OUTING = "outing";
    public static final String TYPE_STATUS_UPDATE = "status_update";

    @Expose(serialize = false)
    @SerializedName("id")
    private long chatId;

    private String content;

    @Expose(serialize = false)
    @SerializedName("created_at")
    @NotNull
    private Date creationDate;

    @Expose(serialize = false)
    private User user;

    @Expose(serialize = false, deserialize = false)
    private boolean isMe;

    @Expose(serialize = false)
    @SerializedName("message_type")
    private String messageType;

    private Metadata metadata;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public ChatMessage(String message) {
        this.content = message;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public long getChatId() {
        return chatId;
    }

    public void setChatId(final long chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @NonNull
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(@NonNull final Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getUserId() {
        if (user != null) {
            return user.getId();
        }
        return 0;
    }

    public String getUserAvatarURL() {
        if (user == null) return null;
        return user.getAvatarURL();
    }

    public String getUserName() {
        if (user == null) return "";
        return user.getDisplayName();
    }

    public String getPartnerLogoSmall() {
        if (user == null || user.getPartner() == null) return null;
        return user.getPartner().getSmallLogoUrl();
    }

    public boolean isMe() {
        return isMe;
    }

    public void setIsMe(final boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessageType() {
        return messageType;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    @NotNull
    public Date getTimestamp() {
        return creationDate;
    }

    @NonNull
    @Override
    public String hashString() {
        return HASH_STRING_HEAD + chatId;
    }

    @Override
    public boolean equals(final Object o) {
        return !(o == null || o.getClass() != this.getClass()) && this.chatId == ((ChatMessage) o).chatId;
    }

    @Override
    public int getType() {
        if (TYPE_OUTING.equalsIgnoreCase(messageType)) return CHAT_MESSAGE_OUTING;
        if (TYPE_STATUS_UPDATE.equalsIgnoreCase(messageType)) return STATUS_UPDATE_CARD;
        return isMe ? CHAT_MESSAGE_ME : CHAT_MESSAGE_OTHER;
    }

    @Override
    public long getId() {
        return chatId;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public static class Metadata implements Serializable {

        private static final long serialVersionUID = 5065260171819947605L;

        public static final String OPERATION_CREATED = "created";
        public static final String OPERATION_UPDATED = "updated";

        private String uuid;

        private String title;

        private String operation;

        @SerializedName("starts_at")
        private Date startsAt;

        @SerializedName("display_address")
        private String displayAddress;

        private String status;

        @SerializedName("outcome_success")
        private boolean outcomeSuccess;

        public String getUUID() {
            return uuid;
        }

        public String getTitle() {
            return title;
        }

        public String getOperation() {
            return operation;
        }

        public Date getStartsAt() {
            return startsAt;
        }

        public String getDisplayAddress() {
            return displayAddress;
        }

        public String getStatus() {
            return status;
        }

        public boolean isOutcomeSuccess() {
            return outcomeSuccess;
        }
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class ChatMessageWrapper {

        @SerializedName("chat_message")
        private ChatMessage chatMessage;

        public ChatMessage getChatMessage() {
            return chatMessage;
        }

        public void setChatMessage(final ChatMessage chatMessage) {
            this.chatMessage = chatMessage;
        }

    }

    public static class ChatMessagesWrapper {

        @SerializedName("chat_messages")
        private List<ChatMessage> chatMessages;

        public List<ChatMessage> getChatMessages() {
            return chatMessages;
        }

        public void setChatMessages(List<ChatMessage> chatMessages) {
            this.chatMessages = chatMessages;
        }

    }

}
