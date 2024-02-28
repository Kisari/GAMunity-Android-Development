package rmitcom.asm1.gamunity.adapter;

import android.content.Context;
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

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Message;
public class ChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ChatMessageRecyclerViewHolder> {
    private final FireBaseManager manager = new FireBaseManager();
    private final Context context;
    private final ArrayList<Message> messagesList;

    public ChatMessageRecyclerViewAdapter(Context context, ArrayList<Message> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ChatMessageRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_chat_message_row, parent,false);
        return new ChatMessageRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageRecyclerViewHolder holder, int position) {
        Message currMess = messagesList.get(position);

        if (currMess != null) {
            String messContent = currMess.getMessageContent();
            String messSender = currMess.getMessageOwnerId();
            Timestamp messTimestamp = currMess.getTimestamp();
            boolean isImage = currMess.isImage();

            if (messSender.equals(manager.getCurrentUser().getUid())) {
                Log.i("Message body", "isCurrUser: " + messContent);
                holder.userLayout.setVisibility(View.VISIBLE);
                holder.otherLayout.setVisibility(View.GONE);

                if (isImage) {
                    try {
                        holder.userTextLayout.setVisibility(View.GONE);
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
                    holder.userTextLayout.setVisibility(View.VISIBLE);
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
                        holder.otherTextLayout.setVisibility(View.GONE);
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
                    holder.otherTextLayout.setVisibility(View.VISIBLE);
                    holder.otherPictureLayout.setVisibility(View.GONE);
                    holder.otherChat.setVisibility(View.VISIBLE);
                    holder.otherChat.setText(messContent);
                }

                manager.getDb().collection("users").document(messSender).get().addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String otherName = document.getString("name");
                        String otherImg = document.getString("profileImgUri");

                        if (otherName != null) {
                            holder.otherName.setText(otherName);
                        }

                        if (otherImg != null) {
                            try {
                                new AsyncImage(holder.otherImage, holder.otherProgressBar).loadImage(otherImg);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
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

    public static class ChatMessageRecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout userLayout, otherLayout, userTextLayout, otherTextLayout;
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

            userTextLayout = itemView.findViewById(R.id.userTextLayout);
            otherTextLayout = itemView.findViewById(R.id.otherTextLayout);

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
