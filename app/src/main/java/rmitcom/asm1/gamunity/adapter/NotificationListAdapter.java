package rmitcom.asm1.gamunity.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Notification;

public class NotificationListAdapter extends BaseAdapter {
    private final ArrayList<Notification> notificationList;
    private View currentView;

    public NotificationListAdapter(ArrayList<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getId(int position){
        return notificationList.get(position).getNotificationId();
    }

    public void updateNotificationAfterClickEvent(int position){
        this.notificationList.get(position).setNotificationIsRead(true);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewNotificationList;

        if (convertView == null) {
            viewNotificationList = View.inflate(parent.getContext(), R.layout.ui_notification_list_view_item, null);
        } else viewNotificationList = convertView;

        Notification notificationItem = (Notification) getItem(position);

        ProgressBar progressBar = viewNotificationList.findViewById(R.id.progressBar);
        ShapeableImageView notificationSenderIcon = viewNotificationList.findViewById(R.id.notificationSenderIcon);
        TextView notificationTitle = viewNotificationList.findViewById(R.id.notificationTitle);
        TextView notificationBody = viewNotificationList.findViewById(R.id.notificationBody);
        LinearLayout notificationIsReadView = viewNotificationList.findViewById(R.id.notificationIsReadView);

        try{
            new AsyncImage(notificationSenderIcon, progressBar).loadImage(notificationItem.getNotificationSenderUrl());
        }
        catch (Exception e){
            Log.e(TAG, "getView: ", e);
            e.printStackTrace();
        }

        notificationTitle.setText(notificationItem.getNotificationTitle());
        notificationBody.setText(notificationItem.getNotificationBody());
        if(notificationItem.getNotificationIsRead()){
            notificationIsReadView.setVisibility(View.GONE);
        }

        currentView = viewNotificationList;

        return viewNotificationList;
    }
}
