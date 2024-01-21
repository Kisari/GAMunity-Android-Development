package rmitcom.asm1.gamunity.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.fragments.HomeFragment;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.components.views.post.PostView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.model.User;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserRecyclerViewHolder>{
    private final Context context;
    private ArrayList<User> userContent;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData;
    private String usernameStr, userProfileImgUri, dataId, otherUserId;
    private ArrayList<String> userIds;
    private boolean toAdmin, isChangeRole, isChat;

    public UserRecyclerViewAdapter(Context context, ArrayList<User> userContent, boolean isChangeRole, boolean toAdmin, boolean isChat, String dataId) {
        this.context = context;
        this.userContent = userContent;
        this.isChangeRole = isChangeRole;
        this.toAdmin = toAdmin;
        this.isChat = isChat;
        this.dataId = dataId;
    }

    @NonNull
    @Override
    public UserRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_member_list_view_item, parent, false);
        return new UserRecyclerViewAdapter.UserRecyclerViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return userContent.size();
    }

    public class UserRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ProgressBar userProgressBar;
        ShapeableImageView userImage;
        RelativeLayout userInfo, userImg;
        Button userButton;
        ImageView baseImage;
        public UserRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.userName);
            userProgressBar = itemView.findViewById(R.id.memberProgressBar1);
            userImage = itemView.findViewById(R.id.memberUserProfile);
            userInfo = itemView.findViewById(R.id.userInfo);
            userImg = itemView.findViewById(R.id.userImg);
            userButton = itemView.findViewById(R.id.userButton);
            baseImage = itemView.findViewById(R.id.baseImg);

