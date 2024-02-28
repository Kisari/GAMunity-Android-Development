package rmitcom.asm1.gamunity.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.chat.ChatView;
import rmitcom.asm1.gamunity.model.GroupChat;

public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ChatRoomRecyclerViewHolder>{
    private final Context context;
    private ArrayList<GroupChat> chatGroupContent;
    private String chatName, chatImg, chatId, dataId;
    private boolean isGroup;
    @NonNull
    @Override
    public ChatRoomRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ui_chat_list_view_item, parent, false);
        return new ChatRoomRecyclerViewAdapter.ChatRoomRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomRecyclerViewHolder holder, int position) {
        GroupChat currGroupChat = chatGroupContent.get(position);

        if (currGroupChat != null) {
            chatName = currGroupChat.getChatTitle();
            chatImg = currGroupChat.getChatImage();
            chatId = currGroupChat.getChatId();
            isGroup = currGroupChat.isGroup();
            dataId = currGroupChat.getDataId();

            if (chatName != null) {
                holder.chatName.setText(chatName);
            }

            if (chatImg != null && !chatImg.isEmpty()) {
                holder.baseImage.setVisibility(View.INVISIBLE);
                holder.chatProgressBar.setVisibility(View.VISIBLE);
                holder.chatImage.setVisibility(View.VISIBLE);
                try {
                    new AsyncImage(holder.chatImage, holder.chatProgressBar).loadImage(chatImg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                holder.baseImage.setVisibility(View.VISIBLE);
                holder.chatProgressBar.setVisibility(View.INVISIBLE);
                holder.chatImage.setVisibility(View.INVISIBLE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return chatGroupContent.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChatGroupContent(ArrayList<GroupChat> chatGroupContent) {
        this.chatGroupContent = chatGroupContent;
        notifyDataSetChanged();
    }

    public class ChatRoomRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView chatName;
        ProgressBar chatProgressBar;
        ShapeableImageView chatImage;
        ImageView baseImage;
        public ChatRoomRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            chatName = itemView.findViewById(R.id.chatTabTitle);
            chatProgressBar = itemView.findViewById(R.id.chatTabProgressBar1);
            chatImage = itemView.findViewById(R.id.chatTabImg);
            baseImage = itemView.findViewById(R.id.baseImg);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    GroupChat groupChat = chatGroupContent.get(position);
                    navigateToChatView(groupChat);
                }
            });
        }

        private void navigateToChatView(GroupChat groupChat) {
            Intent intent = new Intent(context, ChatView.class);
            intent.putExtra("chatId", groupChat.getChatId());
            intent.putExtra("isGroup", groupChat.isGroup());
            intent.putExtra("dataId", groupChat.getDataId());

            ((Activity) context).startActivityForResult(intent, 189);
        }
    }

    public ChatRoomRecyclerViewAdapter(Context context, ArrayList<GroupChat> chatGroupContent) {
        this.context = context;
        this.chatGroupContent = chatGroupContent;
    }
}
