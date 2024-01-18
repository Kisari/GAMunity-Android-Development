package rmitcom.asm1.gamunity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Message;

//import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;

//public class ChatMessageRecyclerViewAdapter extends FirestoreRecyclerAdapter<Message, ChatMessageRecyclerViewAdapter.ChatMessageRecyclerViewHolder> {
//    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
//    private final String userId = userAuth.getUid();
//    private FirebaseStorage storage = FirebaseStorage.getInstance();
//    private final Context context;
//
//    public ChatMessageRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
//        super(options);
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public ChatMessageRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.ui_chat_message_row,parent,false);
//        return new ChatMessageRecyclerViewHolder(view);
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull ChatMessageRecyclerViewHolder holder, int position, @NonNull Message model) {
//        if (model.getMessageOwnerId().equals(userId)) {
//            Log.i("Message body", "isCurrUser: " + userId);
//            holder.userLayout.setVisibility(View.VISIBLE);
//            holder.otherLayout.setVisibility(View.GONE);
//
//            holder.userChat.setText(model.getMessageContent());
//        }
//        else {
//            Log.i("Message body", "isOtherUser: " + model.getMessageOwnerId());
//            holder.userLayout.setVisibility(View.GONE);
//            holder.otherLayout.setVisibility(View.VISIBLE);
//
//            holder.otherChat.setText(model.getMessageContent());
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//    public class ChatMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
//        LinearLayout userLayout, otherLayout;
//        ImageView userBaseImg, otherBaseImg;
//        ProgressBar userProgressBar, otherProgressBar;
//        ShapeableImageView userImage, otherImage;
//        TextView userChat, otherChat;
//        public ChatMessageRecyclerViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            userLayout = itemView.findViewById(R.id.userLayout);
//            otherLayout = itemView.findViewById(R.id.otherLayout);
//
//            userBaseImg = itemView.findViewById(R.id.userBaseImg);
//            otherBaseImg = itemView.findViewById(R.id.otherBaseImg);
//
//            userProgressBar = itemView.findViewById(R.id.userProgressBar);
//            otherProgressBar = itemView.findViewById(R.id.otherProgressBar);
//            userImage = itemView.findViewById(R.id.userImage);
//            otherImage = itemView.findViewById(R.id.otherImage);
//
//            userChat = itemView.findViewById(R.id.userChat);
//            otherChat = itemView.findViewById(R.id.otherChat);
//
//        }
//    }
//}

public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ChatMessageRecyclerViewHolder> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final Context context;
    private ArrayList<Message> messagesList;

    public ChatMessageRecyclerViewAdapter(Context context, ArrayList<Message> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ChatMessageRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_chat_message_row, parent,false);
        return new ChatMessageRecyclerViewAdapter.ChatMessageRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageRecyclerViewHolder holder, int position) {
        Message currMess = messagesList.get(position);

        if (currMess != null) {
            String messContent = currMess.getMessageContent();
            String messSender = currMess.getMessageOwnerId();
            Timestamp messTimestamp = currMess.getTimestamp();
            boolean isImage = currMess.isImage();

            if (messSender.equals(userId)) {
                Log.i("Message body", "isCurrUser: " + messContent);
                holder.userLayout.setVisibility(View.VISIBLE);
                holder.otherLayout.setVisibility(View.GONE);

                if (isImage) {
                    try {
                        holder.userChat.setVisibility(View.GONE);
                        holder.userPictureLayout.setVisibility(View.VISIBLE);
                        new AsyncImage(holder.userPicture, holder.userPictureProgressBar).loadImage(messContent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    holder.userPictureLayout.setVisibility(View.GONE);
                    holder.userChat.setVisibility(View.VISIBLE);
                    holder.userChat.setText(messContent);
                }
            }
            else {
                Log.i("Message body", "isOtherUser: " + messContent);
                holder.userLayout.setVisibility(View.GONE);
                holder.otherLayout.setVisibility(View.VISIBLE);

                if (isImage) {
                    try {
                        holder.otherChat.setVisibility(View.GONE);
                        holder.otherPictureLayout.setVisibility(View.VISIBLE);
                        new AsyncImage(holder.otherPicture, holder.otherPictureProgressBar).loadImage(messContent);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    holder.otherPictureLayout.setVisibility(View.GONE);
                    holder.otherChat.setVisibility(View.VISIBLE);
                    holder.otherChat.setText(messContent);
                }

                db.collection("users").document(messSender).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String otherName = document.getString("name");

                            if (otherName != null) {
                                holder.otherName.setText(otherName);
                            }
                        }
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ChatMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userLayout, otherLayout;
        RelativeLayout userPictureLayout, otherPictureLayout;
        ProgressBar userPictureProgressBar, otherPictureProgressBar;
        ImageView otherBaseImg, userPicture, otherPicture;
        ProgressBar otherProgressBar;
        ShapeableImageView otherImage;
        TextView userChat, otherChat, otherName;

        public ChatMessageRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            userLayout = itemView.findViewById(R.id.userLayout);
            otherLayout = itemView.findViewById(R.id.otherLayout);

            otherBaseImg = itemView.findViewById(R.id.otherBaseImg);

            otherProgressBar = itemView.findViewById(R.id.otherProgressBar);
            otherImage = itemView.findViewById(R.id.otherImage);

            userPicture = itemView.findViewById(R.id.userPicture);
            otherPicture = itemView.findViewById(R.id.otherPicture);

            userPictureLayout = itemView.findViewById(R.id.userPictureLayout);
            otherPictureLayout = itemView.findViewById(R.id.otherPictureLayout);

            userPictureProgressBar = itemView.findViewById(R.id.userPictureProgressBar);
            otherPictureProgressBar = itemView.findViewById(R.id.otherPictureProgressBar);

            userChat = itemView.findViewById(R.id.userChat);
            otherChat = itemView.findViewById(R.id.otherChat);
            otherName = itemView.findViewById(R.id.otherUserName);

        }
    }
}