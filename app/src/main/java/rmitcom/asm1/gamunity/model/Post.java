package rmitcom.asm1.gamunity.model;

import com.google.type.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class Post implements Serializable, Comparable<Post>   {
    private String postId;
    private String ownerId;
    private String forumId;
    private String title;
    private String description;
    private Date timestamp;
    private Date updateTimestamp;
    private String imgUri;
    private ArrayList<String> commentIds, likeIds, dislikeIds;

//    //Create Post constructor
//    public Post(String postId, String ownerId, String forumId, String title, String description, Date timestamp) {
//        this.postId = postId;
//        this.ownerId = ownerId;
//        this.forumId = forumId;
//        this.title = title;
//        this.description = description;
//        this.timestamp = timestamp;
//    }
//
//    //Update Post constructor
//    public Post(String postId, String ownerId, String forumId, String title, String description, Date timestamp, Date updateTimestamp) {
//        this.postId = postId;
//        this.ownerId = ownerId;
//        this.forumId = forumId;
//        this.title = title;
//        this.description = description;
//        this.timestamp = timestamp;
//        this.updateTimestamp = updateTimestamp;
//    }
//
//    public Post(String postId, String ownerId, String forumId, String title, Date timestamp, ArrayList<String> likeIds, ArrayList<String> dislikeIds, long noLike, long noDislike, long noComment) {
//        this.postId = postId;
//        this.ownerId = ownerId;
//        this.forumId = forumId;
//        this.title = title;
//        this.description = description;
//        this.timestamp = timestamp;
//        this.likeIds = likeIds;
//        this.dislikeIds = dislikeIds;
//        this.noLike = noLike;
//        this.noDislike = noDislike;
//        this.noComment = noComment;
//    }

    public Post(String postId, String ownerId, String forumId, String title, String description, Date timestamp, Date updateTimestamp, String imgUri, ArrayList<String> commentIds, ArrayList<String> likeIds, ArrayList<String> dislikeIds) {
        this.postId = postId;
        this.ownerId = ownerId;
        this.forumId = forumId;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.updateTimestamp = updateTimestamp;
        this.imgUri = imgUri;
        this.commentIds = commentIds;
        this.likeIds = likeIds;
        this.dislikeIds = dislikeIds;
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

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
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

    public ArrayList<String> getCommentIds() {
        return commentIds;
    }

    public void setCommentIds(ArrayList<String> commentIds) {
        this.commentIds = commentIds;
    }

    @Override
    public int compareTo(Post otherPost) {
        if (this.timestamp == null && otherPost.timestamp == null) {
            return 0;

        } else if (this.timestamp == null) {
            return 1;

        } else if (otherPost.timestamp == null) {
            return -1;

        } else {
            return this.timestamp.compareTo(otherPost.timestamp);

        }
    }
}
