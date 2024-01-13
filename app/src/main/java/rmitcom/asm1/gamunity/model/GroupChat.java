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

    public GroupChat(ArrayList<String> memberIds, String chatTitle, boolean isGroup, Date lastTimestamp, String lastMessageSenderId) {
        this.memberIds = memberIds;
        this.chatTitle = chatTitle;
        this.isGroup = isGroup;
        this.lastTimestamp = lastTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public GroupChat(String chatId, ArrayList<String> memberIds, String chatTitle, boolean isGroup, Date lastTimestamp, String lastMessageSenderId) {
        this.chatId = chatId;
        this.memberIds = memberIds;
        this.chatTitle = chatTitle;
        this.isGroup = isGroup;
        this.lastTimestamp = lastTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
    }

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
}
