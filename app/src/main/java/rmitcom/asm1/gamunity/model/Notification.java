package rmitcom.asm1.gamunity.model;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Notification implements Serializable {
    private String notificationId;
    private String receiverToken;
    private String notificationTitle;
    private String notificationSenderUrl;
    private String notificationBody;
    private String notificationSenderId;
    private String notificationReceiverId;
    private Boolean notificationIsRead;
    private String sendTime;

    public Notification(String notificationTitle, String notificationSenderUrl, String notificationBody, String notificationSenderId, String notificationReceiverId, Boolean notificationIsRead, String sendTime) {
        this.notificationTitle = notificationTitle;
        this.notificationSenderUrl = notificationSenderUrl;
        this.notificationBody = notificationBody;
        this.notificationSenderId = notificationSenderId;
        this.notificationReceiverId = notificationReceiverId;
        this.notificationIsRead = notificationIsRead;
        this.sendTime = sendTime;
    }

    public Notification(String notificationId, String receiverToken, String notificationTitle, String notificationSenderUrl, String notificationBody, String notificationSenderId, String notificationReceiverId, Boolean notificationIsRead, String sendTime) {
        this.notificationId = notificationId;
        this.receiverToken = receiverToken;
        this.notificationTitle = notificationTitle;
        this.notificationSenderUrl = notificationSenderUrl;
        this.notificationBody = notificationBody;
        this.notificationSenderId = notificationSenderId;
        this.notificationReceiverId = notificationReceiverId;
        this.notificationIsRead = notificationIsRead;
        this.sendTime = sendTime;
    }

    public String getNotificationSenderUrl() {
        return notificationSenderUrl;
    }

    public void setNotificationSenderUrl(String notificationSenderUrl) {
        this.notificationSenderUrl = notificationSenderUrl;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getReceiverToken() {
        return receiverToken;
    }

    public void setReceiverToken(String receiverToken) {
        this.receiverToken = receiverToken;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationBody() {
        return notificationBody;
    }

    public void setNotificationBody(String notificationBody) {
        this.notificationBody = notificationBody;
    }

    public String getNotificationSenderId() {
        return notificationSenderId;
    }

    public void setNotificationSenderId(String notificationSenderId) {
        this.notificationSenderId = notificationSenderId;
    }

    public String getNotificationReceiverId() {
        return notificationReceiverId;
    }

    public void setNotificationReceiverId(String notificationReceiverId) {
        this.notificationReceiverId = notificationReceiverId;
    }

    public Boolean getNotificationIsRead() {
        return notificationIsRead;
    }

    public void setNotificationIsRead(Boolean notificationIsRead) {
        this.notificationIsRead = notificationIsRead;
    }
    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }
}
