package rmitcom.asm1.gamunity.db;

import static android.content.ContentValues.TAG;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FireBaseManager extends FirebaseMessagingService {
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    private final FirebaseUser currentUser;
    private final FirebaseMessaging msgProvider;

    public FireBaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.msgProvider = FirebaseMessaging.getInstance();
        msgProvider.setAutoInitEnabled(true);
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public  StorageReference getStorageRef() {
        return storageRef;
    }

    public FirebaseUser getCurrentUser() {return currentUser;}

    public FirebaseMessaging getMsgProvider(){return msgProvider;}

    @Override
    public void onNewToken(@NonNull String token) {
//        Log.d(TAG, "onNewToken: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

}
