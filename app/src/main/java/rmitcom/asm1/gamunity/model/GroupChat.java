package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class GroupChat implements Serializable {
    private String chatId;
    private ArrayList<String> memberIds;
    private String chatTitle;
    private boolean isGroup;
    private ArrayList<String> messageIds;
    private Date lastTimestamp;
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

    public String getChatTitle() {
        return chatTitle;
    }

    public void setChatTitle(String chatTitle) {
        this.chatTitle = chatTitle;
    }

    public boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
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
