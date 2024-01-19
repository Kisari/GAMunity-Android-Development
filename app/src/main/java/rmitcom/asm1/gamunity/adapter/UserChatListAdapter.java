package rmitcom.asm1.gamunity.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
//import rmitcom.asm1.gamunity.model.GroupChat;
import rmitcom.asm1.gamunity.model.User;

public class UserChatListAdapter extends ArrayAdapter<User> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData;
    private boolean isChatGroup;
    public UserChatListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public UserChatListAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public UserChatListAdapter(@NonNull Context context, int resource, @NonNull User[] objects) {
        super(context, resource, objects);
    }

    public UserChatListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull User[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public UserChatListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects, boolean isChatGroup) {
        super(context, resource, objects);
        this.isChatGroup = isChatGroup;
    }

    public UserChatListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<User> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        TextView username;
        ProgressBar userProgressBar;
        ShapeableImageView userImage;

        String usernameStr, userProfileImgUri;
        ArrayList<String> chatMemberIds;

        if (listItem == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (getItemViewType(position) == 0) {
                listItem = inflater.inflate(R.layout.ui_post_list_view_item, parent, false);
            } else {
                Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
            }
        }

        User currUser = getItem(position);

        username = listItem.findViewById(R.id.userName);
        userProgressBar = listItem.findViewById(R.id.memberProgressBar1);
        userImage = listItem.findViewById(R.id.memberUserProfile);

//        if (currChat != null) {
//            chatMemberIds = currChat.getMemberIds();
//        }


        if (currUser != null) {
            usernameStr = currUser.getUserId();
            userProfileImgUri = currUser.getProfileImgUri();

            if (usernameStr != null) {
                username.setText(usernameStr);
            }

            if (userProfileImgUri != null) {
                try {
                    new AsyncImage(userImage, userProgressBar).loadImage(userProfileImgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ChatView.class);
                intent.putExtra("isGroup", false);
                if (currUser != null) {
                    intent.putExtra("otherUserId", currUser.getUserId());
                }
                getContext().startActivity(intent);
            }
        });

        return listItem;
    }
}
