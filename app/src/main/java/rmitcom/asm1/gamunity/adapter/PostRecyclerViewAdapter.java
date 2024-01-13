package rmitcom.asm1.gamunity.adapter;

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

    @Override
    public void onBindViewHolder(@NonNull PostRecyclerViewHolder holder, int position) {
        Post currPost = postContent.get(position);
        Log.i("TAG", "onBindViewHolder - post: " + position + " - " + currPost.getTitle());

        String format = "dd/MM/yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

        postId = currPost.getPostId();

        holder.title.setText(currPost.getTitle());
        holder.like.setText((int) currPost.getNoLike() + "");
        holder.dislike.setText((int) currPost.getNoDislike() + "");
        holder.comment.setText((int) currPost.getNoComment() + "");

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

                            userImgUri = document.getString("image");
                            if (userImgUri != null) {
                                try {
                                    new AsyncImage(holder.userImage, holder.userProgressBar).loadImage(userImgUri);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return postContent.size();
    }

    public PostRecyclerViewAdapter(Context context, ArrayList<Post> postContent) {
        this.context = context;
        this.postContent = postContent;
    }

    public class PostRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView title, username, timestamp, like, dislike, comment;
        public RelativeLayout imageLayout;
        public ProgressBar userProgressBar, postProgressBar;
        public ImageView postImage;
        public ShapeableImageView userImage;

        public PostRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.postTabTitle);
            username = itemView.findViewById(R.id.postTabUserName);
            timestamp = itemView.findViewById(R.id.postTabTimestamp);
            like = itemView.findViewById(R.id.postTabLike);
            dislike = itemView.findViewById(R.id.postTabDislike);
            comment = itemView.findViewById(R.id.postTabComment);

            imageLayout = itemView.findViewById(R.id.postTabPicture);
            userProgressBar = itemView.findViewById(R.id.postTabProgressBar1);
            postProgressBar = itemView.findViewById(R.id.postTabProgressBar2);
            postImage =itemView.findViewById(R.id.postTabImage);
            userImage = itemView.findViewById(R.id.postTabUserProfile);

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
}
