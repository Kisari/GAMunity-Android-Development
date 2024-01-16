package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class GroupChat implements Serializable {
    private String chatId;
    private ArrayList<String> memberIds, moderatorIds, adminIds;
    private String chatTitle;
    private boolean isGroup;
    private ArrayList<String> messageIds;
    private Date lastTimestamp;
    private String lastMessageSenderId;
    private String chatImage;
    private String dataId;

    public GroupChat() {
    }

    public GroupChat(String chatId, String chatTitle, String chatImage, boolean isGroup, String dataId) {
        this.chatId = chatId;
        this.chatTitle = chatTitle;
        this.chatImage = chatImage;
        this.isGroup = isGroup;
        this.dataId = dataId;
    }

    public GroupChat(ArrayList<String> adminIds, String chatTitle, boolean isGroup, Date lastTimestamp, String lastMessageSenderId, String chatImage) {
        this.adminIds = adminIds;
        this.chatTitle = chatTitle;
        this.isGroup = isGroup;
        this.lastTimestamp = lastTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.chatImage = chatImage;
    }

    //
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

    public ArrayList<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(ArrayList<String> messageIds) {
        this.messageIds = messageIds;
    }

    public Date getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Date lastTimestamp) {
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
}
