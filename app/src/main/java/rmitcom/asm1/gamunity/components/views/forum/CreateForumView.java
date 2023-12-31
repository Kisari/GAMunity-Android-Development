package rmitcom.asm1.gamunity.components.views.forum;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import rmitcom.asm1.gamunity.MainActivity;
import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ForumTagListAdapter;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.model.Constant;

public class CreateForumView extends AppCompatActivity implements ForumTagListAdapter.ItemLongClickListener{

    private FireBaseManager db = new FireBaseManager();
    private String nextForumID;
    private EditText forumCategories;
    private EditText forumDescription;
    private EditText forumName;

    private ImageView forumBackground;
    private ImageButton forumIconImageBtn;
    private Uri backgroundFilePath;
    private Uri iconFilePath;
    private ForumTagListAdapter tagListAdapter;

    private Constant constant = new Constant();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_forum_view);

        Intent intent = getIntent();
        nextForumID = intent.getStringExtra("nextForumID");

        ImageView forumBackgroundImageBtn = findViewById(R.id.forumBackgroundImageBtn);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView submitBtn = findViewById(R.id.submitBtn);
        forumBackground = findViewById(R.id.forumBackground);
        forumIconImageBtn = findViewById(R.id.forumIconImageBtn);
        forumCategories = findViewById(R.id.forumCategories);
        forumDescription = findViewById(R.id.forumDescription);
        forumName = findViewById(R.id.forumName);

        initializeForumTagSelectionView();

        backBtn.setOnClickListener(v -> {
            finish();
        });
        forumBackgroundImageBtn.setOnClickListener(v -> {
            chooseImageFromFile(true);
        });

        forumIconImageBtn.setOnClickListener(v -> {
            chooseImageFromFile(false);
        });

        submitBtn.setOnClickListener(v -> {
//            submit();
            uploadBackgroundImage(backgroundFilePath);
        });
    }

    private void chooseImageFromFile(Boolean isBackground){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if(isBackground){
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_IMAGE_BACKGROUND_REQUEST);
        }
        else {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), constant.PICK_IMAGE_ICON_REQUEST);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == constant.PICK_IMAGE_BACKGROUND_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            backgroundFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), backgroundFilePath);
                forumBackground.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        } else if (requestCode == constant.PICK_IMAGE_ICON_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            iconFilePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), iconFilePath);
                forumIconImageBtn.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void initializeForumTagSelectionView(){
        //get the listview
        RecyclerView forumTagLayout = (RecyclerView) findViewById(R.id.forumTagsLayout);

        forumCategories.setFocusable(false);
        forumDescription.setOnDragListener((v, event) -> true);
        forumName.setOnDragListener((v, event) -> true);

        //initialize adapter
        this.tagListAdapter = new ForumTagListAdapter(this, constant.tagList);

        //set on click listener for every tag
        tagListAdapter.setLongClickListener(this);

        //initialize the Horizontal Scroll View for tag list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        forumTagLayout.setLayoutManager(layoutManager);

        //set adapter to tags view list
        forumTagLayout.setAdapter(tagListAdapter);

        forumCategories.setOnDragListener((v, event) -> {
            switch (event.getAction()){
                case DragEvent.ACTION_DRAG_ENTERED:
                    ((EditText)v).setBackgroundColor(Color.parseColor("#99006400"));
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    ((EditText)v).setBackgroundResource(R.drawable.rounded_pill_purple_stroke);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:

                    // Get the item containing the dragged data.
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    // Get the text data from the item.
                    CharSequence dragData = item.getText();

                    // Set the data to edit text
                    EditText pointerView = ((EditText)v);
                    String addNewItemString = "#" + tagListAdapter.getItem(Integer.parseInt(dragData.toString()));
                    pointerView.setText(pointerView.getText().toString() + " " +  addNewItemString);

                    tagListAdapter.removeAt(Integer.parseInt(dragData.toString()));

                    ((EditText)v).setBackgroundResource(R.drawable.rounded_pill_purple_stroke);
                    v.invalidate();

                    // Return true. DragEvent.getResult() returns true.
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    ((EditText)v).setBackgroundColor(Color.parseColor("#ffffff"));
                    v.invalidate();

                    // Do a getResult() and displays what happens.
                    if (event.getResult()) {
                        Toast.makeText(this, "The drop was handled.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "The drop didn't work.", Toast.LENGTH_LONG).show();
                    }

                    // Return true. The value is ignored.
                    return true;
                default:
                    break;
            }
            return false;
        });
    }


    @Override
    public void onItemLongClick(View view, int position){
        view.setTag(String.valueOf(position));
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());
        ClipData dragData = new ClipData(
                (CharSequence) tagListAdapter.getItem(position),
                new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                item);

        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
        view.startDrag(dragData, myShadow, null, 0);
        Log.d(TAG, "onItemLongClick: " + tagListAdapter.getItem(position));
    }

    private void createForum(){
        String forumNameContent = forumName.getText().toString();
        String forumDescriptionContent = forumDescription.getText().toString();
        String[] forumTagList = convertStringTagToArray(forumCategories.getText().toString());

        Map<String, Object> newForum = new HashMap<>();
        newForum.put("forumId", nextForumID);
        newForum.put("chiefAdmin", "minhTest");
        newForum.put("title", forumNameContent);
        newForum.put("category", Arrays.asList(forumTagList));
        newForum.put("description", forumDescriptionContent);
        newForum.put("moderatorIds", Collections.emptyList());
        newForum.put("memberIds", Collections.emptyList());
        newForum.put("noJoined", 0);
        newForum.put("forumBackground", backgroundFilePath.toString());
        newForum.put("forumIcon", iconFilePath.toString());

        try {
            db.getDb().collection("FORUMS")
                    .add(newForum)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            try {
                                finish();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String[] convertStringTagToArray(String tagString){

        String[] array = tagString.split("#");
        for (int i = 0; i < array.length; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    private void uploadBackgroundImage(Uri submitFilePath) {

        if(submitFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading forum background...");
            progressDialog.show();

            StorageReference ref = db.getStorageRef().child("images/"+ UUID.randomUUID().toString());
            ref.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateForumView.this, "Uploaded Image", Toast.LENGTH_SHORT).show();
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            backgroundFilePath = uri;
                            uploadIconImage(iconFilePath);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateForumView.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
    }


    private void uploadIconImage(Uri submitFilePath) {

        if(submitFilePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading forum background...");
            progressDialog.show();

            StorageReference ref = db.getStorageRef().child("images/"+ UUID.randomUUID().toString());
            ref.putFile(submitFilePath)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateForumView.this, "Uploaded Image", Toast.LENGTH_SHORT).show();
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            iconFilePath = uri;
                            createForum();
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(CreateForumView.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    });
        }
    }

}