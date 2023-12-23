package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class GroupChat implements Serializable {
    private String chatId;
    private ArrayList<String> memberIds;
    private String chatTitle;
    private String chatType;
    private ArrayList<String> messageIds;

    public GroupChat(String chatId, ArrayList<String> memberIds, String chatTitle, String chatType) {
        this.chatId = chatId;
        this.memberIds = memberIds;
        this.chatTitle = chatTitle;
        this.chatType = chatType;
    }

    public GroupChat(String chatId, ArrayList<String> memberIds, String chatTitle, String chatType, ArrayList<String> messageIds) {
        this.chatId = chatId;
        this.memberIds = memberIds;
        this.chatTitle = chatTitle;
        this.chatType = chatType;
        this.messageIds = messageIds;
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

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public ArrayList<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(ArrayList<String> messageIds) {
        this.messageIds = messageIds;
    }
}
