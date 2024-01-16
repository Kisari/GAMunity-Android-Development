package rmitcom.asm1.gamunity.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.model.Message;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ChatMessageRecyclerViewAdapter extends FirestoreRecyclerAdapter<Message, ChatMessageRecyclerViewAdapter.ChatMessageRecyclerViewHolder> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth userAuth = FirebaseAuth.getInstance();
    private final String userId = userAuth.getUid();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final Context context;

    public ChatMessageRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ChatMessageRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ui_chat_message_row,parent,false);
        return new ChatMessageRecyclerViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull ChatMessageRecyclerViewHolder holder, int position) {
//
//    }

    @Override
    protected void onBindViewHolder(@NonNull ChatMessageRecyclerViewHolder holder, int position, @NonNull Message model) {
        if (model.getMessageOwnerId().equals(userId)) {
            Log.i("Message body", "isCurrUser: " + userId);
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.otherLayout.setVisibility(View.GONE);

            holder.userChat.setText(model.getMessageContent());
        }
        else {
            Log.i("Message body", "isOtherUser: " + model.getMessageOwnerId());
            holder.userLayout.setVisibility(View.GONE);
            holder.otherLayout.setVisibility(View.VISIBLE);

            holder.otherChat.setText(model.getMessageContent());
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ChatMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userLayout, otherLayout;
        ImageView userBaseImg, otherBaseImg;
        ProgressBar userProgressBar, otherProgressBar;
        ShapeableImageView userImage, otherImage;
        TextView userChat, otherChat;
        public ChatMessageRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            userLayout = itemView.findViewById(R.id.userLayout);
            otherLayout = itemView.findViewById(R.id.otherLayout);

            userBaseImg = itemView.findViewById(R.id.userBaseImg);
            otherBaseImg = itemView.findViewById(R.id.otherBaseImg);

            userProgressBar = itemView.findViewById(R.id.userProgressBar);
            otherProgressBar = itemView.findViewById(R.id.otherProgressBar);
            userImage = itemView.findViewById(R.id.userImage);
            otherImage = itemView.findViewById(R.id.otherImage);

            userChat = itemView.findViewById(R.id.userChat);
            otherChat = itemView.findViewById(R.id.otherChat);

        }
    }
}
