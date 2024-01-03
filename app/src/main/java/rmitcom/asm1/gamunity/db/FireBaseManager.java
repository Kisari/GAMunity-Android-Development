package rmitcom.asm1.gamunity.db;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FireBaseManager {
    private final FirebaseFirestore db;
    private final StorageReference storageRef;

    private final FirebaseUser currentUser;

    public FireBaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public  StorageReference getStorageRef() {
        return storageRef;
    }

    public FirebaseUser getCurrentUser() {return currentUser;}
}
