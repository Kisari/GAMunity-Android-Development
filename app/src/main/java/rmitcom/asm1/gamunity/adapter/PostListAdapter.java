package rmitcom.asm1.gamunity.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rmitcom.asm1.gamunity.R;
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


        if (currPost != null) {
            String format = "dd/MM/yyyy HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

            postId = currPost.getPostId();

            title.setText(currPost.getTitle());
            like.setText((int) currPost.getNoLike() + "");
            dislike.setText((int) currPost.getNoDislike() + "");
            comment.setText((int) currPost.getNoComment() + "");

            Date timestampDate = currPost.getTimestamp();
            if (timestampDate != null) {
                timestamp.setText(sdf.format(timestampDate));
            } else {
                timestamp.setText("N/A");  // Or set it to some default value
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

                            if (document != null) {
                                username.setText((String) document.get("name"));
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
                    intent.putExtra("postId", postId);
                    intent.putExtra("forumId", currPost.getForumId());
                }
                getContext().startActivity(intent);
            }
        });

        return listItem;
    }

//    public void deletePost() {
//        PostView postView = new PostView();
//        postView.deletePost(postId);
//    }
}
