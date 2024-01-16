package rmitcom.asm1.gamunity.components.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ChatRoomRecyclerViewAdapter;
import rmitcom.asm1.gamunity.adapter.PostRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.views.chat.ChatSearchUser;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.GroupChat;
import rmitcom.asm1.gamunity.model.Post;

public class ChatFragment extends Fragment {
//    private final FireBaseManager dbManager = new FireBaseManager();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData, chatRoomData;
    private SearchView chatSearchBar;
    private RecyclerView chatList;
    private ImageButton chatSearchUser;
    private ArrayList<String> chatGroupIds;
    private  ArrayList<GroupChat> chatGroupList;
    private ChatRoomRecyclerViewAdapter adapter;
    private final Constant constant = new Constant();
    View currentView;
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        currentView = view;
        // Inflate the layout for this fragment

        setUI();

        return view;
    }

    private void setUI() {
        chatSearchBar = currentView.findViewById(R.id.chatSearchBar);
        chatList = currentView.findViewById(R.id.chatList);
        chatSearchUser = currentView.findViewById(R.id.chatSearchUser);

        userData = db.collection("users").document(userId);

        setData();
        accessSearchUser();
    }

    private void setData() {
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    chatGroupIds = new ArrayList<>();

                    if (document.exists()) {
                        if (document.get("chatGroupIds") != null) {
                            chatGroupIds = (ArrayList<String>) document.get("chatGroupIds");

                            if (chatGroupIds != null) {
                                chatGroupList = new ArrayList<>();

                                for (String chatId: chatGroupIds) {
                                    getChatRoomData(chatId);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void getChatRoomData(String chatId) {
        if (chatId != null) {
            db.collection("CHATROOMS").document(chatId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        String chatTitle = "", chatImage = "", forumId = "";

                        if (document.exists()) {
                            chatTitle = document.getString("chatTitle");
                            chatImage = document.getString("chatImg");
                            boolean isGroup = Boolean.TRUE.equals(document.getBoolean("isGroup"));
                            forumId = document.getString("forumId");

                            Log.i("Chat", "chatName - exist: " + chatTitle);


                            GroupChat groupChat = new GroupChat(chatId, chatTitle, chatImage, isGroup, forumId);
                            chatGroupList.add(groupChat);
                        }

                        if (chatGroupList.size() == chatGroupIds.size()) {
                            setupList(chatGroupList);
                        }

                    }
                }
            });
        }
    }

    private void accessSearchUser() {
        chatSearchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getContext(), ChatSearchUser.class);
                startActivity(searchIntent);
            }
        });
    }

    private void setupList(ArrayList<GroupChat> chatGroupList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatList.setLayoutManager(layoutManager);

        adapter = new ChatRoomRecyclerViewAdapter(getContext(), chatGroupList);
        chatList.setAdapter(adapter);
    }
}