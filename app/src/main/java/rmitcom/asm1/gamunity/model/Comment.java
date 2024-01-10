package rmitcom.asm1.gamunity.model;

import java.util.ArrayList;
import java.util.Date;

public class Comment {
    private String commentId;
    private String ownerId;
    private String postId;
    private String description;
    private String repliedCommentId;
    private Date timestamp;
    private Date updateTimestamp;
    private ArrayList<String> likeIds, dislikeIds, replyCommentIds;
    private boolean isReply;
    private long noLike = 0;
    private long noDislike = 0;
    private long noComment = 0;

//    //Create comment constructor
//    public Comment(String commentId, String ownerId, String postId, String description, Date timestamp) {
//        this.commentId = commentId;
//        this.ownerId = ownerId;
//        this.postId = postId;
//        this.description = description;
//        this.timestamp = timestamp;
//    }
//
//    public Comment(String commentId, String ownerId, String postId, String description, Date timestamp, ArrayList<String> likeIds, ArrayList<String> dislikeIds, long noLike, long noDislike, long noComment) {
//        this.commentId = commentId;
//        this.ownerId = ownerId;
//        this.postId = postId;
//        this.description = description;
//        this.timestamp = timestamp;
//        this.likeIds = likeIds;
//        this.dislikeIds = dislikeIds;
//        this.noLike = noLike;
//        this.noDislike = noDislike;
//        this.noComment = noComment;
//    }
//
//    //Update comment constructor
//    public Comment(String commentId, String ownerId, String postId, String description, String repliedCommentId, Date timestamp, Date updateTimestamp) {
//        this.commentId = commentId;
//        this.ownerId = ownerId;
//        this.postId = postId;
//        this.description = description;
//        this.repliedCommentId = repliedCommentId;
//        this.timestamp = timestamp;
//        this.updateTimestamp = updateTimestamp;
//    }
//
//    //Reply comment constructor
//    public Comment(String commentId, String ownerId, String postId, String description, String repliedCommentId, ArrayList<String> replyCommentIds, boolean isReply) {
//        this.commentId = commentId;
//        this.ownerId = ownerId;
//        this.postId = postId;
//        this.description = description;
//        this.repliedCommentId = repliedCommentId;
//        this.replyCommentIds = replyCommentIds;
//        this.isReply = isReply;
//    }

    public Comment(String commentId, String ownerId, String postId, String description, String repliedCommentId, Date timestamp, Date updateTimestamp, ArrayList<String> likeIds, ArrayList<String> dislikeIds, ArrayList<String> replyCommentIds, boolean isReply, long noLike, long noDislike, long noComment) {
        this.commentId = commentId;
        this.ownerId = ownerId;
        this.postId = postId;
        this.description = description;
        this.repliedCommentId = repliedCommentId;
        this.timestamp = timestamp;
        this.updateTimestamp = updateTimestamp;
        this.likeIds = likeIds;
        this.dislikeIds = dislikeIds;
        this.replyCommentIds = replyCommentIds;
        this.isReply = isReply;
        this.noLike = noLike;
        this.noDislike = noDislike;
        this.noComment = noComment;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepliedCommentId() {
        return repliedCommentId;
    }

    public void setRepliedCommentId(String repliedCommentId) {
        this.repliedCommentId = repliedCommentId;
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

    public ArrayList<String> getReplyCommentIds() {
        return replyCommentIds;
    }

    public void setReplyCommentIds(ArrayList<String> replyCommentIds) {
        this.replyCommentIds = replyCommentIds;
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean reply) {
        isReply = reply;
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

    public long getNoComment() {
        return noComment;
    }

    public void setNoComment(long noComment) {
        this.noComment = noComment;
    }
}
