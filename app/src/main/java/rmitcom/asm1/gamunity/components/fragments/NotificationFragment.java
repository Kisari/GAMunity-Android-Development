package rmitcom.asm1.gamunity.components.fragments;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.db.FireBaseManager;

public class NotificationFragment extends Fragment {
    private final FireBaseManager db = new FireBaseManager();
    View currentView;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(currentView.getContext(), "Notifications will be push up in the future", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(currentView.getContext(), "Your app will not receive push notifications", Toast.LENGTH_SHORT).show();
                }
            });

    public NotificationFragment() {
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

        //ask to receive notification
        askNotificationPermission();

        initializeNotification();

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
                        .setPositiveButton("Ok", (dialog, which) -> {
                            Toast.makeText(currentView.getContext(), "Notification disable", Toast.LENGTH_SHORT).show();
                        })
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
        db.getMsgProvider().getToken()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String msg = task.getResult();
//                        String msg = token;
                        Log.d(TAG, msg);
                        Toast.makeText(currentView.getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    }
                });
    }
}