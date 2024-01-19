package rmitcom.asm1.gamunity.model;

import com.google.type.DateTime;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Message implements Serializable {
    private String messageId;
    private String messageOwnerId;
    private DateTime timestamp;
    private String messageContent;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageOwnerId() {
        return messageOwnerId;
    }

    public void setMessageOwnerId(String messageOwnerId) {
        this.messageOwnerId = messageOwnerId;
    }

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Message(String messageId, String messageOwnerId, DateTime timestamp, String messageContent) {
        this.messageId = messageId;
        this.messageOwnerId = messageOwnerId;
        this.timestamp = timestamp;
        this.messageContent = messageContent;
    }
}
