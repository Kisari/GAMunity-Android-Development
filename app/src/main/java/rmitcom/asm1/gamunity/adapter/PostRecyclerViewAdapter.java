package rmitcom.asm1.gamunity.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Post;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostRecyclerViewHolder> {
    private final Context context;
    private ArrayList<Post> postContent;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference userData;
    private String postId;
    private Constant constant = new Constant();

    @NonNull
    @Override
    public PostRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_post_list_view_item, parent, false);
        return new PostRecyclerViewHolder(view);
    }

    public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView title, username, timestamp, like, likeTrue, dislike, dislikeTrue, comment;
        public RelativeLayout imageLayout;
        public ProgressBar userProgressBar, postProgressBar;
        public ImageView postImage, baseImage;
        public ShapeableImageView userImage;

        public PostRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.postTabTitle);
            username = itemView.findViewById(R.id.postTabUserName);
            timestamp = itemView.findViewById(R.id.postTabTimestamp);
            like = itemView.findViewById(R.id.postTabLike);
            likeTrue = itemView.findViewById(R.id.postTabLikeTrue);
            dislike = itemView.findViewById(R.id.postTabDislike);
            dislikeTrue = itemView.findViewById(R.id.postTabDislikeTrue);
            comment = itemView.findViewById(R.id.postTabComment);

            imageLayout = itemView.findViewById(R.id.postTabPicture);
            userProgressBar = itemView.findViewById(R.id.postTabProgressBar1);
            postProgressBar = itemView.findViewById(R.id.postTabProgressBar2);
            postImage =itemView.findViewById(R.id.postTabImage);
            userImage = itemView.findViewById(R.id.postTabUserProfile);

            baseImage = itemView.findViewById(R.id.baseImg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Post post = postContent.get(position);
                        navigateToPostView(post);
                    }
                }
            });
        }

        private void navigateToPostView(Post postContend) {
            Intent intent = new Intent(context, PostView.class);
            intent.putExtra("postId", postContend.getPostId());
            intent.putExtra("forumId", postContend.getForumId());
            ((Activity) context).startActivityForResult(intent, constant.DELETE);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PostRecyclerViewHolder holder, int position) {
        Post currPost = postContent.get(position);
        Log.i("TAG", "onBindViewHolder - post: " + position + " - " + currPost.getTitle());

        String format = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        postId = currPost.getPostId();

        holder.title.setText(currPost.getTitle());

        ArrayList<String> likeIds = currPost.getLikeIds(), dislikeIds = currPost.getDislikeIds(), commentIds = currPost.getCommentIds();
        int likeCount, dislikeCount, commentCount;

        if (likeIds != null) {
            likeCount = likeIds.size();

            if (likeIds.contains(userId)) {
                holder.like.setVisibility(View.INVISIBLE);
                holder.likeTrue.setVisibility(View.VISIBLE);

                holder.likeTrue.setText(String.valueOf(likeCount));
            } else {
                holder.like.setVisibility(View.VISIBLE);
                holder.likeTrue.setVisibility(View.INVISIBLE);

                holder.like.setText(String.valueOf(likeCount));
            }

        }
        else {
            likeCount = 0;

            holder.like.setVisibility(View.VISIBLE);
            holder.likeTrue.setVisibility(View.INVISIBLE);

            holder.like.setText(String.valueOf(likeCount));
        }

        if (dislikeIds != null) {
            dislikeCount = dislikeIds.size();

            if (dislikeIds.contains(userId)) {
                holder.dislike.setVisibility(View.INVISIBLE);
                holder.dislikeTrue.setVisibility(View.VISIBLE);

                holder.dislikeTrue.setText(String.valueOf(dislikeCount));
            } else {
                holder.dislike.setVisibility(View.VISIBLE);
                holder.dislikeTrue.setVisibility(View.INVISIBLE);

                holder.dislike.setText(String.valueOf(dislikeCount));
            }

        }
        else {
            dislikeCount = 0;

            holder.dislike.setVisibility(View.VISIBLE);
            holder.dislikeTrue.setVisibility(View.INVISIBLE);

            holder.dislike.setText(String.valueOf(dislikeCount));
        }

        if (commentIds != null) {
            commentCount = commentIds.size();
        }
        else {
            commentCount = 0;
        }

        holder.comment.setText(String.valueOf(commentCount));

        Date timestampDate = currPost.getTimestamp(), updateTimestampDate = currPost.getUpdateTimestamp();
        StringBuilder timestampStr = new StringBuilder();

        if (timestampDate != null) {
            timestampStr.append(sdf.format(timestampDate));
        }

        if (updateTimestampDate != null) {
            timestampStr.append(" (Edited: ").append(sdf.format(updateTimestampDate)).append(")");
        }

        holder.timestamp.setText(timestampStr);

        String postImgUri = currPost.getImgUri();
        Log.i("Post Tab", "getView - : " + postImgUri);
        if (postImgUri != null) {
            try {
                holder.imageLayout.setVisibility(View.VISIBLE);
                new AsyncImage(holder.postImage, holder.postProgressBar).loadImage(postImgUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imageLayout.setVisibility(View.GONE);
        }

        String ownerId = currPost.getOwnerId();
        Log.i("TAG", "ownerId: " + ownerId);

        if (ownerId != null) {
            userData = db.collection("users").document(ownerId);

            userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String userImgUri;
                        if (document != null) {
                            holder.username.setText((String) document.get("name"));

                            userImgUri = document.getString("profileImgUri");
                            if (userImgUri != null) {
                                try {
                                    holder.baseImage.setVisibility(View.INVISIBLE);
                                    holder.userProgressBar.setVisibility(View.VISIBLE);
                                    holder.userImage.setVisibility(View.VISIBLE);
                                    new AsyncImage(holder.userImage, holder.userProgressBar).loadImage(userImgUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                holder.baseImage.setVisibility(View.VISIBLE);
                                holder.userProgressBar.setVisibility(View.INVISIBLE);
                                holder.userImage.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            });
        }

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accessIntent = new Intent(context, ProfileView.class);
                accessIntent.putExtra("userId", userId);
                context.startActivity(accessIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postContent.size();
    }

    public PostRecyclerViewAdapter(Context context, ArrayList<Post> postContent) {
        this.context = context;
        this.postContent = postContent;
    }
}
