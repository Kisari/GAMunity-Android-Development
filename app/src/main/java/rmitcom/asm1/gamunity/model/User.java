package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class User implements Serializable {
    private String userId;
    private Boolean isAdmin = false;
    private String name;
    private String phone;
    private String email;
    private String profileImgUri;
    private ArrayList<String> ownedForumIds;
    private ArrayList<String> adminForumIds;
    private ArrayList<String> joinedForumIds;
    private ArrayList<String> postIds;
    private ArrayList<String> commentIds;
    private ArrayList<String> followersIds;
    private ArrayList<String> followingIds;
    private ArrayList<String> friendIds;
    private ArrayList<String> chatGroupIds;

    public User() {
    }

    public User(String userId, Boolean isAdmin, String name, String phone, String email) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public User(String userId, String name, String profileImgUri, ArrayList<String> chatGroupIds) {
        this.userId = userId;
        this.name = name;
        this.profileImgUri = profileImgUri;
        this.chatGroupIds = chatGroupIds;
    }

    public User(String userId, Boolean isAdmin, String name, String phone, String email, ArrayList<String> ownedForumIds, ArrayList<String> adminForumIds, ArrayList<String> joinedForumIds, ArrayList<String> postIds, ArrayList<String> commentIds, ArrayList<String> followersIds, ArrayList<String> followingIds, ArrayList<String> friendIds) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.ownedForumIds = ownedForumIds;
        this.adminForumIds = adminForumIds;
        this.joinedForumIds = joinedForumIds;
        this.postIds = postIds;
        this.commentIds = commentIds;
        this.followersIds = followersIds;
        this.followingIds = followingIds;
        this.friendIds = friendIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImgUri() {
        return profileImgUri;
    }

    public void setProfileImgUri(String profileImgUri) {
        this.profileImgUri = profileImgUri;
    }

    public ArrayList<String> getOwnedForumIds() {
        return ownedForumIds;
    }

    public void setOwnedForumIds(ArrayList<String> ownedForumIds) {
        this.ownedForumIds = ownedForumIds;
    }

    public ArrayList<String> getAdminForumIds() {
        return adminForumIds;
    }

    public void setAdminForumIds(ArrayList<String> adminForumIds) {
        this.adminForumIds = adminForumIds;
    }

    public ArrayList<String> getJoinedForumIds() {
        return joinedForumIds;
    }

    public void setJoinedForumIds(ArrayList<String> joinedForumIds) {
        this.joinedForumIds = joinedForumIds;
    }

    public ArrayList<String> getPostIds() {
        return postIds;
    }

    public void setPostIds(ArrayList<String> postIds) {
        this.postIds = postIds;
    }

    public ArrayList<String> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(ArrayList<String> commentIds) {
        this.commentIds = commentIds;
    }

    public ArrayList<String> getFollowersIds() {
        return followersIds;
    }

    public void setFollowersIds(ArrayList<String> followersIds) {
        this.followersIds = followersIds;
    }

    public ArrayList<String> getFollowingIds() {
        return followingIds;
    }

    public void setFollowingIds(ArrayList<String> followingIds) {
        this.followingIds = followingIds;
    }

    public ArrayList<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(ArrayList<String> friendIds) {
        this.friendIds = friendIds;
    }

    public ArrayList<String> getChatGroupIds() {
        return chatGroupIds;
    }

    public void setChatGroupIds(ArrayList<String> chatGroupIds) {
        this.chatGroupIds = chatGroupIds;
    }
}
