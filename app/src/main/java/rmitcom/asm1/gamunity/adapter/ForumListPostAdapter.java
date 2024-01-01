package rmitcom.asm1.gamunity.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.views.PostView;
import rmitcom.asm1.gamunity.model.Post;

public class ForumListPostAdapter extends ArrayAdapter<Post> {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
    private DocumentReference forumData, userData;

    public ForumListPostAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ForumListPostAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ForumListPostAdapter(@NonNull Context context, int resource, @NonNull Post[] objects) {
        super(context, resource, objects);
    }

    public ForumListPostAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull Post[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ForumListPostAdapter(@NonNull Context context, int resource, @NonNull List<Post> objects) {
        super(context, resource, objects);
    }

    public ForumListPostAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Post> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        TextView title, username, timestamp, like, dislike, comment;

        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.activity_post_tab, parent, false);
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
            title.setText(currPost.getTitle());
            timestamp.setText(currPost.getTimestamp().toString());
            like.setText((int) currPost.getNoLike() + "");
            dislike.setText((int) currPost.getNoDislike() + "");
            comment.setText((int) currPost.getNoComment() + "");

            username.setText("testuser1");

//                String ownerId = currPost.getOwnerId();
//                userData = db.collection("USERS").document(ownerId);
//
//                userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//
//                            if (document != null) {
//                                username.setText((String) document.get("username"));
//                            }
//                        }
//                    }
//                });
        }


        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PostView.class);
                if (currPost != null) {
                    intent.putExtra("postId", currPost.getPostId());
                }
                getContext().startActivity(intent);
            }
        });

        return listItem;
    }
}
