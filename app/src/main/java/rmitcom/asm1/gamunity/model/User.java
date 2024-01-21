package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class User implements Serializable {
    private String userId;
    private Boolean isAdmin = false;
    private String name;
    private String dob;
    private String email;
    private String profileImgUri;
    private ArrayList<String> ownedForumIds;
    private ArrayList<String> adminForumIds;
    private ArrayList<String> joinedForumIds;
    private ArrayList<String> postIds;
    private ArrayList<String> commentIds;
    private ArrayList<String> followersIds;
    private ArrayList<String> followingIds;
    private ArrayList<String> chatGroupIds;
    private String backgroundImgUri;


    public User(String profileImgUri, String backgroundImgUri, String name, String dob){
        this.profileImgUri = profileImgUri;
        this.backgroundImgUri = backgroundImgUri;
        this.name = name;
        this.dob = dob;
    }

    public User(String userId, Boolean isAdmin, String name, String dob, String email) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.dob = dob;
        this.email = email;
    }

    public User(String userId, String name, String profileImgUri, ArrayList<String> chatGroupIds) {
        this.userId = userId;
        this.name = name;
        this.profileImgUri = profileImgUri;
        this.chatGroupIds = chatGroupIds;
    }

    public User(String userId, String name, String profileImgUri) {
        this.userId = userId;
        this.name = name;
        this.profileImgUri = profileImgUri;
    }

    public User(String userId, String name, String profileImgUri, ArrayList<String> adminForumIds, ArrayList<String> joinedForumIds) {
        this.userId = userId;
        this.name = name;
        this.profileImgUri = profileImgUri;
        this.adminForumIds = adminForumIds;
        this.joinedForumIds = joinedForumIds;
    }

    public User(String userId, Boolean isAdmin, String name, String dob, String email, ArrayList<String> ownedForumIds, ArrayList<String> adminForumIds, ArrayList<String> joinedForumIds, ArrayList<String> postIds, ArrayList<String> commentIds, ArrayList<String> followersIds, ArrayList<String> followingIds) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.dob = dob;
        this.email = email;
        this.ownedForumIds = ownedForumIds;
        this.adminForumIds = adminForumIds;
        this.joinedForumIds = joinedForumIds;
        this.postIds = postIds;
        this.commentIds = commentIds;
        this.followersIds = followersIds;
        this.followingIds = followingIds;
    }

    public String getBackgroundImgUri() {
        return backgroundImgUri;
    }

    public void setBackgroundImgUri(String backgroundImgUri) {
        this.backgroundImgUri = backgroundImgUri;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
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

    public ArrayList<String> getChatGroupIds() {
        return chatGroupIds;
    }

    public void setChatGroupIds(ArrayList<String> chatGroupIds) {
        this.chatGroupIds = chatGroupIds;
    }
}