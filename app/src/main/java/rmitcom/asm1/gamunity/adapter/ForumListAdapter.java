package rmitcom.asm1.gamunity.adapter;

import static android.content.ContentValues.TAG;

import static androidx.core.app.ActivityCompat.startActivityForResult;

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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;
import rmitcom.asm1.gamunity.model.Notification;

public class ForumListAdapter extends BaseAdapter implements Filterable {

    private final ArrayList<Forum> forumList;
    private final FireBaseManager db = new FireBaseManager();
    private ArrayList<Forum> currentForumList;
    private final Constant constant = new Constant();
    private View currentView;
    private String tag = "";
    private int position = -1;

    public ForumListAdapter(ArrayList<Forum> forumList) {
        this.forumList = forumList;
        this.currentForumList = forumList;
    }

    @Override
    public int getCount() {
        return currentForumList.size();
    }


    @Override
    public Object getItem(int position) {
        return currentForumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(currentForumList.get(position).getForumId());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewForumList;

        if (convertView == null) {
            viewForumList = View.inflate(parent.getContext(), R.layout.ui_forum_list_view_item, null);
        } else viewForumList = convertView;

        Forum forumItem = (Forum) getItem(position);

//        viewForumList.setOnClickListener(v -> {
//            Intent toForumDetailView = new Intent(parent.getContext(), ForumView.class);
//            toForumDetailView.putExtra("forumId", forumItem.getForumRef());
//            ((Activity) v.getContext()).startActivityForResult(toForumDetailView, constant.DELETE);
//        });

        ImageView forumBackground = viewForumList.findViewById(R.id.forumBackground);
        ShapeableImageView forumIcon = viewForumList.findViewById(R.id.forumIcon);
        ProgressBar forumIconProgress = viewForumList.findViewById(R.id.progress1);
        ProgressBar forumBackgroundProgress = viewForumList.findViewById(R.id.progress2);
        TextView forumTitle = viewForumList.findViewById(R.id.forumTitle);
        Button forumActionJoin = viewForumList.findViewById(R.id.forumActionBtnJoin);
        Button forumActionBtnJoined = viewForumList.findViewById(R.id.forumActionBtnJoined);

        //fetch two image with cdn
        try{
            new AsyncImage(forumIcon, forumIconProgress).loadImage(forumItem.getForumIcon());
            new AsyncImage(forumBackground, forumBackgroundProgress).loadImage(forumItem.getForumBackground());
        }
        catch (Exception e){
            Log.e(TAG, "getView: ", e);
            e.printStackTrace();
        }

        //set data for forum view
        forumTitle.setText(forumItem.getTitle());
        Button forumActionBtnOwned = viewForumList.findViewById(R.id.forumActionBtnOwned);
        //set Join/unJoin button function
        if(forumItem.getChiefAdmin().equals(db.getCurrentUser().getUid())){
            forumActionBtnOwned.setVisibility(View.VISIBLE);
        }
        //current user is not the admin of forum
        else{
            if(forumItem.getMemberIds().contains(db.getCurrentUser().getUid())){
                forumActionBtnJoined.setVisibility(View.VISIBLE);
                forumActionJoin.setVisibility(View.GONE);
                forumActionBtnOwned.setVisibility(View.GONE);
            }
            else{
                forumActionBtnJoined.setVisibility(View.GONE);
                forumActionBtnOwned.setVisibility(View.GONE);
                forumActionJoin.setVisibility(View.VISIBLE);
            }
            forumActionBtnJoined.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                //get the custom layout
                LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View unJoinDialogLayout = inflater.inflate(R.layout.ui_forum_unjoin_dialog_view, null);
                ImageView dialogIcon = unJoinDialogLayout.findViewById(R.id.dialogIcon);
                ProgressBar dialogIconProgress = unJoinDialogLayout.findViewById(R.id.dialogIconProgress);
                TextView dialogMessage = unJoinDialogLayout.findViewById(R.id.dialogMessage);
                TextView dialogCancel = unJoinDialogLayout.findViewById(R.id.dialogCancel);
                TextView dialogAccept = unJoinDialogLayout.findViewById(R.id.dialogAccept);
                builder.setView(unJoinDialogLayout);
                AlertDialog dialog = builder.create();
                dialog.show();
                try{
                    new AsyncImage(dialogIcon, dialogIconProgress).loadImage(forumItem.getForumIcon());
                    dialogMessage.setText("You will no longer be a member of this forum.Do you want to unjoin?");
                    dialogCancel.setOnClickListener(v1 -> dialog.dismiss());
                    dialogAccept.setOnClickListener(v12 -> unJoinForum(forumItem, dialog));
                }
                catch (Exception e){
                    Log.e(TAG, "getView: ", e);
                    e.printStackTrace();
                }

            });
            forumActionJoin.setOnClickListener(v -> joinForum(forumItem));
        }

        this.currentView = viewForumList;

        return viewForumList;
    }

    public void setTag(String tag, int position){
        this.tag = tag;
        this.position = position;
    }

    public String getTagName(){
        return this.tag;
    }

    public int getTagPosition(){
        return this.position;
    }

    public void removeTag(){
        this.tag = "";
        this.position = -1;
    }

    public void joinForum(Forum forum){
        String[] newMemberIdsList = new String[forum.getMemberIds().size()+1];
        for (int i = 0; i < forum.getMemberIds().size(); i++) {
            newMemberIdsList[i] = forum.getMemberIds().get(i);
        }
        newMemberIdsList[forum.getMemberIds().size()] = db.getCurrentUser().getUid();
        CollectionReference ref = db.getDb().collection(constant.forums);
        ref.whereEqualTo("forumId", forum.getForumId())
            .get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot returnDocument : task.getResult()) {
                        Map<String, Object> newMemberIds = new HashMap<>();
                        newMemberIds.put("memberIds", Arrays.asList(newMemberIdsList));
                        newMemberIds.put("noJoined", newMemberIdsList.length);
                        ref.document(returnDocument.getId()).set(newMemberIds, SetOptions.merge()).addOnCompleteListener(joinTask -> {
                            if(joinTask.isSuccessful()){
                                updateTheForumMemberList(forum.getForumId(), newMemberIdsList);
                                String avatarUrl = "https://firebasestorage.googleapis.com/v0/b/gamunity-1c175.appspot.com/o/forumIcon1.png?alt=media&token=1ebe3d13-3264-4646-9d63-ecbef8f5c8e4";
                                db.getDb().collection("users")
                                    .whereEqualTo("userId", db.getCurrentUser().getUid())
                                    .get()
                                    .addOnCompleteListener(checkingUser -> {
                                        if(checkingUser.isSuccessful()){
                                            for (QueryDocumentSnapshot document: checkingUser.getResult()){
                                                String userName = document.getString("name");
                                                String notificationBody = userName + " become a new member of " + forum.getTitle();
                                                Notification newNotification = new Notification("Join the forum", avatarUrl, notificationBody, db.getCurrentUser().getUid(), forum.getChiefAdmin(), false, Calendar.getInstance().getTime().toString(), forum.getForumId());
                                                db.sendNotificationToDevice(newNotification, userName, constant.JOIN_FORUM);
                                                Toast.makeText(currentView.getContext(), "You join " + forum.getTitle(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            }
                        });
                    }
                }
            });
    }

    public void unJoinForum(Forum forum, AlertDialog dialog){
        String[] newMemberIdsList = removeItemFromArray(forum.getMemberIds().toArray(new String[0]), db.getCurrentUser().getUid());

        CollectionReference ref = db.getDb().collection(constant.forums);
        ref.whereEqualTo("forumId", forum.getForumId())
            .get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot returnDocument : task.getResult()) {
                        Map<String, Object> newMemberIds = new HashMap<>();
                        newMemberIds.put("memberIds", Arrays.asList(newMemberIdsList));
                        newMemberIds.put("noJoined", newMemberIdsList.length);
                        ref.document(returnDocument.getId()).set(newMemberIds, SetOptions.merge()).addOnCompleteListener(joinTask -> {
                            if(joinTask.isSuccessful()){
                                updateTheForumMemberList(forum.getForumId(), newMemberIdsList);
                                dialog.dismiss();
                                Toast.makeText(currentView.getContext(), "You leave " + forum.getTitle(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
    }

    public static String[] removeItemFromArray(String[] input, String item) {
        if (input == null) {
            return null;
        } else if (input.length == 0) {
            return input;
        } else {
            String[] output = new String[input.length - 1];
            int count = 0;
            for (String i : input) {
                if (!i.equals(item)) {
                    output[count++] = i;
                }
            }
            return output;
        }
    }

    public void updateTheForumMemberList(String forumId, String[] memberIdsList){
        for (int i = 0; i < this.forumList.size(); i++) {
            if(forumList.get(i).getForumId().equals(forumId)){
                forumList.get(i).setMemberIds(new ArrayList<>(Arrays.asList(memberIdsList)));
                forumList.get(i).setNoJoined(memberIdsList.length);
                break;
            }
        }
        notifyDataSetChanged();
    }
    @Override
    public Filter getFilter(){

        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results.count == 0){
                    currentForumList = (ArrayList<Forum>) results.values;
                    notifyDataSetInvalidated();
                }
                else{
                    currentForumList = (ArrayList<Forum>) results.values;
                    notifyDataSetChanged();
                }

            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<Forum> FilteredArrayNames = new ArrayList<>();

                constraint = constraint.toString().toLowerCase();

                for (int i = 0; i < forumList.size(); i++) {
                    String dataNames = forumList.get(i).getTitle();
                    if (dataNames.toLowerCase().startsWith(constraint.toString()))  {
                        FilteredArrayNames.add(forumList.get(i));
                    }
                }

                if(!Objects.equals(tag, "")){
                    for (int i = 0; i < FilteredArrayNames.size(); i++) {
                        ArrayList<String> forumCatsList = FilteredArrayNames.get(i).getCategory();
                        if (!forumCatsList.contains(tag))  {
                            FilteredArrayNames.set(i, null);
                        }
                    }

                    FilteredArrayNames.removeAll(Collections.singletonList(null));
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }

        };
    }
}
