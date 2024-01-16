package rmitcom.asm1.gamunity.components.views.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.model.GroupChat;

public class ChatView extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private DocumentReference chatData, forumData, currUserData, otherUserData;
    private TextView chatTitle, returnBackBtn, accessBtn;
    private EditText inputMessage;
    private ImageView sendMessageBtn;
    private RecyclerView messageBody;
    private String inputMessageStr, chatId, forumId, otherUserId;
    private StringBuilder chatTitleStr;
    private ArrayList<String> chatMemberIds, chatMemberNames;
    private GroupChat groupChat;
    private boolean isGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        setUI();
    }

    private void setUI() {
        Intent getIntent = getIntent();

        chatTitle = findViewById(R.id.chatTitle);
        returnBackBtn = findViewById(R.id.returnBack);
        accessBtn = findViewById(R.id.linkAccess);
        inputMessage = findViewById(R.id.chatWriteMessage);
        sendMessageBtn = findViewById(R.id.messageSubmit);
        messageBody = findViewById(R.id.chatMessages);

        if (getIntent != null) {

            if (Objects.requireNonNull(getIntent.getExtras()).getString("chatId") != null) {
                chatId = Objects.requireNonNull(getIntent.getExtras()).getString("chatId");
                isGroup = Objects.requireNonNull(getIntent.getExtras()).getBoolean("isGroup");

                if (isGroup) {
                    forumId = getIntent.getExtras().getString("forumId");
                    forumData = db.collection("FORUMS").document(forumId);
                    chatTitle.setText(forumId);
                }
                else {
                    otherUserId = getIntent.getExtras().getString("otherUserId");
                    otherUserData = db.collection("users").document(otherUserId);
                    chatTitle.setText(otherUserId);
                }

                chatData = db.collection("CHATROOMS").document(chatId);

            }

            if (getIntent.getExtras().getString("otherUserId") != null) {
                otherUserId = getIntent.getExtras().getString("otherUserId");
                otherUserData = db.collection("users").document(otherUserId);

                chatTitle.setText(otherUserId + " new chat");
            }

            currUserData = db.collection("users").document(userId);
        }

        returnToPreviousPage();
        getOrCreateChatRoom();
    }

    private void getOrCreateChatRoom() {

        chatData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    groupChat = task.getResult().toObject(GroupChat.class);

                    chatTitleStr = new StringBuilder();
                    chatMemberIds = new ArrayList<>();

                    if (isGroup) {
                        forumData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            ArrayList<String> memberIds, moderatorIds;
                            String chiefAdminId, forumTitle;
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    forumTitle = documentSnapshot.getString("title");
                                    chatTitleStr.append(forumTitle).append("'s Group Chat");

                                    memberIds = (ArrayList<String>) documentSnapshot.get("memberIds");
                                    moderatorIds = (ArrayList<String>) documentSnapshot.get("moderatorIds");
                                    chiefAdminId = documentSnapshot.getString("chiefAdmin");

                                    chatMemberIds.add(chiefAdminId);

                                    if(memberIds != null) {
                                        chatMemberIds.addAll(memberIds);
                                    }

                                    if (moderatorIds != null) {
                                        chatMemberIds.addAll(moderatorIds);
                                    }
                                }
                            }
                        });
                    } else {

                    }

                    if (groupChat == null) {
                        //New chat
                        Calendar calendar = Calendar.getInstance();
                        String format = "dd/MM/yyyy HH:mm";
                        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());

                        Date timestamp = new Date();
                        try {
                            timestamp = sdf.parse(sdf.format(calendar.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String chatTitle = String.valueOf(chatTitleStr);

                        groupChat = new GroupChat(chatMemberIds, chatTitle, isGroup, timestamp, "", "");
                        chatData.set(groupChat, SetOptions.merge());
                    } else {

                    }
                    DocumentSnapshot document = task.getResult();

                    if (document.exists()) {
                        chatTitle.setText(document.getString("title"));
                    }
                }
            }
        });
    }

    private void addMessage() {

    }

    private void returnToPreviousPage() {
        returnBackBtn.setOnClickListener(v -> finish());
    }
}