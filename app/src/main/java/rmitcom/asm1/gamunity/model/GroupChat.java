package rmitcom.asm1.gamunity.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class GroupChat implements Serializable {
    private String chatId;
    private String chatTitle;
    private String chatImage;
    private String dataId;
    private boolean isGroup;
    private ArrayList<String> memberIds, moderatorIds, adminIds;
    private Timestamp lastTimestamp;
    private String lastMessageSenderId;

    public GroupChat() {
    }

    public GroupChat(String chatId, String chatTitle, String chatImage, boolean isGroup, String dataId) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.chatImage = chatImage;
        this.isGroup = isGroup;
        this.dataId = dataId;
    }

    public GroupChat(String chatId, String chatTitle, String chatImage, boolean isGroup, String dataId, Timestamp lastTimestamp) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.chatImage = chatImage;
        this.isGroup = isGroup;
        this.dataId = dataId;
        this.lastTimestamp = lastTimestamp;
    }

    public GroupChat(String chatId, String chatTitle, String chatImage, String dataId, boolean isGroup, ArrayList<String> memberIds, ArrayList<String> moderatorIds, ArrayList<String> adminIds, Timestamp lastTimestamp, String lastMessageSenderId) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.chatImage = chatImage;
        this.dataId = dataId;
        this.isGroup = isGroup;
        this.memberIds = memberIds;
        this.moderatorIds = moderatorIds;
        this.adminIds = adminIds;
        this.lastTimestamp = lastTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public GroupChat(String chatId, String chatTitle, String chatImage, boolean isGroup, ArrayList<String> adminIds, Timestamp lastTimestamp, String lastMessageSenderId) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.chatImage = chatImage;
        this.isGroup = isGroup;
        this.adminIds = adminIds;
        this.lastTimestamp = lastTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

//    public GroupChat(String chatId, ArrayList<String> memberIds, ArrayList<String> moderatorIds, ArrayList<String> adminIds, String chatTitle, boolean isGroup, ArrayList<String> messageIds, Date lastTimestamp, String lastMessageSenderId, String chatImage, String forumId) {
//        this.chatId = chatId;
//        this.memberIds = memberIds;
//        this.moderatorIds = moderatorIds;
//        this.adminIds = adminIds;
//        this.chatTitle = chatTitle;
//        this.isGroup = isGroup;
//        this.messageIds = messageIds;
//        this.lastTimestamp = lastTimestamp;
//        this.lastMessageSenderId = lastMessageSenderId;
//        this.chatImage = chatImage;
//        this.forumId = forumId;
//    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }

    public ArrayList<String> getModeratorIds() {
        return moderatorIds;
    }

    public void setModeratorIds(ArrayList<String> moderatorIds) {
        this.moderatorIds = moderatorIds;
    }

    public ArrayList<String> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(ArrayList<String> adminIds) {
        this.adminIds = adminIds;
    }

    public String getChatTitle() {
        return chatTitle;
    }

    public void setChatTitle(String chatTitle) {
        this.chatTitle = chatTitle;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public Timestamp getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Timestamp lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getChatImage() {
        return chatImage;
    }

    public void setChatImage(String chatImage) {
        this.chatImage = chatImage;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public int compareTo(GroupChat otherGroupChat) {
        if (this.lastTimestamp == null && otherGroupChat.lastTimestamp == null) {
            return 0;

        } else if (this.lastTimestamp == null) {
            return 1;

        } else if (otherGroupChat.lastTimestamp == null) {
            return -1;

        } else {
            return this.lastTimestamp.compareTo(otherGroupChat.lastTimestamp);
        }
    }
}