//            if (isChangeRole && toAdmin || isChangeRole || toAdmin) {
//                userButton.setVisibility(View.VISIBLE);
//            } else {
//                userButton.setVisibility(View.GONE);
//            }

            if (!isChangeRole && !toAdmin) {
                userButton.setVisibility(View.GONE);
            }
            else {
                userButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerViewHolder holder, int position) {
        User currUser = userContent.get(position);

        if (currUser != null) {
            usernameStr = currUser.getName();
            userProfileImgUri = currUser.getProfileImgUri();

            if (usernameStr != null) {
                holder.username.setText(usernameStr);
            }

            if (userProfileImgUri != null) {
                holder.baseImage.setVisibility(View.INVISIBLE);
                holder.userProgressBar.setVisibility(View.VISIBLE);
                holder.userImage.setVisibility(View.VISIBLE);
                try {
                    new AsyncImage(holder.userImage, holder.userProgressBar).loadImage(userProfileImgUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                holder.baseImage.setVisibility(View.VISIBLE);
                holder.userProgressBar.setVisibility(View.INVISIBLE);
                holder.userImage.setVisibility(View.INVISIBLE);
            }

            holder.userButton.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View v) {
                    executeAction(currUser);
//                    notifyDataSetChanged();
                }
            });

//            holder.userInfo.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!isChangeRole && toAdmin && isChat) {
//                        executeAction(currUser);
//                    }
//                }
//            });

            holder.userImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accessInfoPage(currUser);
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void executeAction(User currUser) {
        String currUserId = currUser.getUserId();

        ArrayList<String> joinedIds = currUser.getJoinedForumIds();
        ArrayList<String> adminIds = currUser.getAdminForumIds();

        ArrayList<String> chatgroupIds = currUser.getChatGroupIds();

        if (isChangeRole) {
            if (toAdmin) {
                //Add user to a chat group
                if (isChat) {
                    DocumentReference chatData = db.collection("CHATROOMS").document(dataId);
                    chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (document.exists()) {
                                    boolean isGroup = document.getBoolean("isGroup");

                                    if (!isGroup) {
                                        ArrayList<String> adminIds = new ArrayList<>(), userIds = new ArrayList<>(), tempUserIds, allUserIds = new ArrayList<>();

                                        if (document.get("adminIds") != null) {
                                            tempUserIds = (ArrayList) document.get("adminIds");
                                            adminIds.add(userId);
                                            allUserIds.add(userId);

                                            if (tempUserIds != null) {
                                                for (String id: tempUserIds) {
                                                    if (!Objects.equals(id, userId)) {
                                                        Log.i("TAG", "chatAddView - userId: " + id);
                                                        userIds.add(id);
                                                        allUserIds.add(id);
                                                    }
                                                }
                                            }

                                            userIds.add(currUserId);
                                            allUserIds.add(currUserId);

                                            Log.i("TAG", "chatAddView - adminIds: " + tempUserIds);
                                            Log.i("TAG", "chatAddView - current user chose: " + currUserId);
                                            Log.i("TAG", "chatAddView - userIds: " + userIds);
                                            Log.i("TAG", "chatAddView - all user: " + allUserIds);
                                        }

                                        final int size = allUserIds.size();
                                        AtomicInteger counter = new AtomicInteger(0);

                                        Log.i("TAG", "chatAddView - userList size: " + size);

                                        Map<String, Object> newChatRoom = new HashMap<>();
                                        newChatRoom.put("chatTitle", "Group chat");
                                        newChatRoom.put("chatImg", null);
                                        newChatRoom.put("isGroup", true);
                                        newChatRoom.put("lastMessageSenderId", "");
                                        newChatRoom.put("lastTimestamp", Timestamp.now());
                                        newChatRoom.put("adminIds", adminIds);
                                        newChatRoom.put("memberIds", userIds);

                                        final String[] chatId = {""};
                                        db.collection("CHATROOMS").add(newChatRoom).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    chatId[0] = task.getResult().getId();
                                                    Log.i("TAG", "chatAddView getChatId: " + chatId[0]);

                                                    for (String id: allUserIds) {
                                                        db.collection("users").document(id).update("chatGroupIds", FieldValue.arrayUnion(chatId[0]));
                                                        counter.incrementAndGet();
                                                        Log.i("TAG", "chatAddView counter: " + counter.get());

                                                        if (counter.get() == size) {
                                                            Log.i("TAG", "chatAddView call Intent: called");
                                                            Intent chatIntent = new Intent(context, ChatView.class);
                                                            chatIntent.putExtra("chatId", chatId[0]);
                                                            Log.i("TAG", "chatAddView - chatId: " + chatId[0]);
                                                            chatIntent.putExtra("isGroup", true);
                                                            chatIntent.putExtra("dataId", "");
//                                                            ((Activity) context).startActivity(chatIntent);
                                                            ((Activity) context).setResult(Activity.RESULT_OK, chatIntent);
                                                            ((Activity) context).finish();
                                                        }
                                                    }
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        String data = document.getString("dataId");
                                        if (data == null) {
                                            chatData.update("memberIds", FieldValue.arrayUnion(currUserId));

                                            db.collection("users").document(currUserId).update("chatGroupIds", FieldValue.arrayUnion(dataId));

                                            Intent chatIntent = new Intent(context, ChatView.class);
                                            chatIntent.putExtra("chatId", dataId);
                                            Log.i("TAG", "chatAddView - chatId: " + dataId);
                                            chatIntent.putExtra("isGroup", true);
                                            chatIntent.putExtra("dataId", "");
//                                            ((Activity) context).startActivity(chatIntent);
                                            ((Activity) context).setResult(Activity.RESULT_OK, chatIntent);
                                            ((Activity) context).finish();
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                //Promote user in forum
                else {
                    joinedIds.remove(dataId);
                    adminIds.add(dataId);

                    DocumentReference forumData = db.collection("FORUMS").document(dataId);
                    forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                ArrayList<String> memberIds, moderatorIds;

                                if (document.exists()) {
                                    memberIds = new ArrayList<>();
                                    moderatorIds = new ArrayList<>();

                                    Map<String, ArrayList<String>> memberList = new HashMap<>();
                                    Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                    if (document.get("memberIds") != null) {
                                        memberIds = (ArrayList<String>) document.get("memberIds");

                                        if (memberIds != null) {
                                            memberIds.remove(currUserId);
                                            memberList.put("memberIds", memberIds);
                                            forumData.set(memberList, SetOptions.merge());
                                        }
                                    }

                                    if (document.get("moderatorIds") != null) {
                                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                        if (moderatorIds != null) {
                                            moderatorIds.add(currUserId);
                                            moderatorList.put("moderatorIds", moderatorIds);
                                            forumData.set(moderatorList, SetOptions.merge());
                                        }
                                    }
//                                    forumData.update("memberIds", FieldValue.arrayUnion(currUserId));
//                                    forumData.update("moderatorIds", FieldValue.arrayRemove(currUserId));

                                    String chatId = document.getString("chatId");
                                    Log.i("confirmChangeRole", "chatId: " + chatId);
                                    if (chatId != null) {
                                        DocumentReference chatData = db.collection("CHATROOMS").document(chatId);
                                        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    if (document.exists()) {
                                                        ArrayList<String> memberIds = new ArrayList<>();
                                                        ArrayList<String> moderatorIds = new ArrayList<>();

                                                        Map<String, ArrayList<String>> memberList = new HashMap<>();
                                                        Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                                        if (document.get("memberIds") != null) {
                                                            memberIds = (ArrayList<String>) document.get("memberIds");

                                                            if (memberIds != null) {
                                                                memberIds.remove(currUserId);
                                                                memberList.put("memberIds", memberIds);
                                                                chatData.set(memberList, SetOptions.merge());
                                                            }
                                                        }

                                                        if (document.get("moderatorIds") != null) {
                                                            moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                                            if (moderatorIds != null) {
                                                                moderatorIds.add(currUserId);
                                                                moderatorList.put("moderatorIds", moderatorIds);
                                                                chatData.set(moderatorList, SetOptions.merge());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
//                                        chatData.update("memberIds", FieldValue.arrayUnion(currUserId));
//                                        chatData.update("moderatorIds", FieldValue.arrayRemove(currUserId));
                                    }
                                }
                            }
                        }
                    });

                    DocumentReference userData = db.collection("users").document(currUserId);
                    userData.update("joinedForumIds", FieldValue.arrayRemove(dataId));
                    userData.update("adminForumIds", FieldValue.arrayUnion(dataId));

                    notifyDataSetChanged();

                    Intent returnIntent = new Intent(context, ForumView.class);
                    returnIntent.putExtra("forumId", dataId);

                    ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
                    ((Activity) context).finish();

                }
            }
            else {
                //Remove user from chat group
                if (isChat) {
                    DocumentReference chatData = db.collection("CHATROOMS").document(dataId);

                    chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                ArrayList<String> memberIds, moderatorIds;

                                if (document.exists()) {
                                    memberIds = new ArrayList<>();
                                    moderatorIds = new ArrayList<>();

                                    Map<String, ArrayList<String>> memberList = new HashMap<>();
                                    Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                    if (document.get("memberIds") != null) {
                                        memberIds = (ArrayList<String>) document.get("memberIds");

                                        if (memberIds != null) {
                                            memberIds.remove(currUserId);
                                            memberList.put("memberIds", memberIds);
                                            chatData.set(memberList, SetOptions.merge());
                                        }
                                    }

                                    if (document.get("moderatorIds") != null) {
                                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                        if (moderatorIds != null) {
                                            moderatorIds.remove(currUserId);
                                            moderatorList.put("moderatorIds", moderatorIds);
                                            chatData.set(moderatorList, SetOptions.merge());
                                        }
                                    }

                                    DocumentReference userData = db.collection("users").document(currUserId);
                                    userData.update("chatGroupIds", FieldValue.arrayRemove(dataId));
                                }

                                Log.i("TAG", "chatAddView call Intent: called");
                                Intent chatIntent = new Intent(context, ChatView.class);
                                chatIntent.putExtra("chatId", dataId);
                                Log.i("TAG", "chatRemoveView - chatId: " + dataId);
                                chatIntent.putExtra("isGroup", true);
                                chatIntent.putExtra("dataId", "");
//                                ((Activity) context).startActivity(chatIntent);
                                ((Activity) context).setResult(Activity.RESULT_OK, chatIntent);
                                ((Activity) context).finish();
                            }
                        }
                    });

                }
                //Demote user in forum
                else {
                    joinedIds.add(dataId);
                    adminIds.remove(dataId);

                    DocumentReference forumData = db.collection("FORUMS").document(dataId);
                    forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                ArrayList<String> memberIds, moderatorIds;

                                if (document.exists()) {
                                    memberIds = new ArrayList<>();
                                    moderatorIds = new ArrayList<>();

                                    Map<String, ArrayList<String>> memberList = new HashMap<>();
                                    Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                    if (document.get("moderatorIds") != null) {
                                        moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                        if (moderatorIds != null) {
                                            moderatorIds.remove(currUserId);
                                            moderatorList.put("moderatorIds", moderatorIds);
                                            forumData.set(moderatorList, SetOptions.merge());
                                        }
                                    }

                                    if (document.get("memberIds") != null) {
                                        memberIds = (ArrayList<String>) document.get("memberIds");

                                        if (memberIds != null) {
                                            memberIds.add(currUserId);
                                            memberList.put("memberIds", memberIds);
                                            forumData.set(memberList, SetOptions.merge());
                                        }
                                    }
//                                    forumData.update("memberIds", FieldValue.arrayUnion(currUserId));
//                                    forumData.update("moderatorIds", FieldValue.arrayRemove(currUserId));

                                    String chatId = document.getString("chatId");
                                    Log.i("confirmChangeRole", "chatId: " + chatId);
                                    if (chatId != null) {
                                        DocumentReference chatData = db.collection("CHATROOMS").document(chatId);
                                        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    if (document.exists()) {
                                                        ArrayList<String> memberIds = new ArrayList<>();
                                                        ArrayList<String> moderatorIds = new ArrayList<>();

                                                        Map<String, ArrayList<String>> memberList = new HashMap<>();
                                                        Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                                        if (document.get("memberIds") != null) {
                                                            memberIds = (ArrayList<String>) document.get("memberIds");

                                                            if (memberIds != null) {
                                                                memberIds.add(currUserId);
                                                                memberList.put("memberIds", memberIds);
                                                                chatData.set(memberList, SetOptions.merge());
                                                            }
                                                        }

                                                        if (document.get("moderatorIds") != null) {
                                                            moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                                            if (moderatorIds != null) {
                                                                moderatorIds.remove(currUserId);
                                                                moderatorList.put("moderatorIds", moderatorIds);
                                                                chatData.set(moderatorList, SetOptions.merge());
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
//                                        chatData.update("memberIds", FieldValue.arrayUnion(currUserId));
//                                        chatData.update("moderatorIds", FieldValue.arrayRemove(currUserId));
                                    }
                                }
                            }
                        }
                    });

                    DocumentReference userData = db.collection("users").document(currUserId);
                    userData.update("joinedForumIds", FieldValue.arrayUnion(dataId));
                    userData.update("adminForumIds", FieldValue.arrayRemove(dataId));

                    notifyDataSetChanged();

                    Intent returnIntent = new Intent(context, ForumView.class);
                    returnIntent.putExtra("forumId", dataId);

                    ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
                    ((Activity) context).finish();
                }
            }
        }
        else {
            if (toAdmin) {
                //Chat Search User
                if (isChat) {
                    Intent accessIntent = new Intent(context, ChatView.class);
                    accessIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

                    String otherId = currUser.getUserId();
                    String chatId = "";

                    if (userId.hashCode() < otherId.hashCode()) {
                        chatId = userId + "_" + otherId;
                    }
                    else {
                        chatId = otherId + "_" + userId;
                    }

                    db.collection("users").document(userId)
                            .update("chatGroupIds", FieldValue.arrayUnion(chatId));
                    db.collection("users").document(currUser.getUserId())
                            .update("chatGroupIds", FieldValue.arrayUnion(chatId));

                    accessIntent.putExtra("chatId", chatId);
                    accessIntent.putExtra("isGroup", false);
                    accessIntent.putExtra("dataId", currUser.getUserId());

                    context.startActivity(accessIntent);
                    ((Activity) context).finish();

                }
                //Remove user
                else {
                    joinedIds.remove(dataId);
                    adminIds.remove(dataId);

                    if (dataId != null) {
                        DocumentReference forumData = db.collection("FORUMS").document(dataId);
                        forumData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    ArrayList<String> memberIds, moderatorIds;

                                    if (document.exists()) {
                                        memberIds = new ArrayList<>();
                                        moderatorIds = new ArrayList<>();

                                        Map<String, ArrayList<String>> memberList = new HashMap<>();
                                        Map<String, ArrayList<String>> moderatorList = new HashMap<>();
                                        if (document.get("memberIds") != null) {
                                            memberIds = (ArrayList<String>) document.get("memberIds");

                                            if (memberIds != null) {
                                                memberIds.remove(currUserId);
                                                memberList.put("memberIds", memberIds);
                                                forumData.set(memberList, SetOptions.merge());
                                            }
                                        }

                                        if (document.get("moderatorIds") != null) {
                                            moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                            if (moderatorIds != null) {
                                                moderatorIds.remove(currUserId);
                                                moderatorList.put("moderatorIds", moderatorIds);
                                                forumData.set(moderatorList, SetOptions.merge());
                                            }
                                        }
//                                    forumData.update("memberIds", FieldValue.arrayRemove(currUserId));
//                                    forumData.update("moderatorIds", FieldValue.arrayRemove(currUserId));

                                        DocumentReference userData = db.collection("users").document(currUserId);
                                        userData.update("joinedForumIds", FieldValue.arrayRemove(dataId));
                                        userData.update("adminForumIds", FieldValue.arrayRemove(dataId));

                                        String chatId = document.getString("chatId");
                                        Log.i("confirmChangeRole", "chatId: " + chatId);
                                        if (chatId != null) {
                                            DocumentReference chatData = db.collection("CHATROOMS").document(chatId);
                                            chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();

                                                        if (document.exists()) {
                                                            ArrayList<String> memberIds = new ArrayList<>();
                                                            ArrayList<String> moderatorIds = new ArrayList<>();

                                                            Map<String, ArrayList<String>> memberList = new HashMap<>();
                                                            Map<String, ArrayList<String>> moderatorList = new HashMap<>();

                                                            if (document.get("memberIds") != null) {
                                                                memberIds = (ArrayList<String>) document.get("memberIds");

                                                                if (memberIds != null) {
                                                                    memberIds.remove(currUserId);
                                                                    memberList.put("memberIds", memberIds);
                                                                    chatData.set(memberList, SetOptions.merge());
                                                                }
                                                            }

                                                            if (document.get("moderatorIds") != null) {
                                                                moderatorIds = (ArrayList<String>) document.get("moderatorIds");

                                                                if (moderatorIds != null) {
                                                                    moderatorIds.remove(currUserId);
                                                                    moderatorList.put("moderatorIds", moderatorIds);
                                                                    chatData.set(moderatorList, SetOptions.merge());
                                                                }
                                                            }

                                                            userData.update("chatGroupIds", FieldValue.arrayRemove(chatId));

                                                        }
                                                    }
                                                }
                                            });

                                        }

                                    }
                                }
                            }
                        });
                    }
                    notifyDataSetChanged();

                    Intent returnIntent = new Intent(context, ForumView.class);
                    returnIntent.putExtra("forumId", dataId);

                    ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
                    ((Activity) context).finish();
                }
            }
            else {
                if (isChat) { //Display chat info

                }
                else { //Display forum info

                }
            }
        }

        int position = userContent.indexOf(currUser);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    private void accessInfoPage(User currUser) {
        String currId = currUser.getUserId();
        Intent accessIntent = new Intent(context, ProfileView.class);
        accessIntent.putExtra("userId", currId);
        ((Activity)context).startActivity(accessIntent);
    }
}
