package rmitcom.asm1.gamunity.db;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import rmitcom.asm1.gamunity.MainActivity;
import rmitcom.asm1.gamunity.components.views.LoginView;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Notification;

public class FireBaseManager extends FirebaseMessagingService {
    private final FirebaseFirestore db;
    private final StorageReference storageRef;
    private final FirebaseUser currentUser;
    private final FirebaseMessaging msgProvider;
    private final Constant constant = new Constant();

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
        Log.d(TAG, "onNewToken: " + token);
    }

    public void changeUserIdWithDeviceToken(String userID, Context context){
        msgProvider.getToken()
            .addOnCompleteListener(getTokenTask -> {
                if(getTokenTask.isSuccessful()){
                    String token = getTokenTask.getResult();
                    updateTheDeviceToken(userID, token, context);
                }
            });
    }

    public void updateTheDeviceToken(String userID, String token, Context context){
        Log.d(TAG, "This token device: " + token);
        CollectionReference ref = db.collection(constant.deviceTokens);
        ref.whereEqualTo("token", token)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().size() == 0){
                            Log.d(TAG, "Adding new token progress:");
                            Map<String, Object> newNotificationToken = new HashMap<>();
                            newNotificationToken.put("userId", userID);
                            newNotificationToken.put("token", token);
                            ref.add(newNotificationToken)
                                    .addOnCompleteListener(task1 -> {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "New token device " + token + " to user with id " + userID);
                                        }
                                    });
                            return;
                        }
                        for (QueryDocumentSnapshot document1: task.getResult()){
                            Map<String, Object> newNotificationToken = new HashMap<>();
                            newNotificationToken.put("userId", userID);
                            newNotificationToken.put("token", token);
                            ref.document(document1.getId())
                                .set(newNotificationToken, SetOptions.merge())
                                .addOnCompleteListener(updateToken -> {
                                    if(updateToken.isSuccessful()){
                                        Log.d(TAG, "Change token device " + msgProvider.getToken() + " to user with id " + currentUser.getUid());
                                    }
                                    else {
                                        Log.d(TAG, "updateTheDeviceToken: " + "Failed");
                                    }
                                });
                        }
                    }
                    else{
                        Log.d(TAG, "Adding new token progress:");
                        Map<String, Object> newNotificationToken = new HashMap<>();
                        newNotificationToken.put("userId", userID);
                        newNotificationToken.put("token", token);
                        ref.add(newNotificationToken)
                                .addOnCompleteListener(task1 -> {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "New token device " + msgProvider.getToken() + " to user with id " + currentUser.getUid());
                                    }
                                });
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Title: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    public void sendNotificationToDevice(Notification newNo){
        db.collection(constant.deviceTokens)
                .whereEqualTo("userId", newNo.getNotificationReceiverId())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            String token = document.getString("token");
                            newNo.setReceiverToken(token);
                            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                            OkHttpClient client = new OkHttpClient();
                            JSONObject newNotificationObject = new JSONObject();
                            JSONObject dataObject = new JSONObject();
                            try{
                                newNotificationObject.put("title", "Join the forum");
                                newNotificationObject.put("body", "Someone joins your forum");

                                assert token != null;
                                dataObject.put("to", token.trim());
                                dataObject.put("notification", newNotificationObject);
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }

                            RequestBody requestBody = RequestBody.create(mediaType, String.valueOf(dataObject));
                            Request request = new Request.Builder()
                                    .url(constant.notification_url)
                                    .post(requestBody)
                                    .header("Authorization", constant.server_key.trim())
                                    .addHeader("Content-Type", "application/json")
                                    .build();

                            Log.d(TAG, "sendNotificationToDevice: " + request.isHttps());
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                                    if(!response.isSuccessful()){
                                        throw new IOException("Unexpected code " + response.code());
                                    }
                                    else{
                                        createNewNotificationInFireBase(newNo);
                                    }
                                }
                            });

                        }
                    }
                });
    }

    public void createNewNotificationInFireBase(Notification newNotificationData){
        DocumentReference ref = db.collection(constant.notifications).document();

        String newNotificationId = ref.getId();

        Map<String, Object> newNotification = new HashMap<>();
        newNotification.put("notificationId", newNotificationId);
        newNotification.put("receiverToken", newNotificationData.getReceiverToken());
        newNotification.put("notificationTitle", newNotificationData.getNotificationTitle());
        newNotification.put("notificationSenderUrl", newNotificationData.getNotificationSenderUrl());
        newNotification.put("notificationBody", newNotificationData.getNotificationBody());
        newNotification.put("notificationSenderId", newNotificationData.getNotificationSenderId());
        newNotification.put("notificationReceiverId", newNotificationData.getNotificationReceiverId());
        newNotification.put("notificationIsRead", false);
        newNotification.put("sendTime", newNotificationData.getSendTime());
        newNotification.put("notificationForumId", newNotificationData.getNotificationId());

        ref.set(newNotification).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "createNewNotificationInFireBase: "+ "Send notification to " + newNotificationData.getNotificationReceiverId());
            }
        });
    }

}
