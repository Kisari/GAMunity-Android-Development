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

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.fragments.HomeFragment;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.model.User;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.UserRecyclerViewHolder>{
    private final Context context;
    private ArrayList<User> userContent;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData;
    private String usernameStr, userProfileImgUri, forumId, otherUserId;
    private ArrayList<String> userIds;
    private boolean toAdmin, isChangeRole;

    public UserRecyclerViewAdapter(Context context, ArrayList<User> userContent, boolean isChangeRole, boolean toAdmin, String forumId) {
        this.context = context;
        this.userContent = userContent;
        this.isChangeRole = isChangeRole;
        this.toAdmin = toAdmin;
        this.forumId = forumId;
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
        RelativeLayout userInfo;
        Button userButton;
        ImageView baseImage;
        public UserRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.userName);
            userProgressBar = itemView.findViewById(R.id.memberProgressBar1);
            userImage = itemView.findViewById(R.id.memberUserProfile);
            userInfo = itemView.findViewById(R.id.userInfo);
            userButton = itemView.findViewById(R.id.userButton);
            baseImage = itemView.findViewById(R.id.baseImg);

            if (isChangeRole && toAdmin || isChangeRole || toAdmin) {
                userButton.setVisibility(View.VISIBLE);
            } else {
                userButton.setVisibility(View.GONE);
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

            holder.userInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accessInfoPage(currUser);
                }
            });

            holder.userButton.setOnClickListener(v -> {
                confirmChangeRole(currUser);
//                    notifyDataSetChanged();

                Intent returnIntent = new Intent(context, ForumView.class);
                returnIntent.putExtra("forumId", forumId);

                ((Activity) context).setResult(Activity.RESULT_OK, returnIntent);
                ((Activity) context).finish();
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void confirmChangeRole(User currUser) {
        String currUserId = currUser.getUserId();

        ArrayList<String> joinedIds = currUser.getJoinedForumIds();
        ArrayList<String> adminIds = currUser.getAdminForumIds();

        if (isChangeRole) {
            if (toAdmin) { //Change user from member to moderator
                joinedIds.remove(forumId);
                adminIds.add(forumId);

                if (forumId != null) {
                    DocumentReference forumData = db.collection("FORUMS").document(forumId);
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
                    userData.update("joinedForumIds", FieldValue.arrayRemove(forumId));
                    userData.update("adminForumIds", FieldValue.arrayUnion(forumId));
                }

                notifyDataSetChanged();
            }
            else { //Change user from moderator to member
                joinedIds.add(forumId);
                adminIds.remove(forumId);

                if (forumId != null) {
                    DocumentReference forumData = db.collection("FORUMS").document(forumId);
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
                    userData.update("joinedForumIds", FieldValue.arrayUnion(forumId));
                    userData.update("adminForumIds", FieldValue.arrayRemove(forumId));
                }

                notifyDataSetChanged();
            }
        }
        else {
            if (toAdmin) { //For remove user from forum
                joinedIds.remove(forumId);
                adminIds.remove(forumId);

                if (forumId != null) {
                    DocumentReference forumData = db.collection("FORUMS").document(forumId);
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
                                    userData.update("joinedForumIds", FieldValue.arrayRemove(forumId));
                                    userData.update("adminForumIds", FieldValue.arrayRemove(forumId));

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

//                                        chatData.update("memberIds", FieldValue.arrayRemove(currUserId));
//                                        chatData.update("moderatorIds", FieldValue.arrayRemove(currUserId));

                                    }

                                }
                            }
                        }
                    });
                }

                notifyDataSetChanged();
            }
        }

        int position = userContent.indexOf(currUser);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    private void accessInfoPage(User currUser) {
        if (!isChangeRole && !toAdmin) {
            Intent accessIntent;
            if (forumId != null) {
                accessIntent = new Intent(context, HomeFragment.class);
            }
            else {
                accessIntent = new Intent(context, ChatView.class);
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

//                Map<String, Object> newChatroom = new HashMap<>();
//
//                db.collection("CHATROOMS")
//                        .add(newChatroom)
//                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentReference> task) {
//                                if(task.isSuccessful()) {
//                                    String newChatId = task.getResult().getId();
//
//                                    Map<String, String> chatroomId = new HashMap<>();
//                                    chatroomId.put("chatId", newChatId);
//                                }
//                            }
//                        });

                accessIntent.putExtra("chatId", chatId);
                accessIntent.putExtra("isGroup", false);
                accessIntent.putExtra("dataId", currUser.getUserId());
//                accessIntent.putExtra("dataName", currUser.getName());
//                accessIntent.putExtra("dataImg", currUser.getProfileImgUri());


            }
            context.startActivity(accessIntent);
            ((Activity) context).finish();
        }
    }
}
