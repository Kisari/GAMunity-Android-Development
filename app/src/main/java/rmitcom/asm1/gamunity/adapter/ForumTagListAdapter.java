package rmitcom.asm1.gamunity.adapter;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;

public class ForumTagListAdapter extends RecyclerView.Adapter<ForumTagListAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final ArrayList<String> tagList;
    private ItemClickListener mClickListener;
    private ItemLongClickListener mLongClickListener;
    public ForumTagListAdapter(Context context, ArrayList<String> tagList) {
        this.mInflater = LayoutInflater.from(context);
        this.tagList = tagList;
    }

    @NonNull
    @Override
    public ForumTagListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.ui_forum_tag_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumTagListAdapter.ViewHolder holder, int position) {
        String tagName = tagList.get(position);
        holder.tagName.setText(tagName);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView tagName;

        ViewHolder(View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.forumTagItem);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            if (mLongClickListener != null) mLongClickListener.onItemLongClick(view, getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

    }

    public String getItem(int id) {
        return tagList.get(id);
    }

    public void removeAt(int position) {
        tagList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tagList.size());
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface ItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
