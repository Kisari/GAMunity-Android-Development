package rmitcom.asm1.gamunity.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Message implements Serializable {
    private String messageContent;
    private String messageOwnerId;
    private Timestamp timestamp;

    public Message() {
    }

    public Message(String messageContent, String messageOwnerId, Timestamp timestamp) {
        this.messageContent = messageContent;
        this.messageOwnerId = messageOwnerId;
        this.timestamp = timestamp;
    }

    public String getMessageOwnerId() {
        return messageOwnerId;
    }

    public void setMessageOwnerId(String messageOwnerId) {
        this.messageOwnerId = messageOwnerId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

}
