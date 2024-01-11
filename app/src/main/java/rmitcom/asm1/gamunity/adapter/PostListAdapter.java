package rmitcom.asm1.gamunity.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.model.Post;

public class PostListAdapter extends ArrayAdapter<Post> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference userData;
    private String postId;

    public PostListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public PostListAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public PostListAdapter(@NonNull Context context, int resource, @NonNull Post[] objects) {
        super(context, resource, objects);
    }

    public PostListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull Post[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public PostListAdapter(@NonNull Context context, int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);
    }

    public PostListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Post> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        TextView title, username, timestamp, like, dislike, comment;
        RelativeLayout imageLayout;
        ProgressBar userProgressBar, postProgressBar;
        ImageView postImage;
        ShapeableImageView userImage;

        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.ui_post_list_view_item, parent, false);
            } else {
                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            }
        }

        Post currPost = getItem(position);

        title = listItem.findViewById(R.id.postTabTitle);
        username = listItem.findViewById(R.id.postTabUserName);
        timestamp = listItem.findViewById(R.id.postTabTimestamp);
        like = listItem.findViewById(R.id.postTabLike);
        dislike = listItem.findViewById(R.id.postTabDislike);
        comment = listItem.findViewById(R.id.postTabComment);

        imageLayout = listItem.findViewById(R.id.postTabPicture);
        userProgressBar = listItem.findViewById(R.id.postTabProgressBar1);
        postProgressBar = listItem.findViewById(R.id.postTabProgressBar2);
        postImage =listItem.findViewById(R.id.postTabImage);
        userImage = listItem.findViewById(R.id.postTabUserProfile);

        if (currPost != null) {
            String format = "dd/MM/yyyy HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

            postId = currPost.getPostId();

            title.setText(currPost.getTitle());
            like.setText((int) currPost.getNoLike() + "");
            dislike.setText((int) currPost.getNoDislike() + "");
            comment.setText((int) currPost.getNoComment() + "");

            Date timestampDate = currPost.getTimestamp(), updateTimestampDate = currPost.getUpdateTimestamp();
            StringBuilder timestampStr = new StringBuilder();

            if (timestampDate != null) {
                timestampStr.append(sdf.format(timestampDate));
            }

            if (updateTimestampDate != null) {
                timestampStr.append(" (Edited: ").append(sdf.format(updateTimestampDate)).append(")");
            }

            timestamp.setText(timestampStr);

            String postImgUri = currPost.getImgUri();
            Log.i("Post Tab", "getView - : " + postImgUri);
            if (postImgUri != null) {
                try {
                    imageLayout.setVisibility(View.VISIBLE);
                    new AsyncImage(postImage, postProgressBar).loadImage(postImgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                imageLayout.setVisibility(View.GONE);
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
                                username.setText((String) document.get("name"));

                                userImgUri = document.getString("image");
                                if (userImgUri != null) {
                                    try {
                                        new AsyncImage(userImage, userProgressBar).loadImage(userImgUri);
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

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostView.class);
                if (currPost != null) {
                    intent.putExtra("postId", currPost.getPostId());
                    intent.putExtra("forumId", currPost.getForumId());
                }
                getContext().startActivity(intent);
            }
        });

        return listItem;
    }
}
