package rmitcom.asm1.gamunity.model;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class User implements Serializable {
    private String userId;
    private Boolean isAdmin = false;
    private String name;
    private String birthdate;
    private String email;
//    private boolean isLocated;
    private ArrayList<String> ownedForumIds;
    private ArrayList<String> adminForumIds;
    private ArrayList<String> joinedForumIds;
    private ArrayList<String> postIds;
    private ArrayList<String> commentIds;
    private ArrayList<String> followersIds;
    private long noFollowers = 0;
    private ArrayList<String> followingIds;
    private ArrayList<String> friendIds;

    public User() {
    }

    public User(String userId, Boolean isAdmin, String name, String birthdate, String email) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.birthdate = birthdate;
        this.email = email;
//        this.isLocated = isLocated;
    }

    public User(String userId, Boolean isAdmin, String name, String birthdate, String email, ArrayList<String> ownedForumIds, ArrayList<String> adminForumIds, ArrayList<String> joinedForumIds, ArrayList<String> postIds, ArrayList<String> commentIds, ArrayList<String> followersIds, long noFollowers, ArrayList<String> followingIds, ArrayList<String> friendIds) {
        this.userId = userId;
        this.isAdmin = isAdmin;
        this.name = name;
        this.birthdate = birthdate;
        this.email = email;
//        this.isLocated = isLocated;
        this.ownedForumIds = ownedForumIds;
        this.adminForumIds = adminForumIds;
        this.joinedForumIds = joinedForumIds;
        this.postIds = postIds;
        this.commentIds = commentIds;
        this.followersIds = followersIds;
        this.noFollowers = noFollowers;
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

    public String getbirthdate() {
        return birthdate;
    }

    public void setbirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public long getNoFollowers() {
        return noFollowers;
    }

    public void setNoFollowers(long noFollowers) {
        this.noFollowers = noFollowers;
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
}
