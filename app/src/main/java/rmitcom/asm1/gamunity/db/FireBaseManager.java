package rmitcom.asm1.gamunity.db;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FireBaseManager {
    private FirebaseFirestore db;
    private StorageReference storageRef;


    public FireBaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public  StorageReference getStorageRef() {
        return storageRef;
    }
}
