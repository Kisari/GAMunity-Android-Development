package rmitcom.asm1.gamunity.components.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static androidx.core.app.ActivityCompat.recreate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.adapter.ForumListAdapter;
import rmitcom.asm1.gamunity.adapter.ForumTagListAdapter;
import rmitcom.asm1.gamunity.components.views.forum.CreateForumView;
import rmitcom.asm1.gamunity.components.views.forum.ForumView;
import rmitcom.asm1.gamunity.db.FireBaseManager;
import rmitcom.asm1.gamunity.helper.FirebaseFetchAndSetUI;
import rmitcom.asm1.gamunity.model.Constant;
import rmitcom.asm1.gamunity.model.Forum;

public class HomeFragment extends Fragment implements FirebaseFetchAndSetUI,ForumTagListAdapter.ItemClickListener {
    private int currentForumId = 0;
    private final FireBaseManager db = new FireBaseManager();
    private final Constant constant = new Constant();
    private ArrayList<Forum> forumList= new ArrayList<>();
    private View currentView;
    private ForumListAdapter forumListAdapter;
    private ForumTagListAdapter tagListAdapter;
    public HomeFragment() {
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        currentView = view;

        //start to initialize the function
        fetchData();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == constant.CREATE){
            if(resultCode == constant.SUCCESS){
                Forum newAddedforum = (Forum) data.getSerializableExtra("newForum");
                forumList.add(newAddedforum);

                this.forumListAdapter.notifyDataSetChanged();
            }
        }

        if (requestCode == constant.DELETE) {
            if (resultCode == RESULT_OK) {
                recreate(requireActivity());
                this.forumListAdapter.notifyDataSetChanged();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fetchData() {
        db.getDb().collection(constant.forums)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String forumId = document.getString("forumId");
                            String forumRef = document.getId();
                            String forumTitle = document.getString("title");
                            String forumChiefAdmin = document.getString("chiefAdmin");
                            ArrayList<String> forumCategory = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("category")));
                            ArrayList<String> forumMemberIds = new ArrayList<>((List<String>) Objects.requireNonNull(document.get("memberIds")));
                            String forumBackground = document.getString("forumBackground");
                            String forumIcon = document.getString("forumIcon");

                            Forum newForum = new Forum(forumId, forumRef, forumChiefAdmin, forumTitle, forumCategory, forumMemberIds, forumBackground, forumIcon);
                            forumList.add(newForum);
                        }
                        setUI();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    @Override
    public void setUI() {

        initializeForumListView();

        initializeForumTagSelectionView();

        initializeSearchFunctionView();

        ImageButton createForumBtn = currentView.findViewById(R.id.createForumBtn);

        createForumBtn.setOnClickListener(v -> {
            Intent createForumViewIntent = new Intent(requireContext(), CreateForumView.class);
            createForumViewIntent.putExtra("nextForumID", String.valueOf(forumList.size()+1));
            startActivityForResult(createForumViewIntent, constant.CREATE);
        });
    }

    private void initializeForumListView(){
        //get the listview
        ListView forumListView = currentView.findViewById(R.id.forumListView);

        //initialize adapter
        this.forumListAdapter = new ForumListAdapter(forumList);

        //set adapter to forum view list
        forumListView.setAdapter(forumListAdapter);
    }

    private void initializeForumTagSelectionView(){
        //get the listview
        RecyclerView forumTagLayout = (RecyclerView) currentView.findViewById(R.id.forumTagsLayout);

        //initialize adapter
        this.tagListAdapter = new ForumTagListAdapter(getContext(), constant.tagList);

        //set on click listener for every tag
        tagListAdapter.setClickListener(this);

        //initialize the Horizontal Scroll View for tag list
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        forumTagLayout.setLayoutManager(layoutManager);

        //set adapter to tags view list
        forumTagLayout.setAdapter(tagListAdapter);
    }

    //override onClickListener for every tag
    @Override
    public void onItemClick(View view, int position) {
        RecyclerView forumTagLayout = currentView.findViewById(R.id.forumTagsLayout);
        RecyclerView.ViewHolder holder = forumTagLayout.findViewHolderForLayoutPosition(position);

        assert holder != null;
        TextView tagName = holder.itemView.findViewById(R.id.forumTagItem);
        if(forumListAdapter.getTagName().equals(tagListAdapter.getItem(position))){
            forumListAdapter.removeTag();
            GradientDrawable drawable = (GradientDrawable)tagName.getBackground();
            drawable.mutate();
            drawable.setColor(Color.parseColor("#ffffff"));
            tagName.setTextColor(Color.parseColor("#4900FF"));
        }
        else{
            if(!Objects.equals(forumListAdapter.getTagName(), "") && forumListAdapter.getTagPosition() != -1){
                RecyclerView.ViewHolder prevHolder = forumTagLayout.findViewHolderForLayoutPosition(forumListAdapter.getTagPosition());
                assert prevHolder != null;
                TextView prevTagName = prevHolder.itemView.findViewById(R.id.forumTagItem);
                GradientDrawable drawable = (GradientDrawable)prevTagName.getBackground();
                drawable.mutate();
                drawable.setColor(Color.parseColor("#ffffff"));
                prevTagName.setTextColor(Color.parseColor("#4900FF"));
            }
            forumListAdapter.setTag(tagListAdapter.getItem(position), position);
            GradientDrawable drawable = (GradientDrawable)tagName.getBackground();
            drawable.mutate();
            drawable.setColor(Color.parseColor("#4900FF"));
            tagName.setTextColor(Color.parseColor("#ffffff"));
        }
        SearchView searchView = currentView.findViewById(R.id.searchBar);
        forumListAdapter.getFilter().filter(searchView.getQuery().toString());

    }

    private void initializeSearchFunctionView(){
        SearchView searchView = currentView.findViewById(R.id.searchBar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){

            @Override
            public boolean onQueryTextSubmit(String query) {
                filteringForums(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteringForums(newText);
                return false;
            }
        });
    }


    private void filteringForums(String text){
        forumListAdapter.getFilter().filter(text);
        TextView message = currentView.findViewById(R.id.filteringMessage);
        if(forumListAdapter.getCount() > 0 || text.equals("")){
            message.setVisibility(View.GONE);
        }
        else{
            message.setVisibility(View.VISIBLE);
        }
    }

}