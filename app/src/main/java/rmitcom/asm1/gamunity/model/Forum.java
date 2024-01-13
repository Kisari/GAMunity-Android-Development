package rmitcom.asm1.gamunity.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class Forum implements Serializable {
    private String forumId;
    private String forumRef;
    private String chiefAdmin;
    private String title;
    private ArrayList<String> category;
    private String description;
    private ArrayList<String> moderatorIds;
    private ArrayList<String> memberIds;
    private long noJoined = 0;
    private ArrayList<String> postIds;
    private String forumBackground;
    private String forumIcon;
    private String chatId;

    public Forum(String forumId, String forumRef, String chiefAdmin, String title, ArrayList<String> category, String description, ArrayList<String> moderatorIds, ArrayList<String> memberIds, long noJoined, ArrayList<String> postIds, String forumBackground, String forumIcon, String chatId) {
        this.forumId = forumId;
        this.forumRef = forumRef;
        this.chiefAdmin = chiefAdmin;
        this.title = title;
        this.category = category;
        this.description = description;
        this.moderatorIds = moderatorIds;
        this.memberIds = memberIds;
        this.noJoined = noJoined;
        this.postIds = postIds;
        this.forumBackground = forumBackground;
        this.forumIcon = forumIcon;
        this.chatId = chatId;
    }

    public Forum(String forumId, String chiefAdmin, String title, ArrayList<String> category, String description, ArrayList<String> moderatorIds, ArrayList<String> memberIds, long noJoined, ArrayList<String> postIds, String forumBackground, String forumIcon) {
        this.forumId = forumId;
        this.chiefAdmin = chiefAdmin;
        this.title = title;
        this.category = category;
        this.description = description;
        this.moderatorIds = moderatorIds;
        this.memberIds = memberIds;
        this.noJoined = noJoined;
        this.postIds = postIds;
        this.forumBackground = forumBackground;
        this.forumIcon = forumIcon;
    }

    public Forum(String forumId, String chiefAdmin, String title, ArrayList<String> category, String description, ArrayList<String> moderatorIds, ArrayList<String> memberIds, long noJoined, ArrayList<String> postIds, String forumBackground, String forumIcon, String chatId) {
        this.forumId = forumId;
        this.chiefAdmin = chiefAdmin;
        this.title = title;
        this.category = category;
        this.description = description;
        this.moderatorIds = moderatorIds;
        this.memberIds = memberIds;
        this.noJoined = noJoined;
        this.postIds = postIds;
        this.forumBackground = forumBackground;
        this.forumIcon = forumIcon;
        this.chatId = chatId;
    }

    public Forum(String forumId, String forumRef, String chiefAdmin,String title, ArrayList<String> category, ArrayList<String> memberIds, String forumBackground, String forumIcon) {
        this.forumId = forumId;
        this.forumRef = forumRef;
        this.chiefAdmin = chiefAdmin;
        this.title = title;
        this.category = category;
        this.memberIds = memberIds;
        this.forumBackground = forumBackground;
        this.forumIcon = forumIcon;
    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public String getForumRef() {
        return forumRef;
    }

    public void setForumRef(String forumRef) {
        this.forumRef = forumRef;
    }

    public String getChiefAdmin() {
        return chiefAdmin;
    }

    public void setChiefAdmin(String chiefAdmin) {
        this.chiefAdmin = chiefAdmin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public void setCategory(ArrayList<String> category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getModeratorIds() {
        return moderatorIds;
    }

    public void setModeratorIds(ArrayList<String> moderatorIds) {
        this.moderatorIds = moderatorIds;
    }

    public ArrayList<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(ArrayList<String> memberIds) {
        this.memberIds = memberIds;
    }

    public long getNoJoined() {
        return noJoined;
    }

    public void setNoJoined(long noJoined) {
        this.noJoined = noJoined;
    }

    public ArrayList<String> getPostIds() {
        return postIds;
    }

    public void setPostIds(ArrayList<String> postIds) {
        this.postIds = postIds;
    }

    public String getForumBackground() {
        return forumBackground;
    }

    public void setForumBackground(String forumBackground) {
        this.forumBackground = forumBackground;
    }

    public String getForumIcon() {
        return forumIcon;
    }

    public void setForumIcon(String forumIcon) {
        this.forumIcon = forumIcon;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
