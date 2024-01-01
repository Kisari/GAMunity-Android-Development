package rmitcom.asm1.gamunity.model;

import com.google.type.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class Post implements Serializable {
    private String postId;
    private String ownerId;
    private String forumId;
    private String title;
    private String description;
    private Date timestamp;
    private ArrayList<String> likeIds, dislikeIds, commentIds;
    private long noLike = 0;
    private long noDislike = 0;
    private long noComment = 0;

    public Post(String postId, String ownerId, String forumId, String title, String description, Date timestamp) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    public Post(String postId, String ownerId, String forumId, String title, String description, Date timestamp, long noLike, long noDislike, ArrayList<String> commentIds, long noComment) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.noLike = noLike;
        this.noDislike = noDislike;
        this.commentIds = commentIds;
        this.noComment = noComment;
    }

    public Post(String postId, String ownerId, String forumId, String title, Date timestamp) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.timestamp = timestamp;
    }

    public Post(String postId, String ownerId, String title) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.title = title;
    }

    public Post(String postId, String ownerId, String forumId, String title, Date timestamp, long noLike, long noDislike, long noComment) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.timestamp = timestamp;
        this.noLike = noLike;
        this.noDislike = noDislike;
        this.noComment = noComment;
    }

    public Post(String postId, String ownerId, String forumId, String title, long noLike, long noDislike, long noComment) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.noLike = noLike;
        this.noDislike = noDislike;
        this.noComment = noComment;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getNoLike() {
        return noLike;
    }

    public void setNoLike(long noLike) {
        this.noLike = noLike;
    }

    public long getNoDislike() {
        return noDislike;
    }

    public void setNoDislike(long noDislike) {
        this.noDislike = noDislike;
    }

    public ArrayList<String> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(ArrayList<String> commentIds) {
        this.commentIds = commentIds;
    }

    public long getNoComment() {
        return noComment;
    }

    public void setNoComment(long noComment) {
        this.noComment = noComment;
    }

    public ArrayList<String> getLikeIds() {
        return likeIds;
    }

    public void setLikeIds(ArrayList<String> likeIds) {
        this.likeIds = likeIds;
    }

    public ArrayList<String> getDislikeIds() {
        return dislikeIds;
    }

    public void setDislikeIds(ArrayList<String> dislikeIds) {
        this.dislikeIds = dislikeIds;
    }
}
