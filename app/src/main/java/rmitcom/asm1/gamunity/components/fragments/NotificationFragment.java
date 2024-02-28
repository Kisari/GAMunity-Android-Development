package rmitcom.asm1.gamunity.components.fragments;

import static android.content.ContentValues.TAG;
import static androidx.core.app.ActivityCompat.recreate;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.NotificationListAdapter;
import rmitcom.asm1.gamunity.components.views.HomeView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Notification;

public class NotificationFragment extends Fragment implements FirebaseFetchAndSetUI {
    private NotificationListAdapter adapter;
    private final ArrayList<Notification> notificationArrayList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private final FireBaseManager manager = new FireBaseManager();
    private final Constant constant = new Constant();
    View currentView;
    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public NotificationFragment() {
        this.requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(currentView.getContext(), "Notifications will be push up in the future", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(currentView.getContext(), "Your app will not receive push notifications", Toast.LENGTH_SHORT).show();
            }
        });
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        currentView = view;

        Log.d(TAG, "onCreateView: " + "this is notification fragment");

        //ask to receive notification
        askNotificationPermission();

        initializeNotification();

        fetchData();

        return view;
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(currentView.getContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(currentView.getContext())
                        .setTitle("Are you sure ?")
                        .setMessage("You will not receive any push notifications in the future.")
                        .setPositiveButton("Ok", (dialog, which) -> Toast.makeText(currentView.getContext(), "Notification disable", Toast.LENGTH_SHORT).show())
                        .setNegativeButton("Enable Notification", (dialog, which) -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                            Toast.makeText(currentView.getContext(), "Notification enable", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initializeNotification(){
        swipeRefreshLayout = currentView.findViewById(R.id.refreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> recreate(requireActivity()));

        manager.getMsgProvider().getToken()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String msg = task.getResult();
                    Log.d(TAG, msg);
                }
                else{
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                }
            });
    }

    @Override
    public void fetchData() {
        manager.getDb().collection(constant.notifications)
                .whereEqualTo("notificationReceiverId", manager.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            String notificationId = document.getString("notificationId");
                            String receiverToken = document.getString("receiverToken");
                            String notificationTitle = document.getString("notificationTitle");
                            String notificationSenderUrl = document.getString("notificationSenderUrl");
                            String notificationBody = document.getString("notificationBody");
                            String notificationSenderId = document.getString("notificationSenderId");
                            String notificationReceiverId = document.getString("notificationReceiverId");
                            Boolean notificationIsRead = document.getBoolean("notificationIsRead");
                            String sendTime = document.getString("sendTime");
                            String notificationForumId = document.getString("notificationForumId");

                            Notification notificationObject = new Notification(notificationId, receiverToken, notificationTitle, notificationSenderUrl, notificationBody, notificationSenderId, notificationReceiverId, notificationIsRead, sendTime, notificationForumId);

                            notificationArrayList.add(notificationObject);
                        }
                        setUI();
                    }
                    else{
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    @Override
    public void setUI() {
        initializeNotificationListView();
    }

    private void initializeNotificationListView(){
        ListView notificationListView = currentView.findViewById(R.id.notificationListView);

        this.adapter = new NotificationListAdapter(this.notificationArrayList);

        notificationListView.setAdapter(adapter);

        notificationListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Log.d(TAG, "Navigate to notification: " + id);
            updateTheReadNotification(position);
        });
        swipeRefreshLayout.setRefreshing(false);
    }


    private void updateTheReadNotification(int position){
        Notification notification = (Notification) adapter.getItem(position);
        if(notification.getNotificationIsRead()){
            //direct navigate the forum view
            navigateToHomeFragment(notification);
            return;
        }
        CollectionReference ref = manager.getDb().collection(constant.notifications);
        ref.whereEqualTo("notificationId", notification.getNotificationId())
            .get()
            .addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot returnDocument : task.getResult()) {
                        Map<String, Object> updateNotification = new HashMap<>();
                        updateNotification.put("notificationIsRead", true);
                        ref.document(returnDocument.getId()).set(updateNotification, SetOptions.merge()).addOnCompleteListener(updateTask -> {
                            if(updateTask.isSuccessful()){
                                adapter.updateNotificationAfterClickEvent(position);
                                //navigate to the specific event after update the data
                                navigateToHomeFragment(notification);
                            }
                        });
                    }
                }
            });
    }
    private void navigateToHomeFragment(Notification notification){
        try {
            manager.getDb().collection(constant.forums)
                    .whereEqualTo("forumId", notification.getNotificationForumId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                String documentRef = documentSnapshot.getId();
                                ((HomeView) currentView.getContext()).setFragmentItem(0, documentRef);
                            }
                        }
                    });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}