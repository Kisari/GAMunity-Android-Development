package rmitcom.asm1.gamunity.adapter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;

public class ProfileForumListAdapter extends BaseAdapter {
    private final ArrayList<Forum> forumList;
    private final FireBaseManager db = new FireBaseManager();
    private final Constant constant = new Constant();
    private View currentView;
    private String tag = "";
    private int position = -1;

    public ProfileForumListAdapter(ArrayList<Forum> forumList) {
        this.forumList = forumList;
    }

    @Override
    public int getCount() {
        return forumList.size();
    }


    @Override
    public Object getItem(int position) {
        return forumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(forumList.get(position).getForumId());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewForumList;

        if (convertView == null) {
            viewForumList = View.inflate(parent.getContext(), R.layout.ui_profile_forum, null);
        } else viewForumList = convertView;

        Forum forumItem = (Forum) getItem(position);

        viewForumList.setOnClickListener(v -> {
            Intent toForumDetailView = new Intent(parent.getContext(), ForumView.class);
            toForumDetailView.putExtra("forumId", forumItem.getForumRef());
            toForumDetailView.putExtra("mode", constant.VIEW);
            ((Activity) v.getContext()).startActivityForResult(toForumDetailView, constant.DELETE);
            ((Activity) parent.getContext()).finish();
        });

        ImageView forumBackground = viewForumList.findViewById(R.id.profileForumBackgroundImg);
        ShapeableImageView forumIcon = viewForumList.findViewById(R.id.profileForumImg);
        ProgressBar forumIconProgress = viewForumList.findViewById(R.id.profileForumProgressBar);
        ProgressBar profileForumBackgroundProgressBar = viewForumList.findViewById(R.id.profileForumBackgroundProgressBar);
        TextView forumTitle = viewForumList.findViewById(R.id.profileForumName);

        //fetch two image with cdn
        try{
            new AsyncImage(forumIcon, forumIconProgress).loadImage(forumItem.getForumIcon());
            new AsyncImage(forumBackground, profileForumBackgroundProgressBar).loadImage(forumItem.getForumBackground());
        }
        catch (Exception e){
            Log.e(TAG, "getView: ", e);
            e.printStackTrace();
        }

        //set data for forum view
        forumTitle.setText(forumItem.getTitle());

        FrameLayout profileForumModeratorChip = viewForumList.findViewById(R.id.profileForumModeratorChip);
        FrameLayout profileForumMemberChip = viewForumList.findViewById(R.id.profileForumMemberChip);
        FrameLayout profileForumAdminChip =  viewForumList.findViewById(R.id.profileForumAdminChip);

        if(forumItem.getChiefAdmin().equals(db.getCurrentUser().getUid())){
            profileForumAdminChip.setVisibility(View.VISIBLE);

        }
        else if(forumItem.getModeratorIds().contains(db.getCurrentUser().getUid())){
            profileForumModeratorChip.setVisibility(View.VISIBLE);
        }
        else{
            profileForumMemberChip.setVisibility(View.VISIBLE);
        }

        this.currentView = viewForumList;

        return viewForumList;
    }
}
