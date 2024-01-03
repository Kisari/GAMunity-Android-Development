package rmitcom.asm1.gamunity.adapter;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Objects;

import rmitcom.asm1.gamunity.R;
import rmitcom.asm1.gamunity.components.ui.AsyncImage;
import rmitcom.asm1.gamunity.model.Forum;

public class ForumListAdapter extends BaseAdapter implements Filterable {

    private final ArrayList<Forum> forumList;
    private ArrayList<Forum> currentForumList;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewForumList;

        if (convertView == null) {
            viewForumList = View.inflate(parent.getContext(), R.layout.ui_forum_list_view_item, null);
        } else viewForumList = convertView;

        Forum forumItem = (Forum) getItem(position);

        ImageView forumBackground = viewForumList.findViewById(R.id.forumBackground);
        ShapeableImageView forumIcon = viewForumList.findViewById(R.id.forumIcon);
        ProgressBar forumIconProgress = viewForumList.findViewById(R.id.progress1);
        ProgressBar forumBackgroundProgress = viewForumList.findViewById(R.id.progress2);
        TextView forumTitle = viewForumList.findViewById(R.id.forumTitle);
        Button forumAction = viewForumList.findViewById(R.id.forumActionBtn);

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
                Log.d(TAG, "performFiltering: " + constraint.equals(""));
                Log.d(TAG, "performFiltering: " + constraint);

                for (int i = 0; i < forumList.size(); i++) {
                    String dataNames = forumList.get(i).getTitle();
                    if (dataNames.toLowerCase().startsWith(constraint.toString()))  {
                        FilteredArrayNames.add(forumList.get(i));
                    }
                }

                if(!Objects.equals(tag, "")){
                    if(FilteredArrayNames.size() == 0 && constraint.equals("")){
                        for (int i = 0; i < forumList.size(); i++) {
                            ArrayList<String> forumCatsList = forumList.get(i).getCategory();
                            if (forumCatsList.contains(tag))  {
                                FilteredArrayNames.add(forumList.get(i));
                            }
                        }
                    }
                    else{
                        for (int i = 0; i < FilteredArrayNames.size(); i++) {
                            ArrayList<String> forumCatsList = FilteredArrayNames.get(i).getCategory();
                            if (!forumCatsList.contains(tag))  {
                                FilteredArrayNames.remove(FilteredArrayNames.get(i));
                            }
                        }
                    }
                }

                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }

        };
    }
}
