package rmitcom.asm1.gamunity.components.fragments;

import static android.content.ContentValues.TAG;

import static androidx.core.app.ActivityCompat.recreate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ChatRoomRecyclerViewAdapter;
import rmitcom.asm1.gamunity.adapter.PostRecyclerViewAdapter;
import rmitcom.asm1.gamunity.components.views.chat.ChatSearchUser;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.GroupChat;
import rmitcom.asm1.gamunity.model.Post;
import rmitcom.asm1.gamunity.model.User;

public class ChatFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FireBaseManager manager = new FireBaseManager();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private DocumentReference userData, chatRoomData;
    private SearchView chatSearchBar;
    private RecyclerView chatList;
    private ImageButton chatSearchUser;
    private ArrayList<String> chatGroupIds;
    private  ArrayList<GroupChat> chatGroupList = new ArrayList<>();
    private ChatRoomRecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
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

        String userId = manager.getCurrentUser().getUid();

        userData = db.collection("users").document(userId);

        setData(userData);
        accessSearchUser();

        swipeRefreshLayout = currentView.findViewById(R.id.refreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            recreate(requireActivity());
        });

        if(chatGroupList.size() == 0){
            setupList(chatGroupList);
        }
    }

    private void setData(DocumentReference userData) {
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

                                initChatSearch(chatGroupList);
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

                        if (document.exists()) {
                            String chatTitle = document.getString("chatTitle");
                            String chatImage = document.getString("chatImg");
                            Timestamp chatTimestamp = (Timestamp) document.get("lastTimestamp");
                            boolean isGroup = Boolean.TRUE.equals(document.getBoolean("isGroup"));

                            Log.i("Chat", "chatName - exist: " + chatTitle);
                            String forumId;

                            GroupChat groupChat;
                            if (isGroup) {
                                forumId = document.getString("dataId");
                                groupChat = new GroupChat(chatId, chatTitle, chatImage, true, forumId, chatTimestamp);
                            }
                            else {
                                groupChat = new GroupChat(chatId, chatTitle, chatImage, false, null, chatTimestamp);
                            }

                            Collections.sort(chatGroupList, (groupChat1, groupChat2)
                                    -> groupChat2.compareTo(groupChat1));

                            int index = Collections.binarySearch(chatGroupList, groupChat, (groupChat1, groupChat2)
                                    -> groupChat2.compareTo(groupChat1));

                            int insertionPoint = (index < 0) ? -index : index;

                            if (insertionPoint >= chatGroupList.size()) {
                                chatGroupList.add(groupChat);
                            } else {
                                chatGroupList.add(insertionPoint, groupChat);
                            }

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
                //working in progress
                Intent searchIntent = new Intent(getContext(), ChatSearchUser.class);
                startActivityForResult(searchIntent, constant.CHAT_REQUEST);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == constant.CHAT_REQUEST) {
            if (resultCode == constant.CREATE) {
                GroupChat newAddedGroupChat = (GroupChat) data.getSerializableExtra("newGroupChat");

                this.chatGroupList.add(newAddedGroupChat);

                Log.d(TAG, "onActivityResult: chat" + newAddedGroupChat.getChatTitle());
                Log.d(TAG, "onActivityResult: create chat" + chatGroupList.size());

                this.adapter.notifyDataSetChanged();

            }

            if(resultCode == constant.DELETE){
                GroupChat removeGroupChat = (GroupChat) data.getSerializableExtra("removeGroupChat");
                Log.i(TAG, "removeGroupChat: " + removeGroupChat);

                this.chatGroupList.remove(removeGroupChat);

//                Log.d(TAG, "onActivityResult: chat" + removeGroupChat.getChatTitle());
//                Log.d(TAG, "onActivityResult: delete chat" + chatGroupList.size());

                this.adapter.notifyDataSetChanged();
            }
        }
    }

    private void initChatSearch(ArrayList<GroupChat> chatGroupList) {
        chatSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<GroupChat> chatroom = new ArrayList<>();

                if (chatGroupList != null) {
                    if (newText.isEmpty()) {
                        chatroom.addAll(chatGroupList);
                    }
                    else {
                        for (GroupChat chat : chatGroupList) {
                            if (chat.getChatTitle().toLowerCase().contains(newText.toLowerCase()) && !isUserAlreadyAdded(chat, chatroom)) {
                                chatroom.add(chat);
                            }
                        }
                    }

                    adapter.setChatGroupContent(chatroom);
                    adapter.notifyDataSetChanged();

                }
                return false;
            }
        });
    }

    private boolean isUserAlreadyAdded(GroupChat chat, ArrayList<GroupChat> chatroom) {
        for (GroupChat addedChat : chatroom) {
            if (addedChat.getChatId().equals(chat.getChatId())) {
                return true;
            }
        }
        return false;
    }

    private void setupList(ArrayList<GroupChat> chatGroupList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatList.setLayoutManager(layoutManager);

        adapter = new ChatRoomRecyclerViewAdapter(getContext(), chatGroupList);
        chatList.setAdapter(adapter);

        swipeRefreshLayout.setRefreshing(false);
    }
}