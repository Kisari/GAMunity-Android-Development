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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.components.views.profile.ProfileView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.User;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserRecyclerViewHolder>{
    private final Context context;
    private final ArrayList<User> userContent;
    private final FireBaseManager manager = new FireBaseManager();
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

            holder.userButton.setOnClickListener(v -> {
                executeAction(currUser);
            });


            holder.userImg.setOnClickListener(v -> accessInfoPage(currUser));
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
                    DocumentReference chatData = manager.getDb().collection("CHATROOMS").document(dataId);
                    chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        /** @noinspection DataFlowIssue, rawtypes */
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
                                            adminIds.add(manager.getCurrentUser().getUid());
                                            allUserIds.add(manager.getCurrentUser().getUid());

                                            if (tempUserIds != null) {
                                                for (String id: tempUserIds) {
                                                    if (!Objects.equals(id, manager.getCurrentUser().getUid())) {
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

                                        manager.getDb().collection("CHATROOMS").add(newChatRoom).addOnCompleteListener(task12 -> {
                                            if (task12.isSuccessful()) {
                                                String newChatId = task12.getResult().getId();
                                                Log.i("TAG", "chatAddView getChatId: " + newChatId);

                                                for (String id: allUserIds) {
                                                    manager.getDb().collection("users").document(id).update("chatGroupIds", FieldValue.arrayUnion(newChatId));
                                                    counter.incrementAndGet();
                                                    Log.i("TAG", "chatAddView counter: " + counter.get());

                                                    if (counter.get() == size) {
                                                        Log.i("TAG", "chatAddView call Intent: called");
                                                        Intent chatIntent = new Intent(context, ChatView.class);
                                                        chatIntent.putExtra("chatId", newChatId);
                                                        ((Activity) context).setResult(Activity.RESULT_OK, chatIntent);
                                                        ((Activity) context).finish();
                                                    }
                                                }
                                            }
                                        });

                                    }
                                    else {
                                        String data = document.getString("dataId");
                                        if (data == null) {
                                            chatData.update("memberIds", FieldValue.arrayUnion(currUserId));

                                            manager.getDb().collection("users").document(currUserId).update("chatGroupIds", FieldValue.arrayUnion(dataId));

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

                    DocumentReference forumData = manager.getDb().collection("FORUMS").document(dataId);
                    forumData.get().addOnCompleteListener(task -> {
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

                                String chatId = document.getString("chatId");
                                Log.i("confirmChangeRole", "chatId: " + chatId);
                                if (chatId != null) {
                                    DocumentReference chatData = manager.getDb().collection("CHATROOMS").document(chatId);
                                    chatData.get().addOnCompleteListener(task13 -> {
                                        if (task13.isSuccessful()) {
                                            DocumentSnapshot document12 = task13.getResult();

                                            if (document12.exists()) {
                                                ArrayList<String> memberIds12;
                                                ArrayList<String> moderatorIds12;

                                                Map<String, ArrayList<String>> memberList12 = new HashMap<>();
                                                Map<String, ArrayList<String>> moderatorList12 = new HashMap<>();

                                                if (document12.get("memberIds") != null) {
                                                    memberIds12 = (ArrayList<String>) document12.get("memberIds");

                                                    if (memberIds12 != null) {
                                                        memberIds12.remove(currUserId);
                                                        memberList12.put("memberIds", memberIds12);
                                                        chatData.set(memberList12, SetOptions.merge());
                                                    }
                                                }

                                                if (document12.get("moderatorIds") != null) {
                                                    moderatorIds12 = (ArrayList<String>) document12.get("moderatorIds");

                                                    if (moderatorIds12 != null) {
                                                        moderatorIds12.add(currUserId);
                                                        moderatorList12.put("moderatorIds", moderatorIds12);
                                                        chatData.set(moderatorList12, SetOptions.merge());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                    DocumentReference userData = manager.getDb().collection("users").document(currUserId);
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
                    DocumentReference chatData = manager.getDb().collection("CHATROOMS").document(dataId);

                    chatData.get().addOnCompleteListener(task -> {
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

                                DocumentReference userData = manager.getDb().collection("users").document(currUserId);
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
                    });

                }
                //Demote user in forum
                else {
                    joinedIds.add(dataId);
                    adminIds.remove(dataId);

                    DocumentReference forumData = manager.getDb().collection("FORUMS").document(dataId);
                    forumData.get().addOnCompleteListener(task -> {
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

                                String chatId = document.getString("chatId");
                                Log.i("confirmChangeRole", "chatId: " + chatId);
                                if (chatId != null) {
                                    DocumentReference chatData = manager.getDb().collection("CHATROOMS").document(chatId);
                                    chatData.get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            DocumentSnapshot document1 = task1.getResult();

                                            if (document1.exists()) {
                                                ArrayList<String> memberIds1 = new ArrayList<>();
                                                ArrayList<String> moderatorIds1 = new ArrayList<>();

                                                Map<String, ArrayList<String>> memberList1 = new HashMap<>();
                                                Map<String, ArrayList<String>> moderatorList1 = new HashMap<>();

                                                if (document1.get("memberIds") != null) {
                                                    memberIds1 = (ArrayList<String>) document1.get("memberIds");

                                                    if (memberIds1 != null) {
                                                        memberIds1.add(currUserId);
                                                        memberList1.put("memberIds", memberIds1);
                                                        chatData.set(memberList1, SetOptions.merge());
                                                    }
                                                }

                                                if (document1.get("moderatorIds") != null) {
                                                    moderatorIds1 = (ArrayList<String>) document1.get("moderatorIds");

                                                    if (moderatorIds1 != null) {
                                                        moderatorIds1.remove(currUserId);
                                                        moderatorList1.put("moderatorIds", moderatorIds1);
                                                        chatData.set(moderatorList1, SetOptions.merge());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                    DocumentReference userData = manager.getDb().collection("users").document(currUserId);
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

                    if (manager.getCurrentUser().getUid().hashCode() < otherId.hashCode()) {
                        chatId = manager.getCurrentUser().getUid() + "_" + otherId;
                    }
                    else {
                        chatId = otherId + "_" + manager.getCurrentUser().getUid();
                    }

                    manager.getDb().collection("users").document(manager.getCurrentUser().getUid())
                            .update("chatGroupIds", FieldValue.arrayUnion(chatId));
                    manager.getDb().collection("users").document(currUser.getUserId())
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
                        DocumentReference forumData = manager.getDb().collection("FORUMS").document(dataId);
                        forumData.get().addOnCompleteListener(task -> {
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

                                    DocumentReference userData = manager.getDb().collection("users").document(currUserId);
                                    userData.update("joinedForumIds", FieldValue.arrayRemove(dataId));
                                    userData.update("adminForumIds", FieldValue.arrayRemove(dataId));

                                    String chatId = document.getString("chatId");
                                    Log.i("confirmChangeRole", "chatId: " + chatId);
                                    if (chatId != null) {
                                        DocumentReference chatData = manager.getDb().collection("CHATROOMS").document(chatId);
                                        chatData.get().addOnCompleteListener(task14 -> {
                                            if (task14.isSuccessful()) {
                                                DocumentSnapshot document13 = task14.getResult();

                                                if (document13.exists()) {
                                                    ArrayList<String> memberIds13 = new ArrayList<>();
                                                    ArrayList<String> moderatorIds13 = new ArrayList<>();

                                                    Map<String, ArrayList<String>> memberList13 = new HashMap<>();
                                                    Map<String, ArrayList<String>> moderatorList13 = new HashMap<>();

                                                    if (document13.get("memberIds") != null) {
                                                        memberIds13 = (ArrayList<String>) document13.get("memberIds");

                                                        if (memberIds13 != null) {
                                                            memberIds13.remove(currUserId);
                                                            memberList13.put("memberIds", memberIds13);
                                                            chatData.set(memberList13, SetOptions.merge());
                                                        }
                                                    }

                                                    if (document13.get("moderatorIds") != null) {
                                                        moderatorIds13 = (ArrayList<String>) document13.get("moderatorIds");

                                                        if (moderatorIds13 != null) {
                                                            moderatorIds13.remove(currUserId);
                                                            moderatorList13.put("moderatorIds", moderatorIds13);
                                                            chatData.set(moderatorList13, SetOptions.merge());
                                                        }
                                                    }

                                                    userData.update("chatGroupIds", FieldValue.arrayRemove(chatId));

                                                }
                                            }
                                        });

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
